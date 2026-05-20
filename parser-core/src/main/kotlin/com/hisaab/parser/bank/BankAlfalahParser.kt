package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from Bank Alfalah.
 * Sender IDs: "ALFALAH", "BANKALFALAH", "BAL"
 *
 * Supported formats:
 *  DEBIT    → "PKR <amount> has been debited ..."
 *  CREDIT   → "PKR <amount> has been credited ..."
 *  TRANSFER → "You have transferred PKR <amount> to ..."
 *  BILL     → "Your bill payment of PKR <amount> ..."
 */
class BankAlfalahParser : BankParser {

    companion object {
        private val SENDERS = setOf("alfalah", "bankalfalah", "bal")

        private val AMOUNT_DEBIT    = Regex("""PKR\s+([\d,]+)\s+has been debited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT   = Regex("""PKR\s+([\d,]+)\s+has been credited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_TRANSFER = Regex("""transferred PKR\s+([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""bill payment of PKR\s+([\d,]+)""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""[Aa]vailable [Bb]alance[:\s]+PKR\s*([\d,]+)""")
        private val BALANCE_ALT     = Regex("""[Bb]alance[:\s]+PKR\s*([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""transferred PKR[\d,\s]+to ([A-Za-z ]+?)(?:\s*\(|\.|\s{2}|$)""", RegexOption.IGNORE_CASE)
        private val REF_NO          = Regex("""(?:Ref|TRN|Ref#)[.#:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase().replace("-", "").replace(" ", "") in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE.firstGroup(body) ?: BALANCE_ALT.firstGroup(body))?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Bank Alfalah", type = TransactionType.DEBIT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        AMOUNT_CREDIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Bank Alfalah", type = TransactionType.CREDIT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        AMOUNT_TRANSFER.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Bank Alfalah", type = TransactionType.TRANSFER,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(),
                referenceNumber = ref,
            )
        }

        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Bank Alfalah", type = TransactionType.BILL_PAYMENT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        return buildTransaction(
            sender = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution = "Bank Alfalah", type = TransactionType.UNKNOWN,
            amount = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
