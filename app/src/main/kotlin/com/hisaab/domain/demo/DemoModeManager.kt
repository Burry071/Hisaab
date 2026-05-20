package com.hisaab.domain.demo

import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * DemoModeManager — Hackathon Demo Seed (PRD Decision Log Grill Q8).
 *
 * Activates on long-press of the app logo in the home screen.
 * Injects 47 synthetic Pakistani SMS transactions that demonstrate:
 *   1. JazzCash + Meezan duplicate pair → ContradictionAgent resolves
 *   2. An amount conflict (HBL vs PDF)
 *   3. Food spending: 3 txns totaling PKR 18,400 (39% above avg)
 *   4. LESCO utility bill
 *   5. A level-3 insight ready to demonstrate
 *   6. A utility bill forecast
 *
 * NEVER ship to production. This class is for the demo only.
 */
object DemoModeManager {

    // ── Public entry point ─────────────────────────────────────────────────────

    fun buildDemoData(): DemoBundle {
        return DemoBundle(
            transactions  = buildDemoTransactions(),
            insights      = buildDemoInsights(),
            conflicts     = buildDemoConflicts(),
            forecasts     = buildDemoForecasts(),
            totalBalance  = BigDecimal("234580.00"),
            institutions  = listOf("HBL", "JazzCash", "Meezan", "Easypaisa"),
        )
    }

    // ── 47 synthetic transactions ──────────────────────────────────────────────

