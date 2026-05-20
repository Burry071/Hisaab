package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from UPaisa (sender: "UPaisa").
 *
 * Supported formats:
 *  DEBIT  → "Rs.<amount> transferred to <name> ..."
 *  CREDIT → "Rs.<amount> received from <name> ..."
 *  BILL   → "<util> bill Rs.<amount> paid ..."
 *  TOP_UP → "Rs.<amount> loaded to UPaisa ..."
 */
class UPaisaParser : WalletParser {

    companion object {
        private val SENDERS = setOf("upaisa")

        private val AMOUNT_TRANSFERRED = Regex("""Rs\.([\d,]+) transferred to""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED    = Regex("""Rs\.([\d,]+) received from""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL        = Regex("""bill Rs\.([\d,]+) paid""", RegexOption.IGNORE_CASE)
        private val AMOUNT_LOADED      = Regex("""Rs\.([\d,]+) loaded to UPaisa""", RegexOption.IGNORE_CASE)

        private val BALANCE            = Regex("""UPaisa Balance:\s*Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_TO    = Regex("""transferred to ([A-Za-z ]+?)\s*(?:\(|\.|Ref)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM  = Regex("""received from ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val BILL_NAME          = Regex("""^([A-Za-z ]+?) bill Rs""", RegexOption.IGNORE_CASE)
        private val REF_NO             = Regex("""Ref(?:\s+No)?[:\s#]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_TRANSFERRED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "UPaisa", type = TransactionType.DEBIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "UPaisa", type = TransactionType.CREDIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "UPaisa", type = TransactionType.BILL_PAYMENT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = BILL_NAME.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_LOADED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "UPaisa", type = TransactionType.TOP_UP,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        return buildTransaction(
            sender = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution = "UPaisa", type = TransactionType.UNKNOWN,
            amount = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
