package com.hisaab.data.demo

import com.hisaab.domain.model.AgentName
import com.hisaab.domain.model.ForecastType
import com.hisaab.domain.model.Insight
import com.hisaab.domain.model.InsightCategory
import com.hisaab.domain.model.InsightLevel
import com.hisaab.parser.model.IngestionSource
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId

/**
 * DemoModeManager — hackathon reliability layer.
 *
 * The live demo must NEVER depend on the judge's phone having:
 *   • Real Pakistani bank SMS
 *   • Active internet for Gemini API
 *   • SmsReaderWorker permissions granted
 *
 * This class seeds synthetic data that looks real, covers all 6 institutions,
 * and exercises all 3 agent pathways (ingestion, contradiction, insight).
 *
 * Usage (in DemoActivity or MainActivity toggle):
 *   val txns = DemoModeManager.getSeedTransactions()
 *   agentViewModel.runPipeline(incoming = txns.takeLast(5), existing = txns.dropLast(5))
 */
object DemoModeManager {

    // ── Synthetic transactions covering last 3 months ────────────────────────

    fun getSeedTransactions(): List<ParsedTransaction> {
        val now = LocalDate.now(ZoneId.of("Asia/Karachi"))

        return buildList {
            // Month -3: baseline
            add(tx("HBL",       "Salary",         TransactionType.CREDIT,     BigDecimal("85000"),  now.minusMonths(3).withDayOfMonth(27)))
            add(tx("HBL",       "LESCO Bill",     TransactionType.BILL_PAYMENT, BigDecimal("3200"), now.minusMonths(3).withDayOfMonth(5)))
            add(tx("JazzCash",  "Mehran Hotel",   TransactionType.DEBIT,      BigDecimal("2400"),   now.minusMonths(3).withDayOfMonth(8)))
            add(tx("JazzCash",  "Careem",         TransactionType.DEBIT,      BigDecimal("850"),    now.minusMonths(3).withDayOfMonth(10)))
            add(tx("Easypaisa", "Netflix",        TransactionType.DEBIT,      BigDecimal("1200"),   now.minusMonths(3).withDayOfMonth(15)))
            add(tx("MCB",       "Groceries",      TransactionType.DEBIT,      BigDecimal("8900"),   now.minusMonths(3).withDayOfMonth(18)))
            add(tx("HBL",       "Fuel",           TransactionType.DEBIT,      BigDecimal("5000"),   now.minusMonths(3).withDayOfMonth(22)))

            // Month -2: slight increase
            add(tx("HBL",       "Salary",         TransactionType.CREDIT,     BigDecimal("85000"),  now.minusMonths(2).withDayOfMonth(27)))
            add(tx("HBL",       "LESCO Bill",     TransactionType.BILL_PAYMENT, BigDecimal("3400"), now.minusMonths(2).withDayOfMonth(5)))
            add(tx("JazzCash",  "Mehran Hotel",   TransactionType.DEBIT,      BigDecimal("3100"),   now.minusMonths(2).withDayOfMonth(7)))
            add(tx("JazzCash",  "McDonald's",     TransactionType.DEBIT,      BigDecimal("1900"),   now.minusMonths(2).withDayOfMonth(12)))
            add(tx("Easypaisa", "Netflix",        TransactionType.DEBIT,      BigDecimal("1200"),   now.minusMonths(2).withDayOfMonth(15)))
            add(tx("MCB",       "Groceries",      TransactionType.DEBIT,      BigDecimal("9500"),   now.minusMonths(2).withDayOfMonth(19)))
            add(tx("NayaPay",   "Uber",           TransactionType.DEBIT,      BigDecimal("1200"),   now.minusMonths(2).withDayOfMonth(21)))
            add(tx("HBL",       "Fuel",           TransactionType.DEBIT,      BigDecimal("5000"),   now.minusMonths(2).withDayOfMonth(23)))

            // Month -1: noticeably above avg
            add(tx("HBL",       "Salary",         TransactionType.CREDIT,     BigDecimal("85000"),  now.minusMonths(1).withDayOfMonth(27)))
            add(tx("HBL",       "LESCO Bill",     TransactionType.BILL_PAYMENT, BigDecimal("3600"), now.minusMonths(1).withDayOfMonth(4)))
            add(tx("JazzCash",  "Mehran Hotel",   TransactionType.DEBIT,      BigDecimal("4200"),   now.minusMonths(1).withDayOfMonth(6)))
            add(tx("JazzCash",  "Burger House",   TransactionType.DEBIT,      BigDecimal("2100"),   now.minusMonths(1).withDayOfMonth(9)))
            add(tx("JazzCash",  "Pizza Point",    TransactionType.DEBIT,      BigDecimal("2800"),   now.minusMonths(1).withDayOfMonth(14)))
            add(tx("Easypaisa", "Netflix",        TransactionType.DEBIT,      BigDecimal("1200"),   now.minusMonths(1).withDayOfMonth(15)))
            add(tx("MCB",       "Groceries",      TransactionType.DEBIT,      BigDecimal("11200"),  now.minusMonths(1).withDayOfMonth(17)))
            add(tx("NayaPay",   "Careem",         TransactionType.DEBIT,      BigDecimal("1800"),   now.minusMonths(1).withDayOfMonth(20)))
            add(tx("HBL",       "Fuel",           TransactionType.DEBIT,      BigDecimal("7500"),   now.minusMonths(1).withDayOfMonth(24)))

            // Current month: highest food spend yet — drives L3 insight
            add(tx("HBL",       "Salary",         TransactionType.CREDIT,     BigDecimal("85000"),  now.withDayOfMonth(if (now.dayOfMonth > 27) 27 else now.dayOfMonth.coerceAtMost(27))))
            add(tx("JazzCash",  "Mehran Hotel",   TransactionType.DEBIT,      BigDecimal("5500"),   now.minusDays(12)))
            add(tx("JazzCash",  "Burger House",   TransactionType.DEBIT,      BigDecimal("2900"),   now.minusDays(9)))
            add(tx("JazzCash",  "KFC",            TransactionType.DEBIT,      BigDecimal("3200"),   now.minusDays(7)))
            add(tx("JazzCash",  "Pizza Point",    TransactionType.DEBIT,      BigDecimal("2800"),   now.minusDays(5)))
            add(tx("NayaPay",   "Careem",         TransactionType.DEBIT,      BigDecimal("2200"),   now.minusDays(4)))
            add(tx("Easypaisa", "Netflix",        TransactionType.DEBIT,      BigDecimal("1200"),   now.minusDays(3)))
            add(tx("HBL",       "LESCO Bill",     TransactionType.BILL_PAYMENT, BigDecimal("3800"), now.minusDays(2)))

            // Conflict seed — same transaction from HBL + JazzCash (cross-source conflict demo)
            add(tx("HBL",      "Careem ride",     TransactionType.DEBIT,      BigDecimal("1800"),   now.minusDays(1), forceTs = now.minusDays(1).toEpochDay() * 86_400_000 + 3600_000))
            add(tx("JazzCash", "Careem ride",     TransactionType.DEBIT,      BigDecimal("1950"),   now.minusDays(1), forceTs = now.minusDays(1).toEpochDay() * 86_400_000 + 3620_000))
        }
    }