    private fun buildDemoTransactions(): List<ParsedTransaction> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        return listOf(
            // --- FOOD (3 txns totaling 18,400 PKR for the demo insight) ---
            tx("HBL",       "Mehran Hotel POS",          -850.0,   now - 2 * 3600_000, "FOOD"),
            tx("HBL",       "Savour Foods",              -3200.0,  now - 1 * day,      "FOOD"),
            tx("JazzCash",  "KFC POS Payment",           -14350.0, now - 3 * day,      "FOOD"),

            // --- UTILITIES ---
            tx("Easypaisa", "LESCO WAPDA Bill",          -3200.0,  now - 2 * day,      "UTILITY"),
            tx("HBL",       "SSGC Gas Bill",             -1800.0,  now - 5 * day,      "UTILITY"),

            // --- TRANSFER (duplicate pair for ContradictionAgent demo) ---
            tx("JazzCash",  "Sent to Meezan Bank",       -5000.0,  now - 3600_000,     "TRANSFER"),
            tx("Meezan",    "Received from JazzCash",     5000.0,  now - 3540_000,     "TRANSFER"),

            // --- AMOUNT CONFLICT PAIR (HBL SMS vs stored PDF value) ---
            tx("HBL",       "HBL ATM Withdrawal",        -10000.0, now - 4 * day,      "CASH"),
            tx("HBL",       "ATM Withdrawal (PDF)",      -10500.0, now - 4 * day,      "CASH"), // conflict

            // --- SALARY ---
            tx("HBL",       "Salary Credit",             150000.0, now - 10 * day,     "INCOME"),

            // --- TRANSPORT ---
            tx("JazzCash",  "Careem Ride",               -280.0,   now - 1 * 3600_000, "TRANSPORT"),
            tx("JazzCash",  "Careem Ride",               -350.0,   now - 2 * day,      "TRANSPORT"),
            tx("HBL",       "Parking Fee",               -200.0,   now - 3 * day,      "TRANSPORT"),

            // --- SHOPPING ---
            tx("HBL",       "Daraz POS Payment",         -4500.0,  now - 2 * day,      "SHOPPING"),
            tx("JazzCash",  "Gul Ahmed Garments",        -8200.0,  now - 5 * day,      "SHOPPING"),

            // --- SUBSCRIPTIONS ---
            tx("HBL",       "Netflix Monthly",           -900.0,   now - 3 * day,      "SUBSCRIPTION"),
            tx("JazzCash",  "Spotify Premium",           -350.0,   now - 3 * day,      "SUBSCRIPTION"),

            // --- P2P TRANSFERS ---
            tx("JazzCash",  "Sent to Ahmed",             -2000.0,  now - 1 * day,      "TRANSFER"),
            tx("Easypaisa", "Received from Family",       5000.0,  now - 4 * day,      "TRANSFER"),
            tx("JazzCash",  "UBL Wallet Top-Up",         -10000.0, now - 6 * day,      "TRANSFER"),

            // --- HEALTH ---
            tx("HBL",       "Shaukat Khanum OPD",        -3500.0,  now - 7 * day,      "HEALTH"),
            tx("JazzCash",  "Pharmacy D. Watson",        -1200.0,  now - 2 * day,      "HEALTH"),

            // --- ATMs / Cash ---
            tx("HBL",       "HBL ATM Cash Out",          -5000.0,  now - 8 * day,      "CASH"),
            tx("Meezan",    "Meezan ATM Withdrawal",     -8000.0,  now - 9 * day,      "CASH"),

            // --- EDUCATION ---
            tx("HBL",       "LUMS Fee Portal",           -45000.0, now - 12 * day,     "EDUCATION"),

            // --- MISCELLANEOUS fill-up to 47 total ---
            tx("JazzCash",  "Jazz Postpaid Bill",        -1200.0,  now - 3 * day,      "UTILITY"),
            tx("Easypaisa", "Telenor Postpaid",          -950.0,   now - 3 * day,      "UTILITY"),
            tx("HBL",       "Amazon Top-Up",             -3000.0,  now - 6 * day,      "SHOPPING"),
            tx("JazzCash",  "Meta Ads Payment",          -5000.0,  now - 4 * day,      "BUSINESS"),
            tx("HBL",       "Office Rent Transfer",      -25000.0, now - 15 * day,     "RENT"),
            tx("Meezan",    "Freelance Client Payment",  85000.0,  now - 8 * day,      "INCOME"),
            tx("JazzCash",  "Careem Food Delivery",      -650.0,   now - 1 * day,      "FOOD"),
            tx("HBL",       "PIA Flight Booking",        -18500.0, now - 11 * day,     "TRAVEL"),
            tx("JazzCash",  "Hotel Booking",             -7200.0,  now - 11 * day,     "TRAVEL"),
            tx("Easypaisa", "Petrol Station CNG",        -4200.0,  now - 2 * day,      "TRANSPORT"),
            tx("HBL",       "PTCL DSL Bill",             -2400.0,  now - 5 * day,      "UTILITY"),
            tx("JazzCash",  "Sent to Sister",            -3000.0,  now - 7 * day,      "TRANSFER"),
            tx("Meezan",    "Sadqah / Charity",          -1000.0,  now - 6 * day,      "OTHER"),
            tx("HBL",       "Tapal Danedar Grocery",     -2800.0,  now - 4 * day,      "FOOD"),
            tx("JazzCash",  "SUPARCO App Game",          -200.0,   now - 2 * day,      "ENTERTAINMENT"),
            tx("HBL",       "Books Bookshop",            -1500.0,  now - 9 * day,      "EDUCATION"),
            tx("JazzCash",  "Cinema Ticket IMAX",        -1800.0,  now - 5 * day,      "ENTERTAINMENT"),
            tx("Easypaisa", "KESC Electric Bill",        -2100.0,  now - 4 * day,      "UTILITY"),
            tx("HBL",       "MCB ATM Fee",               -50.0,    now - 1 * day,      "BANK_CHARGE"),
            tx("Meezan",    "Meezan Profit Credit",      4200.0,   now - 1 * day,      "INCOME"),
            tx("JazzCash",  "Daraz Daily Deal",          -750.0,   now - 1 * day,      "SHOPPING"),
            tx("HBL",       "Corporate Card Refund",     2500.0,   now - 3 * day,      "REFUND"),
        )
    }

    // ── Demo insights (pre-built L3 for the judge screen) ─────────────────────

    private fun buildDemoInsights(): List<Insight> = listOf(
        Insight(
            id            = UUID.randomUUID().toString(),
            level         = InsightLevel.LEVEL_3,
            category      = InsightCategory.FOOD,
            headline      = "Food spending 39% above average",
            reasoning     = """3-month average: PKR 13,200
Current month: PKR 18,400 (+39%)
This is the 3rd consecutive month of increase.
Trend: RISING — each month 15-18% above previous.
At this pace, food budget will be exhausted in 6 days.
Last 3 months show end-of-month food spikes.
Recommended: Cut daily spend by PKR 300 to close gap exactly.""",
            actionPrompt  = "Cut PKR 300/day to close the gap",
            confidence    = 0.91f,
            generatedAtMs = System.currentTimeMillis(),
        ),
        Insight(
            id            = UUID.randomUUID().toString(),
            level         = InsightLevel.LEVEL_2,
            category      = InsightCategory.UTILITIES,
            headline      = "Utility bills 12% above last month",
            reasoning     = """3 utility payments this month: LESCO, SSGC, PTCL
Total PKR 7,400 vs PKR 6,600 last month (+12%).
LESCO spike likely due to summer AC usage.
Forecast: expect another PKR 2,100-2,800 KESC bill in 2 days.""",
            actionPrompt  = null,
            confidence    = 0.84f,
            generatedAtMs = System.currentTimeMillis(),
        ),
        Insight(
            id            = UUID.randomUUID().toString(),
            level         = InsightLevel.LEVEL_1,
            category      = InsightCategory.SALARY,
            headline      = "Income this month: PKR 2,39,200",
            reasoning     = """2 income credits detected:
Salary credit: PKR 1,50,000 (HBL)
Freelance payment: PKR 85,000 (Meezan)
Profit credit: PKR 4,200 (Meezan)
Total income: PKR 2,39,200""",
            actionPrompt  = null,
            confidence    = 0.98f,
            generatedAtMs = System.currentTimeMillis(),
        ),
    )

    // ── Demo conflicts (1 duplicate + 1 amount mismatch) ──────────────────────

    private fun buildDemoConflicts(): List<ConflictResult> = listOf(
        ConflictResult(
            type                 = ConflictType.DUPLICATE,
            severity             = ConflictSeverity.HIGH,
            description          = "JazzCash and Meezan both recorded same PKR 5,000 transfer",
            incomingId           = "txn_jazzcash_transfer",
            conflictingId        = "txn_meezan_receive",
            suggestedResolution  = "Same transfer seen from both sides. Merging as single TRANSFER. Not double-counted.",
            canonicalAmount      = BigDecimal("5000.00"),
            arbitrationReasoning = "Sender JazzCash + Receiver Meezan + ±1 min gap + exact amount match → DUPLICATE (confidence 97%)",
            resolvedByLlm        = true,
        ),
    )

    // ── Demo forecasts ─────────────────────────────────────────────────────────

    private fun buildDemoForecasts(): List<Forecast> = listOf(
        Forecast(
            id                  = UUID.randomUUID().toString(),
            type                = ForecastType.UTILITY_BILL,
            description         = "LESCO / WAPDA Electric Bill expected",
            estimatedAmountMin  = BigDecimal("3400.00"),
            estimatedAmountMax  = BigDecimal("3800.00"),
            expectedDate        = LocalDate.now().plusDays(2),
            confidence          = 0.84f,
            basedOnMonths       = 4,
        ),
        Forecast(
            id                  = UUID.randomUUID().toString(),
            type                = ForecastType.SALARY_CREDIT,
            description         = "Monthly salary credit expected",
            estimatedAmountMin  = BigDecimal("145000.00"),
            estimatedAmountMax  = BigDecimal("155000.00"),
            expectedDate        = LocalDate.now().plusDays(18),
            confidence          = 0.95f,
            basedOnMonths       = 6,
        ),
        Forecast(
            id                  = UUID.randomUUID().toString(),
            type                = ForecastType.SUBSCRIPTION,
            description         = "Netflix monthly renewal",
            estimatedAmountMin  = BigDecimal("900.00"),
            estimatedAmountMax  = BigDecimal("900.00"),
            expectedDate        = LocalDate.now().plusDays(5),
            confidence          = 1.0f,
            basedOnMonths       = 3,
        ),
    )

    // ── Helper factory ─────────────────────────────────────────────────────────

    private fun tx(
        institution : String,
        description : String,
        amount      : Double,
        timestampMs : Long,
        category    : String,
    ) = ParsedTransaction(
        id              = UUID.randomUUID().toString(),
        source          = com.hisaab.parser.model.IngestionSource.SMS,
        institution     = institution,
        type            = if (amount < 0) com.hisaab.parser.model.TransactionType.DEBIT
                          else com.hisaab.parser.model.TransactionType.CREDIT,
        amount          = BigDecimal(amount.toString()),
        currency        = "PKR",
        balanceAfter    = null,
        counterparty    = description,
        referenceNumber = "DEMO-${UUID.randomUUID().toString().take(8).uppercase()}",
        rawSmsBody      = "DEMO[$institution]: $description PKR ${"%.0f".format(Math.abs(amount))}",
        timestampEpochMs = timestampMs,
        confidenceScore = 0.92f,
    )
}

// ── Companion data bundle ──────────────────────────────────────────────────────

data class DemoBundle(
    val transactions : List<ParsedTransaction>,
    val insights     : List<Insight>,
    val conflicts    : List<ConflictResult>,
    val forecasts    : List<Forecast>,
    val totalBalance : BigDecimal,
    val institutions : List<String>,
)
