package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from UBL (United Bank Limited).
 * Sender IDs: "UBL", "UBLBANK", "UBLDIRECT"
 *
 * Supported formats:
 *  DEBIT    → "PKR <amount> debited from A/C ..."
 *  CREDIT   → "PKR <amount> credited to your A/C ..."
 *  TRANSFER → "Fund Transfer of PKR <amount> to ..."
 *  BILL     → "Utility bill payment of PKR <amount> ..."
 */
class UBLParser : BankParser {

    companion object {
        private val SENDERS = setOf("ubl", "ublbank", "ubldirect")

        private val AMOUNT_DEBIT    = Regex("""PKR\s+([\d,]+)\s+debited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT   = Regex("""PKR\s+([\d,]+)\s+credited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_TRANSFER = Regex("""Fund Transfer of PKR\s+([\d,]+)""", RegexOption.IGNORE_CASE)
        private val AMOUNT_BILL     = Regex("""[Uu]tility bill payment of PKR\s+([\d,]+)""")

        private val BALANCE         = Regex("""[Aa]vailable [Bb]al(?:ance)?[:\s]+PKR\s*([\d,]+)""")
        private val BALANCE_ALT     = Regex("""[Bb]al[:\s]+PKR\s*([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""to ([A-Za-z ]+?)(?:\s*A/C|\s*Acc|\.|$)""", RegexOption.IGNORE_CASE)
        private val REF_NO          = Regex("""(?:Ref|TRN|Txn)[.#:\s]*([\w]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase().replace("-", "") in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = (BALANCE.firstGroup(body) ?: BALANCE_ALT.firstGroup(body))?.let { parseAmount(it) }
        val ref     = REF_NO.firstGroup(body)

        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender,
                body             = body,
                timestampEpochMs = timestampEpochMs,
                institution      = "UBL",
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
                institution      = "UBL",
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
                institution      = "UBL",
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
                institution      = "UBL",
                type             = TransactionType.BILL_PAYMENT,
                amount           = parseAmount(rawAmt),
                balanceAfter     = balance,
                referenceNumber  = ref,
            )
        }

        return buildTransaction(
            sender           = sender,
            body             = body,
            timestampEpochMs = timestampEpochMs,
            institution      = "UBL",
            type             = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO,
            confidenceScore  = 0.3f,
        )
    }
}
