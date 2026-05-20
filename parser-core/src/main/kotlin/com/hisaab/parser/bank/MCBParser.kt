package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from MCB (Muslim Commercial Bank).
 * Sender IDs: "MCB", "MCBBANK", "MCB-BANK"
 *
 * Supported formats:
 *  DEBIT    → "Your A/C ... debited with Rs.<amount> ..."
 *  CREDIT   → "Your A/C ... credited with Rs.<amount> ..."
 *  TRANSFER → "Transfer of Rs.<amount> made to ..."
 *  BILL     → "MCB bill payment Rs.<amount> ..."
 *  ATM      → "ATM Cash Withdrawal Rs.<amount> ..."
 */
class MCBParser : BankParser {

    companion object {
        private val SENDERS = setOf("mcb", "mcbbank", "mcb-bank")

        private val AMOUNT_DEBIT    = Regex("""debited with Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT   = Regex("""credited with Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_TRANSFER = Regex("""Transfer of Rs\.([\d,]+)\s+made""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""[Bb]ill payment\s+Rs\.([\d,]+)""")
        private val AMOUNT_ATM      = Regex("""ATM Cash Withdrawal\s+Rs\.([\d,]+)""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""[Aa]vailable [Bb]alance[:\s]+Rs\.([\d,]+)""")
        private val BALANCE_ALT     = Regex("""[Bb]al[:\s]+Rs\.([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""made to ([A-Za-z ]+?)(?:\s*\(|\.|\s{2}|$)""", RegexOption.IGNORE_CASE)
        private val REF_NO          = Regex("""(?:Ref|Txn|TRN)[.#:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase().replace("-", "") in setOf("mcb", "mcbbank")

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE.firstGroup(body) ?: BALANCE_ALT.firstGroup(body))?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "MCB",
                type             = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        AMOUNT_CREDIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "MCB",
                type             = TransactionType.CREDIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        AMOUNT_TRANSFER.firstGroup(body)?.let { rawAmt ->
            val counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd()
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "MCB",
                type             = TransactionType.TRANSFER,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = counterparty,
                referenceNumber  = ref,
            )
        }

        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "MCB",
                type             = TransactionType.BILL_PAYMENT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        AMOUNT_ATM.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "MCB",
                type             = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = "ATM",
                referenceNumber  = ref,
            )
        }

        return buildTransaction(
            sender           = sender,
            body             = body,
            timestampEpochMs = timestampEpochMs,
            institution      = "MCB",
            type             = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO,
            confidenceScore  = 0.3f,
        )
    }
}
