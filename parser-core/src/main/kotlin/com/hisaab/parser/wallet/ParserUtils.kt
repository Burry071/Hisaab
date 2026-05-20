package com.hisaab.parser.wallet

import com.hisaab.parser.model.IngestionSource
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.security.MessageDigest

/**
 * Utility functions shared across all wallet parsers.
 * Keeps individual parsers lean — just regex patterns and institution name.
 */
internal object ParserUtils {

    /**
     * Strips commas and whitespace then converts to BigDecimal.
     * Handles formats: "5,000"  "1,50,000"  "3.200"
     */
    fun parseAmount(raw: String): BigDecimal =
        BigDecimal(raw.replace(",", "").trim())

    /**
     * SHA-256 deterministic ID so the same SMS never creates two records.
     */
    fun deterministicId(sender: String, body: String, timestampEpochMs: Long): String {
        val input = "$sender|$body|$timestampEpochMs"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Extract first regex group as a trimmed string, or null.
     */
    fun Regex.firstGroup(text: String): String? =
        find(text)?.groupValues?.getOrNull(1)?.trim()

    /**
     * Convenience: build a [ParsedTransaction] with sane defaults.
     */
    fun buildTransaction(
        sender: String,
        body: String,
        timestampEpochMs: Long,
        institution: String,
        type: TransactionType,
        amount: BigDecimal,
        balanceAfter: BigDecimal? = null,
        counterparty: String? = null,
        referenceNumber: String? = null,
        confidenceScore: Float = 0.9f,
    ) = ParsedTransaction(
        id = deterministicId(sender, body, timestampEpochMs),
        source = IngestionSource.SMS,
        institution = institution,
        type = type,
        amount = amount,
        currency = "PKR",
        balanceAfter = balanceAfter,
        counterparty = counterparty,
        referenceNumber = referenceNumber,
        rawSmsBody = body,
        timestampEpochMs = timestampEpochMs,
        confidenceScore = confidenceScore,
    )
}
