package com.hisaab.parser

import com.hisaab.parser.bank.*
import com.hisaab.parser.model.ParsedTransaction

import com.hisaab.parser.wallet.EasypaisaParser
import com.hisaab.parser.wallet.JazzCashParser
import com.hisaab.parser.wallet.WalletParser
import com.hisaab.parser.model.TransactionType

/**
 * SmsParserRegistry — dispatches each incoming SMS to the correct bank parser.
 *
 * Tier 1: Exact sender-ID match (deterministic regex parsers).
 * Tier 2: Pattern match on body (for unknown sender IDs).
 * Tier 3: LLM fallback (invoked only when confidence < 0.65 or no match).
 *
 * All Tier 1/2 parsers registered here. New banks: add to parsers list only.
 */
object SmsParserRegistry {

    /** All registered Tier 1/2 bank parsers */
    private val bankParsers: List<BankParser> = listOf(
        HBLParser(),         // HBL — most common
        MeezanBankParser(),  // Meezan Islamic
        AlliedBankParser,    // Allied Bank (ABL)
        NBPParser,           // National Bank of Pakistan
        HabibMetroParser,    // Habib Metropolitan Bank
        BankOfPunjabParser,  // Bank of Punjab
        AskariParser,        // Askari Bank
    )

    /** All registered Tier 1/2 wallet parsers */
    private val walletParsers: List<WalletParser> = listOf(
        JazzCashParser(),    // JazzCash mobile wallet
        EasypaisaParser(),   // Easypaisa wallet
    )

    /**
     * Parse an SMS and return a [ParsedTransaction] or null (triggers Tier 3 LLM fallback).
     *
     * @param smsBody  Full raw SMS body text
     * @param sender   Sender ID from Android SmsMessage.originatingAddress
     * @param timestamp Unix epoch milliseconds of SMS receipt
     */
    fun parse(smsBody: String, sender: String, timestamp: Long): ParsedTransaction? {
        // Fast path: find matching parser by sender ID
        val exactBankParser = bankParsers.firstOrNull { it.canParse(sender, smsBody) }
        if (exactBankParser != null) {
            return exactBankParser.parse(sender, smsBody, timestamp)
                .let { PakistaniSmsNormalizer.normalize(it) }
        }
        val exactWalletParser = walletParsers.firstOrNull { it.canParse(sender, smsBody) }
        if (exactWalletParser != null) {
            return exactWalletParser.parse(sender, smsBody, timestamp)
                .let { PakistaniSmsNormalizer.normalize(it) }
        }

        // Fallback: try every parser on body (for aggregated senders like INFO, ALERTS)
        for (parser in bankParsers) {
            val result = parser.parse(sender, smsBody, timestamp)
            if (result.type != TransactionType.UNKNOWN) {
                return PakistaniSmsNormalizer.normalize(result)
            }
        }
        for (parser in walletParsers) {
            val result = parser.parse(sender, smsBody, timestamp)
            if (result.type != TransactionType.UNKNOWN) {
                return PakistaniSmsNormalizer.normalize(result)
            }
        }

        // No match → return null to trigger Tier 3
        return null
    }

    /**
     * Parse a batch of SMS messages.
     * Applies deduplication (network retries) via [PakistaniSmsNormalizer.normalizeBatch].
     */
    fun parseBatch(messages: List<Triple<String, String, Long>>): List<ParsedTransaction> {
        val parsed = messages.mapNotNull { (body, sender, ts) ->
            val exactBankParser = bankParsers.firstOrNull { it.canParse(sender, body) }
            if (exactBankParser != null) return@mapNotNull exactBankParser.parse(sender, body, ts)
            
            val exactWalletParser = walletParsers.firstOrNull { it.canParse(sender, body) }
            if (exactWalletParser != null) return@mapNotNull exactWalletParser.parse(sender, body, ts)

            bankParsers.firstOrNull { it.parse(sender, body, ts).type != TransactionType.UNKNOWN }?.parse(sender, body, ts)
                ?: walletParsers.firstOrNull { it.parse(sender, body, ts).type != TransactionType.UNKNOWN }?.parse(sender, body, ts)
        }
        return PakistaniSmsNormalizer.normalizeBatch(parsed)
    }

    /** Returns list of all registered institution names (for demo display) */
    fun registeredInstitutions(): List<String> = listOf(
        "HBL", "JazzCash", "Meezan Bank", "Easypaisa",
        "Allied Bank", "NBP", "Habib Metropolitan", "Bank of Punjab", "Askari Bank",
    )
}
