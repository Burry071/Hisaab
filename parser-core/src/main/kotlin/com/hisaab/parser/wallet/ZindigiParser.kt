package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from Zindigi (sender: "Zindigi" or "ZINDIGI").
 * Zindigi is a separate wallet app distinct from JazzCash
 * despite both being under the Jazz umbrella.
 *
 * Supported formats:
 *  DEBIT    → "PKR <amount> debited ... Transferred to <name> ..."
 *  CREDIT   → "PKR <amount> credited ... from <name> ..."
 *  PURCHASE → "PKR <amount> debited for purchase at <merchant> ..."
 *  TOP_UP   → "PKR <amount> added to your Zindigi wallet ..."
 */
class ZindigiParser : WalletParser {

    companion object {
        private val SENDERS = setOf("zindigi")

        private val AMOUNT_DEBIT    = Regex("""PKR ([\d,]+) debited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_CREDIT   = Regex("""PKR ([\d,]+) credited""", RegexOption.IGNORE_CASE)
        private val AMOUNT_ADDED    = Regex("""PKR ([\d,]+) added to your Zindigi""", RegexOption.IGNORE_CASE)

        private val IS_PURCHASE     = Regex("""debited for purchase at""", RegexOption.IGNORE_CASE)
        private val IS_TRANSFER     = Regex("""Transferred to""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""[Bb]alance:\s*PKR\s*([\d,]+)""")
        private val COUNTERPARTY_TO = Regex("""Transferred to ([A-Za-z ]+?)\s*(?:\(|\.)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM = Regex("""credited.*?from ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val MERCHANT        = Regex("""purchase at ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val ZND_ID          = Regex("""ID:\s*([\w-]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = ZND_ID.firstGroup(body)

        // DEBIT — check purchase vs transfer
        AMOUNT_DEBIT.firstGroup(body)?.let { rawAmt ->
            val isPurchase = IS_PURCHASE.containsMatchIn(body)
            val counterparty = if (isPurchase) MERCHANT.firstGroup(body)?.trimEnd()
                               else            COUNTERPARTY_TO.firstGroup(body)?.trimEnd()
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Zindigi", type = TransactionType.DEBIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = counterparty, referenceNumber = ref,
            )
        }

        // CREDIT
        AMOUNT_CREDIT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Zindigi", type = TransactionType.CREDIT,
                amount = parseAmount(rawAmt), balanceAfter = balance,
                counterparty = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(),
                referenceNumber = ref,
            )
        }

        // TOP_UP
        AMOUNT_ADDED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution = "Zindigi", type = TransactionType.TOP_UP,
                amount = parseAmount(rawAmt), balanceAfter = balance, referenceNumber = ref,
            )
        }

        return buildTransaction(
            sender = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution = "Zindigi", type = TransactionType.UNKNOWN,
            amount = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
