package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction

/**
 * Contract for all traditional bank SMS parsers.
 * Mirrors [WalletParser] so the orchestrator can treat bank and wallet
 * parsers uniformly through a common interface.
 */
interface BankParser {
    /**
     * Returns true if this parser can handle the given sender/body pair.
     * Checked before [parse] is called.
     */
    fun canParse(sender: String, body: String): Boolean

    /**
     * Parses a bank SMS and returns a canonical [ParsedTransaction].
     * Always returns a result — falls back to UNKNOWN type with
     * confidenceScore=0.3 when no pattern matches.
     */
    fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction
}
