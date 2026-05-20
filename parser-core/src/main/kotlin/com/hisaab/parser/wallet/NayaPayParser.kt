package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from NayaPay (sender: "NayaPay" or "NAYAPAY").
 *
 * Supported formats:
 *  DEBIT  → "You've sent PKR <amount> to <name> ..."
 *  CREDIT → "PKR <amount> received from <name> ..."
 *  BILL   → "<util> bill payment of PKR <amount> ..."
 */
class NayaPayParser : WalletParser {

    companion object {
        private val SENDERS = setOf("nayapay")

        private val AMOUNT_SENT     = Regex("""You've sent PKR ([\d,]+) to""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED = Regex("""PKR ([\d,]+) received from""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""bill payment of PKR ([\d,]+)""", RegexOption.IGNORE_CASE)

        private val BALANCE_AVAIL   = Regex("""Available balance:\s*PKR\s*([\d,]+)""", RegexOption.IGNORE_CASE)
        private val BALANCE_NEW     = Regex("""New balance:\s*PKR\s*([\d,]+)""", RegexOption.IGNORE_CASE)
        private val BALANCE_GENERIC = Regex("""[Bb]alance:\s*PKR\s*([\d,]+)""")

        private val COUNTERPARTY_TO   = Regex("""sent PKR [\d,]+ to ([A-Za-z ]+?)\s*[..]""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM = Regex("""received from ([A-Za-z ]+?)\s*[..]""", RegexOption.IGNORE_CASE)
        private val BILL_NAME         = Regex("""^([A-Z]+) bill payment""", RegexOption.IGNORE_CASE)
        private val TXN_ID            = Regex("""Txn ID:\s*([\w-]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE_AVAIL.firstGroup(body)
            ?: BALANCE_NEW.firstGroup(body)
            ?: BALANCE_GENERIC.firstGroup(body))?.let { parseAmount(it) }
        val ref = TXN_ID.firstGroup(body)

        // DEBIT — "You've sent PKR X to"
        AMOUNT_SENT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "NayaPay",
                type             = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        // CREDIT — "PKR X received from"
        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "NayaPay",
                type             = TransactionType.CREDIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        // BILL_PAYMENT — "<util> bill payment of PKR X"
        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "NayaPay",
                type             = TransactionType.BILL_PAYMENT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = BILL_NAME.firstGroup(body),
                referenceNumber  = ref,
            )
        }

        return buildTransaction(
            sender           = sender,
            body             = body,
            timestampEpochMs = timestampEpochMs,
            institution      = "NayaPay",
            type             = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO,
            confidenceScore  = 0.3f,
        )
    }
}
