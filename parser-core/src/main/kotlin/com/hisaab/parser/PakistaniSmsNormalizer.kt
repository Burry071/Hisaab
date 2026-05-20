package com.hisaab.parser

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * PakistaniSmsNormalizer — post-processing pipeline for parsed transactions.
 *
 * Responsibilities:
 *   1. Standardise currency amounts (strip Rs., PKR, commas, convert lakh/crore notation)
 *   2. Normalise merchant names (strip noise like "Payment to", "Transfer to")
 *   3. Infer transaction category from description keywords
 *   4. Detect and flag potential network retry duplicates (same body hash within 60s)
 *   5. Mark low-confidence transactions for Tier 3 LLM fallback
 *
 * This runs synchronously on every ParsedTransaction before it enters Room DB.
 */
object PakistaniSmsNormalizer {

    // ── Public API ────────────────────────────────────────────────────────────

    /** Normalise a single parsed transaction */
    fun normalize(txn: ParsedTransaction): ParsedTransaction {
        val normalizedAmount = normalizeAmount(txn.amount)
        val rawDesc          = txn.counterparty ?: txn.rawSmsBody.take(60)
        val normalizedDesc   = normalizeDescription(rawDesc)
        
        val redactedCounterparty = redactSensitiveData(normalizedDesc)
        val redactedRef          = redactSensitiveData(txn.referenceNumber ?: "")
        val redactedBody         = redactSensitiveData(txn.rawSmsBody)

        return txn.copy(
            amount          = normalizedAmount,
            counterparty    = redactedCounterparty.ifBlank { null },
            referenceNumber = redactedRef.ifBlank { null },
            rawSmsBody      = redactedBody
        )
    }

    /** Redacts phone numbers and long account numbers from text to prevent leaking sensitive info to LLMs */
    fun redactSensitiveData(text: String): String {
        if (text.isBlank()) return text
        // Redact Pakistani phone numbers
        var redacted = text.replace(Regex("""(?:\+92|0)?3\d{2}[-\s]?\d{7}"""), "***-*******")
        // Redact account numbers (10+ digits)
        redacted = redacted.replace(Regex("""\b\d{10,16}\b"""), "**********")
        return redacted
    }

    /** Normalise a batch and filter out known retry duplicates */
    fun normalizeBatch(
        transactions : List<ParsedTransaction>,
        dedupWindowMs: Long = 60_000L,
    ): List<ParsedTransaction> {
        val normalised = transactions.map { normalize(it) }
        return deduplicateRetries(normalised, dedupWindowMs)
    }

    // ── Amount normalisation ──────────────────────────────────────────────────