    /** Returns the synthetic insights the demo should show (pre-canned for offline fallback). */
    fun getFallbackInsights(): List<Insight> = listOf(
        Insight(
            id            = "demo-insight-food-l3",
            level         = InsightLevel.LEVEL_3,
            category      = InsightCategory.FOOD,
            headline      = "Food spending 39% above your 3-month average",
            reasoning     = "3-month avg: PKR 13,200\n" +
                "This month so far: PKR 18,400 (+39%)\n" +
                "Consecutive months above avg: 3\n" +
                "Current daily rate: PKR 1,380\n" +
                "Projected month total: PKR 24,840\n" +
                "Days remaining: 18",
            actionPrompt  = "Cut PKR 300/day on food to close the gap by month-end",
            confidence    = 0.88f,
            generatedAtMs = System.currentTimeMillis(),
        ),
        Insight(
            id            = "demo-insight-transport-l2",
            level         = InsightLevel.LEVEL_2,
            category      = InsightCategory.TRANSPORT,
            headline      = "Transport 22% above your 3-month average",
            reasoning     = "3-month avg: PKR 3,600\n" +
                "This month: PKR 4,400 (+22%)\n" +
                "Trend: RISING",
            actionPrompt  = "Review your Careem/Uber usage this month",
            confidence    = 0.82f,
            generatedAtMs = System.currentTimeMillis(),
        ),
    )

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun tx(
        institution: String,
        counterparty: String,
        type: TransactionType,
        amount: BigDecimal,
        date: LocalDate,
        forceTs: Long? = null,
    ): ParsedTransaction {
        val ts   = forceTs ?: (date.toEpochDay() * 86_400_000 + 43_200_000) // noon UTC
        val body = "$institution: $counterparty ${type.name} PKR $amount"
        return ParsedTransaction(
            id               = deterministicId(institution, body, ts),
            source           = IngestionSource.SMS,
            institution      = institution,
            type             = type,
            amount           = amount,
            currency         = "PKR",
            balanceAfter     = null,
            counterparty     = counterparty,
            referenceNumber  = "DEMO-${date.dayOfMonth}${institution.take(3)}",
            rawSmsBody       = body,
            timestampEpochMs = ts,
            confidenceScore  = 0.95f,
        )
    }

    private fun deterministicId(sender: String, body: String, ts: Long): String {
        val input = "$sender|$body|$ts"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
