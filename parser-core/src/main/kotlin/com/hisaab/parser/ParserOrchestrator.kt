package com.hisaab.parser

import com.hisaab.parser.bank.BankAlfalahParser
import com.hisaab.parser.bank.BankParser
import com.hisaab.parser.bank.HBLParser
import com.hisaab.parser.bank.MCBParser
import com.hisaab.parser.bank.MeezanBankParser
import com.hisaab.parser.bank.UBLParser
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.EasypaisaParser
import com.hisaab.parser.wallet.JazzCashParser
import com.hisaab.parser.wallet.KonnectParser
import com.hisaab.parser.wallet.NayaPayParser
import com.hisaab.parser.wallet.SadaPayParser
import com.hisaab.parser.wallet.UPaisaParser
import com.hisaab.parser.wallet.WalletParser
import com.hisaab.parser.wallet.ZindigiParser

/**
 * Master orchestrator for all 15 bank + wallet parsers.
 *
 * Tier 1 — Native regex parsers (this class). Handles ~80% of volume.
 * Tier 2 — Heuristic fallback (future: generic pattern matching).
 * Tier 3 — Gemini API fallback (for confidence < 0.6 or UNKNOWN type).
 *
 * Usage:
 *   val orchestrator = ParserOrchestrator()
 *   val tx = orchestrator.parse(senderAddress, smsBody, System.currentTimeMillis())
 */
class ParserOrchestrator {

    /** All 7 wallet parsers — ordered by estimated volume (JazzCash first). */
    private val walletParsers: List<WalletParser> = listOf(
        JazzCashParser(),
        EasypaisaParser(),
        NayaPayParser(),
        SadaPayParser(),
        UPaisaParser(),
        ZindigiParser(),
        KonnectParser(),
    )

    /** All 5 bank parsers. */
    private val bankParsers: List<BankParser> = listOf(
        HBLParser(),
        UBLParser(),
        MCBParser(),
        MeezanBankParser(),
        BankAlfalahParser(),
    )

    /**
     * Attempts to parse an SMS using all 15 parsers.
     *
     * @return [ParsedTransaction] — always non-null.
     *   - confidenceScore >= 0.9  → clean regex match
     *   - confidenceScore = 0.3   → unrecognised format; escalate to Tier 3
     *   - type == UNKNOWN         → escalate to Tier 3 (GeminiParserFallback)
     */
    fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val normalizedSender = sender.trim()

        // Wallet parsers — fast path
        walletParsers.firstOrNull { it.canParse(normalizedSender, body) }?.let { parser ->
            return parser.parse(normalizedSender, body, timestampEpochMs)
        }

        // Bank parsers — second path
        bankParsers.firstOrNull { it.canParse(normalizedSender, body) }?.let { parser ->
            return parser.parse(normalizedSender, body, timestampEpochMs)
        }

        // No match — return a Tier 3 escalation record
        return buildUnknownTransaction(sender, body, timestampEpochMs)
    }

    /**
     * Returns true if this SMS will be escalated to Tier 3 (Gemini API).
     * Called by the ingestion pipeline to decide whether to queue a Gemini call.
     */
    fun requiresTier3Fallback(result: ParsedTransaction): Boolean =
        result.type == TransactionType.UNKNOWN || result.confidenceScore < 0.6f

    private fun buildUnknownTransaction(
        sender: String,
        body: String,
        timestampEpochMs: Long,
    ): ParsedTransaction {
        val id = com.hisaab.parser.wallet.ParserUtils.deterministicId(sender, body, timestampEpochMs)
        return ParsedTransaction(
            id               = id,
            source           = com.hisaab.parser.model.IngestionSource.SMS,
            institution      = sender,
            type             = TransactionType.UNKNOWN,
            amount           = java.math.BigDecimal.ZERO,
            currency         = "PKR",
            balanceAfter     = null,
            counterparty     = null,
            referenceNumber  = null,
            rawSmsBody       = body,
            timestampEpochMs = timestampEpochMs,
            confidenceScore  = 0.2f,  // below Tier 3 threshold — will always escalate
        )
    }

    /** Useful for diagnostics — returns which parser matched, or "Unmatched". */
    fun diagnose(sender: String, body: String): String {
        walletParsers.firstOrNull { it.canParse(sender, body) }
            ?.let { return it::class.simpleName ?: "WalletParser" }
        bankParsers.firstOrNull { it.canParse(sender, body) }
            ?.let { return it::class.simpleName ?: "BankParser" }
        return "Unmatched → Tier 3 escalation"
    }
}
