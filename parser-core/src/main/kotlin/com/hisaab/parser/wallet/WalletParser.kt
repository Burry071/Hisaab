package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction

/**
 * Contract every wallet parser must implement.
 * The IngestionAgent routes incoming SMS through registered parsers
 * by calling [canParse] first; only the matching parser's [parse] is invoked.
 */
interface WalletParser {

    /**
     * Returns true if this parser owns the given [sender] + [body] combination.
     * Must be fast and side-effect-free (called on every SMS).
     */
    fun canParse(sender: String, body: String): Boolean

    /**
     * Parses a raw SMS into a [ParsedTransaction].
     * Implementors MUST:
     *  - set [ParsedTransaction.confidenceScore] >= 0.8 for full regex matches
     *  - set a deterministic [ParsedTransaction.id] via sha256(sender+body+timestamp)
     *  - set [ParsedTransaction.institution] to the canonical institution name
     *  - never throw; return a transaction with confidenceScore < 0.6 for unknown formats
     */
    fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction
}
