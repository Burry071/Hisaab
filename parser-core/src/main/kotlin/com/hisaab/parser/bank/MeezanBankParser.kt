package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from Meezan Bank (Islamic Bank).
 * Sender IDs: "MEEZANBANK", "MEEZAN", "MBL"
 *
 * Supported formats:
 *  DEBIT    → "PKR <amount> debited from your Meezan ..."
 *  CREDIT   → "PKR <amount> credited to your Meezan ..."
 *  TRANSFER → "Ibft of PKR <amount> to ..."  (Islamic Interbank Fund Transfer)
 *  BILL     → "Bill payment of PKR <amount> ..."
 */
class MeezanBankParser : BankParser {

    companion object {
        private val SENDERS = setOf("meezanbank", "meezan", "mbl")

        private val AMOUNT_DEBIT  = Regex("""PKR\s+([\d,]+)\s+debited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT = Regex("""PKR\s+([\d,]+)\s+credited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_IBFT   = Regex("""[Ii]bft of PKR\s+([\d,]+)""")
        private val AMOUNT_BILL   = Regex("""[Bb]ill payment of PKR\s+([\d,]+)""")

        private val BALANCE       = Regex("""[Aa]vailable [Bb]alance[:\s]+PKR\s*([\d,]+)""")
        private val BALANCE_ALT   = Regex("""[Bb]al[:\s]+PKR\s*([\d,]+)""")
        private val IBFT_TO       = Regex("""[Ii]bft of PKR[\d,\s]+to ([A-Za-z ]+?)(?:\s*\(|\.|\s{2}|$)""")
        private val REF_NO        = Regex("""(?:Ref|TRN|Stan)[.#:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase().replace("-", "").replace(" ", "") in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE.firstGroup(body) ?: BALANCE_ALT.firstGroup(body))?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Meezan Bank", type = TransactionType.DEBIT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        AMOUNT_CREDIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Meezan Bank", type = TransactionType.CREDIT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        AMOUNT_IBFT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Meezan Bank", type = TransactionType.TRANSFER,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = IBFT_TO.firstGroup(body)?.trimEnd(), referenceNumber = ref,
            )
        }

        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Meezan Bank", type = TransactionType.BILL_PAYMENT,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        return buildTransaction(
            sender = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution = "Meezan Bank", type = TransactionType.UNKNOWN,
            amount = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
