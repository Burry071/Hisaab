package com.hisaab.data.ingestion

import com.hisaab.domain.llm.LlmPromptLibrary
import com.hisaab.domain.llm.LlmService
import com.hisaab.parser.model.IngestionSource
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.security.MessageDigest

/**
 * Tier 3 Fallback Parser — uses the injected [LlmService] to parse SMS bodies
 * that Tier 1 (native regex) and Tier 2 (heuristic) could not handle.
 *
 * Called by [ParserOrchestrator] when:
 *   • confidenceScore < 0.6
 *   • type == UNKNOWN after Tier 1+2
 *
 * The LLM is prompted to extract structured fields in plain-text format.
 * We then parse that structured response deterministically.
 *
 * This design ensures:
 *   1. The LLM handles long-tail formats we haven't seen
 *   2. The response parsing is still regex/deterministic (no hallucination on numbers)
 *   3. Any provider (Gemini / OpenAI / Ollama) works transparently
 */
class GeminiParserFallback(
    private val llmService: LlmService,
) {
    /**
     * @param smsBody   Raw SMS text
     * @param sender    Sender ID (e.g., "HBL-Alert", "JazzCash")
     * @param timestamp Epoch ms of the SMS
     * @return Parsed transaction, or null if LLM response is unparseable
     */
    suspend fun parse(
        smsBody: String,
        sender: String,
        timestamp: Long,
    ): ParsedTransaction? {
        return try {
            val response = llmService.complete(
                systemPrompt = LlmPromptLibrary.PARSER_SYSTEM,
                userPrompt   = LlmPromptLibrary.parserUser(smsBody, sender),
                maxTokens    = 200,
            )
            parseStructuredResponse(response.content, smsBody, sender, timestamp)
        } catch (e: Exception) {
            null  // Tier 3 failure → skip this SMS gracefully
        }
    }

    // ── Response parser ───────────────────────────────────────────────────────

    private fun parseStructuredResponse(
        llmOutput: String,
        originalSms: String,
        sender: String,
        timestamp: Long,
    ): ParsedTransaction? {
        val lines = llmOutput.lines().associate { line ->
            val idx = line.indexOf(":")
            if (idx < 0) "" to ""
            else line.substring(0, idx).trim().uppercase() to line.substring(idx + 1).trim()
        }

        val typeStr     = lines["TYPE"]       ?: return null
        val amountStr   = lines["AMOUNT"]     ?: return null
        val balanceStr  = lines["BALANCE"]
        val counterparty= lines["COUNTERPARTY"]?.takeIf { it != "UNKNOWN" }
        val refNo       = lines["REF"]        ?.takeIf { it != "UNKNOWN" }
        val confidence  = lines["CONFIDENCE"] ?.toFloatOrNull() ?: 0.5f

        val type   = parseType(typeStr)
        val amount = amountStr.replace(",", "").toBigDecimalOrNull() ?: return null
        val balance= balanceStr?.replace(",", "")?.toBigDecimalOrNull()

        // Guard: reject obviously wrong amounts
        if (amount <= BigDecimal.ZERO) return null

        return ParsedTransaction(
            id               = deterministicId(sender, originalSms, timestamp),
            source           = IngestionSource.SMS,
            institution      = normaliseInstitution(sender),
            type             = type,
            amount           = amount,
            currency         = "PKR",
            balanceAfter     = balance,
            counterparty     = counterparty,
            referenceNumber  = refNo,
            rawSmsBody       = originalSms,
            timestampEpochMs = timestamp,
            confidenceScore  = confidence.coerceIn(0f, 1f),
        )
    }

    private fun parseType(raw: String): TransactionType = when (raw.uppercase().trim()) {
        "DEBIT"           -> TransactionType.DEBIT
        "CREDIT"          -> TransactionType.CREDIT
        "TRANSFER"        -> TransactionType.TRANSFER
        "BILL_PAYMENT"    -> TransactionType.BILL_PAYMENT
        "ATM_WITHDRAWAL"  -> TransactionType.DEBIT
        else              -> TransactionType.UNKNOWN
    }

    private fun normaliseInstitution(sender: String): String {
        val s = sender.uppercase()
        return when {
            s.contains("HBL")      -> "HBL"
            s.contains("MCB")      -> "MCB"
            s.contains("UBL")      -> "UBL"
            s.contains("MEEZAN")   -> "Meezan"
            s.contains("ALFALAH")  -> "Bank Alfalah"
            s.contains("JAZZCASH") -> "JazzCash"
            s.contains("EASYPAISA")-> "Easypaisa"
            s.contains("NAYAPAY")  -> "NayaPay"
            s.contains("SADAPAY")  -> "SadaPay"
            else                   -> sender
        }
    }

    private fun deterministicId(sender: String, body: String, ts: Long): String {
        val input = "$sender|$body|$ts"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
