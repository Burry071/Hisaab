package com.hisaab.parser.bank

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.model.IngestionSource
import java.math.BigDecimal
import java.util.UUID

/**
 * Allied Bank (ABL) SMS parser.
 * Sender IDs: ABL, ALLIEDBANK
 *
 * Sample formats:
 *   ABL: PKR 12,000 debited from your account. Ref: ABL12345. Balance: PKR 45,000.
 *   ABL: PKR 5,000 credited to your account ending 1234. Ref: ABL98765.
 */
object AlliedBankParser : BankParser {

    private val DEBIT_REGEX  = Regex("""PKR\s*([\d,]+(?:\.\d{2})?)\s+debited""", RegexOption.IGNORE_CASE)
    private val CREDIT_REGEX = Regex("""PKR\s*([\d,]+(?:\.\d{2})?)\s+credited""", RegexOption.IGNORE_CASE)
    private val BAL_REGEX    = Regex("""[Bb]alance[:\s]+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val REF_REGEX    = Regex("""[Rr]ef[:\s]+(\w+)""")

    override fun canParse(sender: String, body: String): Boolean =
        sender.equals("ABL", ignoreCase = true) ||
        sender.contains("ALLIEDBANK", ignoreCase = true)

    override fun parse(sender: String, smsBody: String, timestampEpochMs: Long): ParsedTransaction {
        val timestamp = timestampEpochMs
        val debitMatch  = DEBIT_REGEX.find(smsBody)
        val creditMatch = CREDIT_REGEX.find(smsBody)
        val match       = debitMatch ?: creditMatch ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Allied Bank", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )

        val amount  = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Allied Bank", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )
        val type    = if (debitMatch != null) TransactionType.DEBIT else TransactionType.CREDIT
        val balance = BAL_REGEX.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toBigDecimalOrNull()
        val ref     = REF_REGEX.find(smsBody)?.groupValues?.get(1) ?: UUID.randomUUID().toString().take(10)

        return ParsedTransaction(
            id               = UUID.randomUUID().toString(),
            amount           = amount,
            type             = type,
            institution      = "Allied Bank",
            counterparty     = null,
            referenceNumber  = ref,
            balanceAfter     = balance,
            timestampEpochMs = timestamp,
            source           = IngestionSource.SMS,
            confidenceScore  = 0.88f,
            rawSmsBody       = smsBody,
        )
    }
}

/**
 * National Bank of Pakistan (NBP) SMS parser.
 * Sender IDs: NBP, NBP-ALERT
 *
 * Sample formats:
 *   NBP-ALERT: Debit of PKR 3,500.00 from Account **4321. Bal: PKR 22,000.00
 *   NBP-ALERT: Credit of PKR 50,000.00 in Account **4321. Transaction ID: NBP99887.
 */
object NBPParser : BankParser {

    private val DEBIT_REGEX  = Regex("""[Dd]ebit\s+of\s+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val CREDIT_REGEX = Regex("""[Cc]redit\s+of\s+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val BAL_REGEX    = Regex("""[Bb]al[:\s]+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val TXN_REGEX    = Regex("""Transaction\s+ID[:\s]+(\w+)""", RegexOption.IGNORE_CASE)

    override fun canParse(sender: String, body: String): Boolean =
        sender.equals("NBP", ignoreCase = true) ||
        sender.contains("NBP", ignoreCase = true)

    override fun parse(sender: String, smsBody: String, timestampEpochMs: Long): ParsedTransaction {
        val timestamp = timestampEpochMs
        val debitMatch  = DEBIT_REGEX.find(smsBody)
        val creditMatch = CREDIT_REGEX.find(smsBody)
        val match       = debitMatch ?: creditMatch ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "National Bank of Pakistan", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )

        val amount  = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "NBP", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )
        val type    = if (debitMatch != null) TransactionType.DEBIT else TransactionType.CREDIT
        val balance = BAL_REGEX.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toBigDecimalOrNull()
        val ref     = TXN_REGEX.find(smsBody)?.groupValues?.get(1) ?: UUID.randomUUID().toString().take(10)

        return ParsedTransaction(
            id               = UUID.randomUUID().toString(),
            amount           = amount,
            type             = type,
            institution      = "National Bank of Pakistan",
            counterparty     = null,
            referenceNumber  = ref,
            balanceAfter     = balance,
            timestampEpochMs = timestamp,
            source           = IngestionSource.SMS,
            confidenceScore  = 0.87f,
            rawSmsBody       = smsBody,
        )
    }
}

/**
 * Habib Metropolitan Bank (HMB) SMS parser.
 * Sender IDs: HMB, HABIBMETRO
 *
 * Sample formats:
 *   HMB: Your account has been debited by Rs.8,000. Available balance Rs.56,000.
 *   HMB: Your account has been credited by Rs.20,000. Ref: HMB202603.
 */
object HabibMetroParser : BankParser {

