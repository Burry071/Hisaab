package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from JazzCash (sender: "JazzCash" or "JAZZ").
 *
 * Supported formats:
 *  DEBIT  → "You have sent Rs.<amount> to <name> ..."
 *  CREDIT → "You have received Rs.<amount> from <name> ..."
 *  TOP_UP → "... loaded with Rs.<amount> ..."
 *  BILL   → "<util> bill of Rs.<amount> paid ..."
 */
class JazzCashParser : WalletParser {

    companion object {
        private val SENDERS = setOf("jazzcash", "jazz")

        // Amount pattern — matches "Rs.5,000" or "Rs.1,50,000"
        private val AMOUNT_SENT     = Regex("""You have sent Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED = Regex("""You have received Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_LOADED   = Regex("""loaded with Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""bill of Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""(?:balance|Balance)[:\s]+Rs\.([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""sent Rs\.[\d,]+ to ([A-Za-z ]+?)\s*(?:\(|\.| )""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM = Regex("""received Rs\.[\d,]+ from ([A-Za-z ]+?)\s*(?:\(|\.| )""", RegexOption.IGNORE_CASE)
        private val BILL_NAME       = Regex("""^([A-Z]+)\s+bill""")
        private val REF_NO          = Regex("""Ref#([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        // DEBIT — "You have sent"
        AMOUNT_SENT.firstGroup(body)?.let { rawAmt ->
            val counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd()
            return buildTransaction(
                sender          = sender,
                body            = body,
                timestampEpochMs = timestampEpochMs,
                institution     = "JazzCash",
                type            = TransactionType.DEBIT,
                amount          = parseAmount(rawAmt),
                balanceAfter    = balance,
                counterparty    = counterparty,
                referenceNumber = ref,
            )
        }

        // CREDIT — "You have received"
        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            val counterparty = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd()
            return buildTransaction(
                sender          = sender,
                body            = body,
                timestampEpochMs = timestampEpochMs,
                institution     = "JazzCash",
                type            = TransactionType.CREDIT,
                amount          = parseAmount(rawAmt),
                balanceAfter    = balance,
                counterparty    = counterparty,
                referenceNumber = ref,
            )
        }

        // TOP_UP — "loaded with"
        AMOUNT_LOADED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender          = sender,
                body            = body,
                timestampEpochMs = timestampEpochMs,
                institution     = "JazzCash",
                type            = TransactionType.TOP_UP,
                amount          = parseAmount(rawAmt),
                balanceAfter    = balance,
                referenceNumber = ref,
            )
        }

        // BILL_PAYMENT — "<UTIL> bill of Rs."
        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            val utility = BILL_NAME.firstGroup(body)
            return buildTransaction(
                sender          = sender,
                body            = body,
                timestampEpochMs = timestampEpochMs,
                institution     = "JazzCash",
                type            = TransactionType.BILL_PAYMENT,
                amount          = parseAmount(rawAmt),
                balanceAfter    = balance,
                counterparty    = utility,
                referenceNumber = ref,
            )
        }

        // Unknown format — return low-confidence record for Tier 3 fallback
        return buildTransaction(
            sender          = sender,
            body            = body,
            timestampEpochMs = timestampEpochMs,
            institution     = "JazzCash",
            type            = TransactionType.UNKNOWN,
            amount          = BigDecimal.ZERO,
            confidenceScore = 0.3f,
        )
    }
}
