package com.hisaab.data.demo

import com.hisaab.data.local.TransactionDao
import com.hisaab.data.local.TransactionEntity
import com.hisaab.parser.model.IngestionSource
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class DemoDataSeeder @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun seedIfEmpty() {
        if (transactionDao.countTransactions() == 0) {
            transactionDao.insertAll(getPakistaniMockEntities())
        }
    }

    /**
     * Generates a robust list of 10 Pakistani-localized transactions as ParsedTransactions
     * for direct injection into the Agent Pipeline.
     */
    fun getPakistaniMockTransactions(): List<ParsedTransaction> {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        return listOf(
            // 1. Salary from HBL
            mockTx(
                institution = "HBL",
                type = TransactionType.CREDIT,
                amount = "125000",
                counterparty = "AISeekho Solutions",
                body = "Your account has been credited with PKR 125,000.00 (Salary May 2026)",
                ts = now - (15 * dayMs)
            ),
            // 2. Freelance from Meezan
            mockTx(
                institution = "Meezan Bank",
                type = TransactionType.CREDIT,
                amount = "45000",
                counterparty = "Upwork / Freelance",
                body = "Inward Clearing: PKR 45,000.00 from Upwork Global",
                ts = now - (10 * dayMs)
            ),
            // 3. JazzCash Transfer (Conflict Part A)
            mockTx(
                institution = "JazzCash",
                type = TransactionType.TRANSFER,
                amount = "5000",
                counterparty = "Meezan Bank",
                body = "Paid PKR 5,000 to Meezan A/C 0123. Ref: 887210. Fee: 0.0",
                ts = now - (2 * dayMs)
            ),
            // 4. JazzCash Transfer (Conflict Part B - Duplicate)
            mockTx(
                institution = "JazzCash",
                type = TransactionType.TRANSFER,
                amount = "5000",
                counterparty = "Meezan Bank",
                body = "Paid PKR 5,000 to Meezan A/C 0123. Ref: 887210. Fee: 0.0",
                ts = now - (2 * dayMs) + 1000 // 1 second apart
            ),
            // 5. Careem Ride
            mockTx(
                institution = "Easypaisa",
                type = TransactionType.DEBIT,
                amount = "850",
                counterparty = "Careem Pakistan",
                body = "Trx 992831: Paid PKR 850.0 to Careem for Ride",
                ts = now - (3 * dayMs)
            ),
            // 6. Netflix Subscription
            mockTx(
                institution = "Standard Chartered",
                type = TransactionType.DEBIT,
                amount = "1100",
                counterparty = "Netflix.com",
                body = "Online purchase of PKR 1,100.00 at Netflix using Card ending 1234",
                ts = now - (5 * dayMs)
            ),
            // 7. Food: Bundu Khan
            mockTx(
                institution = "Meezan Bank",
                type = TransactionType.DEBIT,
                amount = "4200",
                counterparty = "Bundu Khan Restaurant",
                body = "Paid PKR 4,200.00 at Bundu Khan Liberty via POS",
                ts = now - (4 * dayMs)
            ),
            // 8. Food: Foodpanda
            mockTx(
                institution = "JazzCash",
                type = TransactionType.DEBIT,
                amount = "1250",
                counterparty = "Foodpanda",
                body = "Payment of PKR 1,250.0 to Foodpanda successful",
                ts = now - (6 * dayMs)
            ),
            // 9. Utilities: LESCO Bill
            mockTx(
                institution = "Easypaisa",
                type = TransactionType.BILL_PAYMENT,
                amount = "8400",
                counterparty = "LESCO",
                body = "Bill payment of PKR 8,400 to LESCO successful. Ref: 14122341",
                ts = now - (12 * dayMs)
            ),
            // 10. PTCL Flash Fiber
            mockTx(
                institution = "Standard Chartered",
                type = TransactionType.BILL_PAYMENT,
                amount = "4500",
                counterparty = "PTCL",
                body = "Bill payment PKR 4,500.00 for PTCL Broadband",
                ts = now - (11 * dayMs)
            )
        )
    }

    private fun getPakistaniMockEntities(): List<TransactionEntity> {
        val now = LocalDate.now()
        fun dateMs(minusDays: Long) = now.minusDays(minusDays).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return listOf(
            TransactionEntity(merchantName = "HBL Salary Credit", amount = 125000, type = "INCOME", category = "Salary", timestampMs = dateMs(15)),
            TransactionEntity(merchantName = "Meezan Bank Credit", amount = 45000, type = "INCOME", category = "Transfer", timestampMs = dateMs(10)),
            TransactionEntity(merchantName = "JazzCash Transfer", amount = 5000, type = "TRANSFER", category = "Transfer", timestampMs = dateMs(2)),
            TransactionEntity(merchantName = "Careem Ride", amount = 850, type = "EXPENSE", category = "Transport", timestampMs = dateMs(3)),
            TransactionEntity(merchantName = "Netflix Subscription", amount = 1100, type = "EXPENSE", category = "General", timestampMs = dateMs(5)),
            TransactionEntity(merchantName = "Bundu Khan", amount = 4200, type = "EXPENSE", category = "Food", timestampMs = dateMs(4)),
            TransactionEntity(merchantName = "Foodpanda", amount = 1250, type = "EXPENSE", category = "Food", timestampMs = dateMs(6)),
            TransactionEntity(merchantName = "LESCO Bill", amount = 8400, type = "EXPENSE", category = "Utilities", timestampMs = dateMs(12)),
            TransactionEntity(merchantName = "PTCL Bill", amount = 4500, type = "EXPENSE", category = "Utilities", timestampMs = dateMs(11)),
            TransactionEntity(merchantName = "JazzCash Top-up", amount = 2000, type = "INCOME", category = "Top-up", timestampMs = dateMs(1))
        )
    }

    private fun mockTx(
        institution: String,
        type: TransactionType,
        amount: String,
        counterparty: String,
        body: String,
        ts: Long
    ) = ParsedTransaction(
        id = UUID.randomUUID().toString(),
        source = IngestionSource.SMS,
        institution = institution,
        type = type,
        amount = BigDecimal(amount),
        balanceAfter = null,
        counterparty = counterparty,
        referenceNumber = "REF${ts}",
        rawSmsBody = body,
        timestampEpochMs = ts,
        confidenceScore = 0.95f
    )
}
