package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.wallet.ParserUtils.buildTransaction
import com.hisaab.parser.wallet.ParserUtils.firstGroup
import com.hisaab.parser.wallet.ParserUtils.parseAmount
import java.math.BigDecimal

/**
 * Parses SMS from SadaPay (sender: "SadaPay" or "SADAPAY").
 *
 * Supported formats:
 *  DEBIT    → "You sent PKR <amount> to <name> ..."
 *  CREDIT   → "PKR <amount> received from <name> ..."
 *  PURCHASE → "Purchase of PKR <amount> at <merchant> ..."
 *  TOP_UP   → "... card has been loaded with PKR <amount> ..."
 */
class SadaPayParser : WalletParser {

    companion object {
        private val SENDERS = setOf("sadapay")

        private val AMOUNT_SENT     = Regex("""You sent PKR ([\d,]+) to""", RegexOption.IGNORE_CASE)
        private val AMOUNT_RECEIVED = Regex("""PKR ([\d,]+) received from""", RegexOption.IGNORE_CASE)
        private val AMOUNT_PURCHASE = Regex("""Purchase of PKR ([\d,]+) at""", RegexOption.IGNORE_CASE)
        private val AMOUNT_LOADED   = Regex("""loaded with PKR ([\d,]+)""", RegexOption.IGNORE_CASE)

        private val BALANCE         = Regex("""(?:SadaPay balance is|SadaPay balance:|balance:)\s*PKR\s*([\d,]+)""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_TO = Regex("""sent PKR [\d,]+ to ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val COUNTERPARTY_FROM = Regex("""received from ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val MERCHANT        = Regex("""Purchase of PKR [\d,]+ at ([A-Za-z ]+?)\s*[.]""", RegexOption.IGNORE_CASE)
        private val SP_ID           = Regex("""ID:\s*([\w-]+)""", RegexOption.IGNORE_CASE)
    }

    override fun canParse(sender: String, body: String): Boolean =
        sender.lowercase() in SENDERS

    override fun parse(sender: String, body: String, timestampEpochMs: Long): ParsedTransaction {
        val balance = BALANCE.firstGroup(body)?.let { parseAmount(it) }
        val ref     = SP_ID.firstGroup(body)

        AMOUNT_SENT.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution      = "SadaPay", type = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt), balanceAfter = balance,
                counterparty     = COUNTERPARTY_TO.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        AMOUNT_RECEIVED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution      = "SadaPay", type = TransactionType.CREDIT,
                amount           = parseAmount(rawAmt), balanceAfter = balance,
                counterparty     = COUNTERPARTY_FROM.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        AMOUNT_PURCHASE.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution      = "SadaPay", type = TransactionType.DEBIT,
                amount           = parseAmount(rawAmt), balanceAfter = balance,
                counterparty     = MERCHANT.firstGroup(body)?.trimEnd(),
                referenceNumber  = ref,
            )
        }

        AMOUNT_LOADED.firstGroup(body)?.let { rawAmt ->
            return buildTransaction(
                sender           = sender, body = body, timestampEpochMs = timestampEpochMs,
                institution      = "SadaPay", type = TransactionType.TOP_UP,
                amount           = parseAmount(rawAmt), balanceAfter = balance,
                referenceNumber  = ref,
            )
        }

        return buildTransaction(
            sender           = sender, body = body, timestampEpochMs = timestampEpochMs,
            institution      = "SadaPay", type = TransactionType.UNKNOWN,
            amount           = BigDecimal.ZERO, confidenceScore = 0.3f,
        )
    }
}
