package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from Konnect by HBL (sender: "Konnect" or "KONNECT").
 * Konnect is HBL's mobile wallet — distinct from HBL bank SMS.
 *
 * Supported formats:
 *  DEBIT  → "PKR <amount> sent to <name> via Konnect ..."
 *  CREDIT → "PKR <amount> received in your Konnect account from <name> ..."
 *  BILL   → "<util> bill PKR <amount> paid via Konnect ..."
 *  TOP_UP → "PKR <amount> deposited in your Konnect account ..."
 */
class KonnectParser : WalletParser {

    companion object {
        private val SENDERS = setOf("konnect")

        private val AMOUNT_SENT     = Regex("""PKR ([\d,]+) sent to""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED = Regex("""PKR ([\d,]+) received in""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""bill PKR ([\d,]+) paid""", RegexOption.IGNORE_CASE)
        private val AMOUNT_DEPOSIT  = Regex("""PKR ([\d,]+) deposited in""", RegexOption.IGNORE_CASE)

        private val BALANCE            = Regex("""(?:Remaining balance|Balance):\s*PKR\s*([\d,]+)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_TO    = Regex("""sent to ([A-Za-z ]+?) via Konnect""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM  = Regex("""received.*?from ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val BILL_NAME          = Regex("""^([A-Za-z]+) bill PKR""", RegexOption.IGNORE_CASE)
        private val REF_NO             = Regex("""Ref[:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_SENT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Konnect", type = TransactionType.DEBIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Konnect", type = TransactionType.CREDIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Konnect", type = TransactionType.BILL_PAYMENT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = BILL_NAME.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_DEPOSIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Konnect", type = TransactionType.TOP_UP,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        return buildTransaction(
            sender = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution = "Konnect", type = TransactionType.UNKNOWN,
            amount = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
