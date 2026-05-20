package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from HBL (Habib Bank Limited).
 * Sender IDs: "HBL", "HBLBANK", "HBL-BANK"
 *
 * Supported formats:
 *  DEBIT       → "Rs.<amount> has been debited from your HBL account ..."
 *  CREDIT      → "Rs.<amount> has been credited to your HBL account ..."
 *  TRANSFER    → "Amount Rs.<amount> transferred to ..."
 *  BILL_PAYMENT→ "Bill payment of Rs.<amount> ..."
 *  ATM         → "Withdrawal of Rs.<amount> at ATM ..."
 */
class HBLParser : BankParser {

    companion object {
        private val SENDERS = setOf("hbl", "hblbank", "hbl-bank")

        private val AMOUNT_DEBIT    = Regex("""Rs\.([\d,]+)\s+has been debited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT   = Regex("""Rs\.([\d,]+)\s+has been credited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_TRANSFER = Regex("""Amount\s+Rs\.([\d,]+)\s+transferred""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""[Bb]ill payment of Rs\.([\d,]+)""")
        private val AMOUNT_ATM      = Regex("""[Ww]ithdrawal of Rs\.([\d,]+)\s+at ATM""")

        private val BALANCE         = Regex("""[Aa]vailable [Bb]alance[:\s]+Rs\.([\d,]+)""")
        private val BALANCE_ALT     = Regex("""[Bb]alance[:\s]+Rs\.([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""transferred to ([A-Za-z ]+?)(?:\s*\(|\.|\s{2}|$)""", RegexOption.IGNORE_CASE)
        private val REF_NO          = Regex("""(?:Ref|TXN|Trn)[.#:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
        private val ACCOUNT_LAST4   = Regex("""account\s+(?:ending\s+)?(?:with\s+)?(?:No\.?\s+)?[Xx*]+(\d{4})""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase().replace("-", "") in setOf("hbl", "hblbank")

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE.firstGroup(body) ?: BALANCE_ALT.firstGroup(body))?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        // DEBIT
        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "HBL",
                type             = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        // CREDIT
        AMOUNT_CREDIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "HBL",
                type             = TransactionType.CREDIT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        // TRANSFER
        AMOUNT_TRANSFER.firstGroup(body)?.let { rawAmt ->
            val counterparty = COUNTERPARTY_TO.firstGroup(body)?.trimEnd()
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "HBL",
                type             = TransactionType.TRANSFER,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                counterparty     = counterparty,
                referenceNumber  = ref,
            )
        }

        // BILL_PAYMENT
        AMOUNT_BILL.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "HBL",
                type             = TransactionType.BILL_PAYMENT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        // ATM WITHDRAWAL
        AMOUNT_ATM.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "HBL",
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
            institution      = "HBL",
            type             = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO,
            confidenceScore  = 0.3f,
        )
    }
}