    private val DEBIT_REGEX  = Regex("""debited\s+by\s+Rs[.\s]*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    private val CREDIT_REGEX = Regex("""credited\s+by\s+Rs[.\s]*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    private val BAL_REGEX    = Regex("""balance\s+Rs[.\s]*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    private val REF_REGEX    = Regex("""[Rr]ef[:\s]+(\w+)""")

    override fun canParse(sender: String, body: String): Boolean =
        sender.equals("HMB", ignoreCase = true) ||
        sender.contains("HABIBMETRO", ignoreCase = true)

    override fun parse(sender: String, smsBody: String, timestampEpochMs: Long): ParsedTransaction {
        val timestamp = timestampEpochMs
        val debitMatch  = DEBIT_REGEX.find(smsBody)
        val creditMatch = CREDIT_REGEX.find(smsBody)
        val match       = debitMatch ?: creditMatch ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Habib Metropolitan Bank", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )

        val amount  = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Habib Metro", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )
        val type    = if (debitMatch != null) TransactionType.DEBIT else TransactionType.CREDIT
        val balance = BAL_REGEX.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toBigDecimalOrNull()
        val ref     = REF_REGEX.find(smsBody)?.groupValues?.get(1) ?: UUID.randomUUID().toString().take(10)

        return ParsedTransaction(
            id               = UUID.randomUUID().toString(),
            amount           = amount,
            type             = type,
            institution      = "Habib Metropolitan Bank",
            counterparty     = null,
            referenceNumber  = ref,
            balanceAfter     = balance,
            timestampEpochMs = timestamp,
            source           = IngestionSource.SMS,
            confidenceScore  = 0.87f,
            rawSmsBody       = smsBody,
        )
    }
}

/**
 * Bank of Punjab (BOP) SMS parser.
 * Sender IDs: BOP, BOPUNJAB
 *
 * Sample formats:
 *   BOP: PKR 6,000.00 has been debited from your BOP account 1234. Balance: PKR 34,000.00
 *   BOP: PKR 15,000.00 has been credited to your BOP account 1234. Ref No: BOP45678.
 */
object BankOfPunjabParser : BankParser {

    private val DEBIT_REGEX  = Regex("""PKR\s*([\d,]+(?:\.\d{2})?)\s+has been debited""", RegexOption.IGNORE_CASE)
    private val CREDIT_REGEX = Regex("""PKR\s*([\d,]+(?:\.\d{2})?)\s+has been credited""", RegexOption.IGNORE_CASE)
    private val BAL_REGEX    = Regex("""[Bb]alance[:\s]+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val REF_REGEX    = Regex("""Ref\s+No[:\s]+(\w+)""", RegexOption.IGNORE_CASE)

    override fun canParse(sender: String, body: String): Boolean =
        sender.equals("BOP", ignoreCase = true) ||
        sender.contains("BOPUNJAB", ignoreCase = true)

    override fun parse(sender: String, smsBody: String, timestampEpochMs: Long): ParsedTransaction {
        val timestamp = timestampEpochMs
        val debitMatch  = DEBIT_REGEX.find(smsBody)
        val creditMatch = CREDIT_REGEX.find(smsBody)
        val match       = debitMatch ?: creditMatch ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Bank of Punjab", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )

        val amount  = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Bank of Punjab", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )
        val type    = if (debitMatch != null) TransactionType.DEBIT else TransactionType.CREDIT
        val balance = BAL_REGEX.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toBigDecimalOrNull()
        val ref     = REF_REGEX.find(smsBody)?.groupValues?.get(1) ?: UUID.randomUUID().toString().take(10)

        return ParsedTransaction(
            id               = UUID.randomUUID().toString(),
            amount           = amount,
            type             = type,
            institution      = "Bank of Punjab",
            counterparty     = null,
            referenceNumber  = ref,
            balanceAfter     = balance,
            timestampEpochMs = timestamp,
            source           = IngestionSource.SMS,
            confidenceScore  = 0.88f,
            rawSmsBody       = smsBody,
        )
    }
}

/**
 * Askari Bank SMS parser.
 * Sender IDs: ASKARI, ASKARIBANK
 *
 * Sample formats:
 *   ASKARI: Debit transaction of PKR 4,500 processed. A/C No. XXXX1234. Bal: PKR 67,000.
 *   ASKARI: Credit transaction of PKR 10,000 received. Ref: ASK7890.
 */
object AskariParser : BankParser {

    private val DEBIT_REGEX  = Regex("""[Dd]ebit\s+transaction\s+of\s+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val CREDIT_REGEX = Regex("""[Cc]redit\s+transaction\s+of\s+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val BAL_REGEX    = Regex("""[Bb]al[:\s]+PKR\s*([\d,]+(?:\.\d{2})?)""")
    private val REF_REGEX    = Regex("""[Rr]ef[:\s]+(\w+)""")

    override fun canParse(sender: String, body: String): Boolean =
        sender.equals("ASKARI", ignoreCase = true) ||
        sender.contains("ASKARIBANK", ignoreCase = true)

    override fun parse(sender: String, smsBody: String, timestampEpochMs: Long): ParsedTransaction {
        val timestamp = timestampEpochMs
        val debitMatch  = DEBIT_REGEX.find(smsBody)
        val creditMatch = CREDIT_REGEX.find(smsBody)
        val match       = debitMatch ?: creditMatch ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Askari Bank", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )

        val amount  = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return ParsedTransaction(
            id = UUID.randomUUID().toString(), amount = BigDecimal.ZERO, type = TransactionType.UNKNOWN,
            institution = "Askari Bank", counterparty = null, referenceNumber = null, balanceAfter = null,
            timestampEpochMs = timestampEpochMs, source = IngestionSource.SMS, confidenceScore = 0.3f, rawSmsBody = smsBody
        )
        val type    = if (debitMatch != null) TransactionType.DEBIT else TransactionType.CREDIT
        val balance = BAL_REGEX.find(smsBody)?.groupValues?.get(1)?.replace(",", "")?.toBigDecimalOrNull()
        val ref     = REF_REGEX.find(smsBody)?.groupValues?.get(1) ?: UUID.randomUUID().toString().take(10)

        return ParsedTransaction(
            id               = UUID.randomUUID().toString(),
            amount           = amount,
            type             = type,
            institution      = "Askari Bank",
            counterparty     = null,
            referenceNumber  = ref,
            balanceAfter     = balance,
            timestampEpochMs = timestamp,
            source           = IngestionSource.SMS,
            confidenceScore  = 0.87f,
            rawSmsBody       = smsBody,
        )
    }
}
