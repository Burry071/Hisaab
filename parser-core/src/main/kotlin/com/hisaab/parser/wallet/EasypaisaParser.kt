package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from Easypaisa (sender: "Easypaisa" or "EP").
 *
 * Supported formats:
 *  DEBIT  → "PKR <amount> sent to <name> ..."
 *  CREDIT → "PKR <amount> received ... from <name> ..."
 *  BILL   → "Your <util> bill of PKR <amount> has been paid ..."
 *  TOP_UP → "PKR <amount> added to your Easypaisa account ..."
 */
class EasypaisaParser : WalletParser {

    companion object {
        private val SENDERS = setOf("easypaisa", "ep")

        private val AMOUNT_SENT     = Regex("""PKR ([\d,]+) sent to""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED = Regex("""PKR ([\d,]+) received""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""bill of PKR ([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_ADDED    = Regex("""PKR ([\d,]+) added to""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""(?:balance|Balance):\s*PKR\s*([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""sent to ([A-Za-z ]+?)\s*(?:\(|\.|from|Ref)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM = Regex("""received.*?from ([A-Za-z ]+?)\s*(?:\(|\.|Easypaisa|Balance)""", RegexOption.IGNORE_CASE)
        private val BILL_NAME       = Regex("""Your ([A-Z]+) bill""", RegexOption.IGNORE_CASE)
        private val REF_NO          = Regex("""Ref(?:erence)?[:\s#]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        // DEBIT — "PKR X sent to"
        AMOUNT_SENT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "Easypaisa",
                type             = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        // CREDIT — "PKR X received"
        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "Easypaisa",
                type             = TransactionType.CREDIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        // BILL_PAYMENT — "bill of PKR X"
        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "Easypaisa",
                type             = TransactionType.BILL_PAYMENT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = BILL_NAME.firstGroup(body),
                referenceNumber  = ref,
            )
        }

        // TOP_UP — "PKR X added to your Easypaisa"
        AMOUNT_ADDED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "Easypaisa",
                type             = TransactionType.TOP_UP,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        return buildTransaction(
            sender           = sender,
            body             = body,
            timestampEpochMs = timestampEpochMs,
            institution      = "Easypaisa",
            type             = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO,
            confidenceScore  = 0.3f,
        )
    }
}