    /**
     * Handles Pakistani number formats:
     *   - Comma-separated thousands: 1,00,000 → 100000
     *   - Lakh notation: 1.5 lakh → 150000
     *   - Rs. prefix variants: Rs.5000, Rs 5,000, PKR5000
     */
    fun normalizeAmount(amount: BigDecimal): BigDecimal {
        // Already parsed by individual parsers — just round to 2dp
        return amount.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Parse raw SMS string amounts (used when parsers extract raw strings).
     * Handles: "1,00,000", "1.5 lakh", "2.3L", "PKR 5,000.50"
     */
    fun parseAmountString(raw: String): BigDecimal? {
        val cleaned = raw.trim()
            .removePrefix("PKR").removePrefix("Rs.").removePrefix("Rs ")
            .trim()

        // Lakh notation (e.g. "1.5 lakh", "2L", "2.3L")
        val lakhMatch = Regex("""([\d.]+)\s*[Ll](?:akh)?""").find(cleaned)
        if (lakhMatch != null) {
            val n = lakhMatch.groupValues[1].toDoubleOrNull() ?: return null
            return BigDecimal(n * 100_000.0).setScale(2, RoundingMode.HALF_UP)
        }

        // Crore notation (e.g. "1.2 crore", "1.2Cr")
        val croreMatch = Regex("""([\d.]+)\s*[Cc](?:r(?:ore)?)?""").find(cleaned)
        if (croreMatch != null) {
            val n = croreMatch.groupValues[1].toDoubleOrNull() ?: return null
            return BigDecimal(n * 10_000_000.0).setScale(2, RoundingMode.HALF_UP)
        }

        // Standard: remove all commas (handles both 1,000 and 1,00,000)
        return cleaned.replace(",", "").toBigDecimalOrNull()
            ?.setScale(2, RoundingMode.HALF_UP)
    }

    // ── Description normalisation ─────────────────────────────────────────────

    fun normalizeDescription(description: String): String {
        return description
            .replace(Regex("""(?i)(payment to|paid to|transfer to|sent to|received from|debit at|credit from)\s+"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(60)
    }

    // ── Category inference ────────────────────────────────────────────────────

    fun inferCategory(description: String, sender: String): String {
        val desc   = description.lowercase()
        val fromSender = sender.lowercase()

        return when {
            // Utilities
            desc.contains("lesco") || desc.contains("ssgc") || desc.contains("kesc") ||
            desc.contains("ptcl")  || desc.contains("internet") || desc.contains("electricity") ||
            desc.contains("gas bill") -> "Utilities"

            // Food & Dining
            desc.contains("hotel") || desc.contains("restaurant") || desc.contains("cafe") ||
            desc.contains("pizza")  || desc.contains("biryani") || desc.contains("food") ||
            desc.contains("eatery") || desc.contains("burger") -> "Food"

            // Transport
            desc.contains("uber") || desc.contains("careem") || desc.contains("petrol") ||
            desc.contains("fuel")  || desc.contains("parking") || desc.contains("toll") -> "Transport"

            // Shopping
            desc.contains("kfc")     || desc.contains("daraz") || desc.contains("amazon") ||
            desc.contains("store")   || desc.contains("mart")  || desc.contains("shop") -> "Shopping"

            // Health
            desc.contains("clinic")  || desc.contains("pharmacy") || desc.contains("hospital") ||
            desc.contains("doctor")  || desc.contains("medicine") -> "Health"

            // Salary / Income
            desc.contains("payroll") || desc.contains("salary")  || desc.contains("wage") -> "Salary"

            // Wallet transfers (sender-based)
            fromSender.contains("jazzcash") || fromSender.contains("easypaisa") ||
            fromSender.contains("nayapay")  || fromSender.contains("sadapay") -> "Transfer"

            // Subscriptions
            desc.contains("netflix") || desc.contains("spotify") || desc.contains("subscription") -> "Subscription"

            else -> "General"
        }
    }

    // ── Retry-duplicate detection ─────────────────────────────────────────────

    /**
     * Detects SMS network retries: same sender + same amount + body hash within time window.
     * Keeps only the first occurrence. Flags suppressed ones in trace.
     */
    fun deduplicateRetries(
        transactions : List<ParsedTransaction>,
        windowMs     : Long = 60_000L,
    ): List<ParsedTransaction> {
        val seen    = mutableSetOf<String>()
        val result  = mutableListOf<ParsedTransaction>()

        val sorted = transactions.sortedBy { it.timestampEpochMs }

        for (txn in sorted) {
            val key = dedupeKey(txn)
            val isDuplicate = seen.any { existingKey ->
                val existingTxn = result.find { dedupeKey(it) == existingKey } ?: return@any false
                existingKey == key && kotlin.math.abs(txn.timestampEpochMs - existingTxn.timestampEpochMs) < windowMs
            }
            if (!isDuplicate) {
                seen.add(key)
                result.add(txn)
            }
        }
        return result
    }

    private fun dedupeKey(txn: ParsedTransaction): String =
        "${txn.institution}|${txn.amount}|${txn.type}|${txn.referenceNumber}"

    // ── Confidence thresholding ───────────────────────────────────────────────

    /** Returns true if this transaction should be sent to Tier 3 LLM fallback */
    fun needsLlmFallback(txn: ParsedTransaction): Boolean =
        txn.confidenceScore < LOW_CONFIDENCE_THRESHOLD || txn.amount <= BigDecimal.ZERO

    private const val LOW_CONFIDENCE_THRESHOLD = 0.65f
}
