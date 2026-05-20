package com.hisaab.domain.agents

import com.hisaab.domain.llm.LlmPromptLibrary
import com.hisaab.domain.llm.LlmService
import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Wave 2 — InsightAgent (UPDATED — LLM-enhanced reasoning)
 *
 * Two-tier insight generation:
 *   Tier A: Pure algorithmic (fast, offline) — always runs
 *   Tier B: LLM-enhanced reasoning — runs on top of Tier A for L2/L3 insights
 *           to produce human-quality narrative in the agent's reasoning field.
 *
 * The LLM is called ONLY when:
 *   • Deviation > 15% OR trend is RISING for 2+ months
 *   • i.e. only when the insight is worth explaining in detail
 *
 * Tools logged: analyze_spending_pattern, generate_insight
 */
class InsightAgent(
    private val llmService: LlmService? = null,  // null = offline / demo mode
) {

    suspend fun run(
        transactions: List<ParsedTransaction>,
        trace: AgentTrace,
    ): List<Insight> {
        if (transactions.isEmpty()) return emptyList()

        val now      = ZonedDateTime.now(ZoneId.of("Asia/Karachi"))
        val insights = mutableListOf<Insight>()

        for (category in HIGH_SIGNAL_CATEGORIES) {
            val categoryTxns = transactions.filter {
                categorize(it) == category && it.type == TransactionType.DEBIT
            }
            if (categoryTxns.isEmpty()) continue

            trace.step(AgentName.INSIGHT, "analyze_spending_pattern",
                "Analysing $category — ${categoryTxns.size} txns",
                toolCall = "analyze_spending_pattern(category=$category, lookback=3m)",
                status = AgentTaskStatus.RUNNING)

            val pattern = analyzePattern(categoryTxns, category, now)

            trace.step(AgentName.INSIGHT, "analyze_spending_pattern",
                "$category: avg=PKR ${pattern.threeMonthAverage.toLong()} dev=${pattern.percentageDeviation.toInt()}%",
                toolResult = "trend=${pattern.trend} consecutive=${pattern.consecutiveMonthsAbove}",
                status = AgentTaskStatus.DONE)

            // ── Tier A: algorithmic insight ───────────────────────────────────
            val baseInsight = buildAlgorithmicInsight(pattern, now)

            // ── Tier B: LLM enhancement (L2/L3 only, non-blocking on failure) ─
            val finalInsight = if (
                llmService != null &&
                (baseInsight.level == InsightLevel.LEVEL_2 || baseInsight.level == InsightLevel.LEVEL_3)
            ) {
                trace.step(AgentName.INSIGHT, "generate_insight",
                    "Requesting LLM reasoning for ${pattern.category} (${baseInsight.level})",
                    toolCall = "generate_insight(category=$category dev=${pattern.percentageDeviation.toInt()}%)",
                    status = AgentTaskStatus.RUNNING)

                enhanceWithLlm(baseInsight, pattern)
                    .also { enhanced ->
                        trace.step(AgentName.INSIGHT, "generate_insight",
                            "✅ LLM insight: ${enhanced.headline}",
                            toolResult = "confidence=${enhanced.confidence} provider=llm",
                            status = AgentTaskStatus.DONE)
                    }
            } else {
                trace.step(AgentName.INSIGHT, "generate_insight",
                    "✅ Algorithmic insight: ${baseInsight.headline}",
                    toolResult = "confidence=${baseInsight.confidence} provider=algo",
                    status = AgentTaskStatus.DONE)
                baseInsight
            }

            insights.add(finalInsight)
        }

        return insights.sortedByDescending { it.level.ordinal }
    }

    // ── Tier B: LLM enhancement ───────────────────────────────────────────────

    private suspend fun enhanceWithLlm(base: Insight, pattern: PatternAnalysis): Insight {
        return try {
            val response = llmService!!.complete(
                systemPrompt = LlmPromptLibrary.INSIGHT_SYSTEM,
                userPrompt   = LlmPromptLibrary.insightUser(
                    category               = pattern.category.name.lowercase(),
                    currentMonthPkr        = pattern.currentMonthAmount.toLong(),
                    avgPkr                 = pattern.threeMonthAverage.toLong(),
                    deviationPct           = pattern.percentageDeviation.toInt(),
                    trend                  = pattern.trend.name,
                    daysRemaining          = pattern.daysRemainingInMonth,
                    consecutiveMonthsAbove = pattern.consecutiveMonthsAbove,
                ),
                maxTokens = 300,
            )

            val parsed = parseLlmInsightResponse(response.content)
            base.copy(
                headline     = parsed.headline  ?: base.headline,
                reasoning    = parsed.reasoning ?: base.reasoning,
                actionPrompt = parsed.action    ?: base.actionPrompt,
                confidence   = parsed.confidence ?: base.confidence,
            )
        } catch (e: Exception) {
            base  // graceful fallback to algorithmic on LLM failure
        }
    }

    private data class ParsedLlmInsight(
        val headline: String?,
        val reasoning: String?,
        val action: String?,
        val confidence: Float?,
    )

    private fun parseLlmInsightResponse(raw: String): ParsedLlmInsight {
        val lines = raw.lines().associate { line ->
            val idx = line.indexOf(":")
            if (idx < 0) "" to ""
            else line.substring(0, idx).trim().uppercase() to line.substring(idx + 1).trim()
        }
        return ParsedLlmInsight(
            headline   = lines["HEADLINE"],
            reasoning  = lines["REASONING"],
            action     = lines["ACTION"]?.takeIf { it != "NONE" },
            confidence = lines["CONFIDENCE"]?.toFloatOrNull(),
        )
    }

    // ── Tier A: algorithmic pattern analysis ──────────────────────────────────

    private fun analyzePattern(
        txns: List<ParsedTransaction>,
        category: InsightCategory,
        now: ZonedDateTime,
    ): PatternAnalysis {
        val startOfMonth  = now.withDayOfMonth(1).toInstant().toEpochMilli()
        val daysRemaining = now.toLocalDate().lengthOfMonth() - now.dayOfMonth
        val currentTotal  = txns.filter { it.timestampEpochMs >= startOfMonth }.sumAmount()
        val avg3m         = computeThreeMonthAvg(txns, now)
        val deviation     = if (avg3m.signum() == 0) 0.0
                            else (currentTotal - avg3m).toDouble() / avg3m.toDouble() * 100.0
        val daysElapsed   = now.dayOfMonth.coerceAtLeast(1)
        val dailyRate     = currentTotal.divide(BigDecimal(daysElapsed), 2, RoundingMode.HALF_UP)
        val projected     = dailyRate.multiply(BigDecimal(now.toLocalDate().lengthOfMonth()))
        val consecutive   = countConsecutiveMonthsAbove(txns, now, avg3m)
        val trend         = when {
            deviation > 10.0  -> Trend.RISING
            deviation < -10.0 -> Trend.FALLING
            else              -> Trend.STABLE
        }
        return PatternAnalysis(category, currentTotal, avg3m, deviation,
            consecutive, daysRemaining, projected, trend)
    }

    private fun buildAlgorithmicInsight(pattern: PatternAnalysis, now: ZonedDateTime): Insight {
        val level = when {
            pattern.trend == Trend.RISING && pattern.consecutiveMonthsAbove >= 2 -> InsightLevel.LEVEL_3
            kotlin.math.abs(pattern.percentageDeviation) > 15.0 -> InsightLevel.LEVEL_2
            else -> InsightLevel.LEVEL_1
        }
        val pct = pattern.percentageDeviation.toInt()
        val catName = pattern.category.name.lowercase()
        val (headline, reasoning, action) = when (level) {
            InsightLevel.LEVEL_1 -> Triple(
                "PKR ${pattern.currentMonthAmount.toLong()} spent on $catName this month",
                "Monthly $catName spend: PKR ${pattern.currentMonthAmount.toLong()}",
                null,
            )
            InsightLevel.LEVEL_2 -> Triple(
                "$catName ${kotlin.math.abs(pct)}% ${if (pct > 0) "above" else "below"} average",
                "3-month avg: PKR ${pattern.threeMonthAverage.toLong()}\n" +
                    "This month: PKR ${pattern.currentMonthAmount.toLong()} ($pct%)\n" +
                    "Trend: ${pattern.trend.name}",
                "Review your $catName spending",
            )
            InsightLevel.LEVEL_3 -> {
                val dailyCut = if (pattern.daysRemainingInMonth > 0)
                    (pattern.projectedMonthTotal - pattern.threeMonthAverage)
                        .divide(BigDecimal(pattern.daysRemainingInMonth), 0, RoundingMode.CEILING)
                else BigDecimal.ZERO
                Triple(
                    "Budget pressure on $catName — ${pattern.consecutiveMonthsAbove} months rising",
                    "3-month avg: PKR ${pattern.threeMonthAverage.toLong()}\n" +
                        "This month: PKR ${pattern.currentMonthAmount.toLong()} (+$pct%)\n" +
                        "Consecutive months above avg: ${pattern.consecutiveMonthsAbove}\n" +
                        "Projected month total: PKR ${pattern.projectedMonthTotal.toLong()}\n" +
                        "Days remaining: ${pattern.daysRemainingInMonth}",
                    "Cut PKR ${dailyCut.toLong()}/day on $catName to stay within budget",
                )
            }
        }
        return Insight(
            id            = UUID.randomUUID().toString(),
            level         = level,
            category      = pattern.category,
            headline      = headline,
            reasoning     = reasoning,
            actionPrompt  = action,
            confidence    = if (level == InsightLevel.LEVEL_3) 0.88f else if (level == InsightLevel.LEVEL_2) 0.82f else 0.95f,
            generatedAtMs = System.currentTimeMillis(),
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun computeThreeMonthAvg(txns: List<ParsedTransaction>, now: ZonedDateTime): BigDecimal {
        val buckets = (1..3).map { back ->
            val t = now.minusMonths(back.toLong())
            val s = t.withDayOfMonth(1).toInstant().toEpochMilli()
            val e = t.withDayOfMonth(t.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).toInstant().toEpochMilli()
            txns.filter { it.timestampEpochMs in s..e }.sumAmount()
        }.filter { it.signum() > 0 }
        return if (buckets.isEmpty()) BigDecimal.ZERO
        else buckets.reduce { a, b -> a.add(b) }.divide(BigDecimal(buckets.size), 2, RoundingMode.HALF_UP)
    }

    private fun countConsecutiveMonthsAbove(txns: List<ParsedTransaction>, now: ZonedDateTime, avg: BigDecimal): Int {
        var count = 0
        for (i in 1..3) {
            val t = now.minusMonths(i.toLong())
            val s = t.withDayOfMonth(1).toInstant().toEpochMilli()
            val e = t.withDayOfMonth(t.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).toInstant().toEpochMilli()
            if (txns.filter { it.timestampEpochMs in s..e }.sumAmount() > avg) count++ else break
        }
        return count
    }

    private fun List<ParsedTransaction>.sumAmount() =
        fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

    private fun BigDecimal.toLong() = this.setScale(0, RoundingMode.HALF_UP).toLong()

    private fun categorize(tx: ParsedTransaction): InsightCategory {
        val desc = "${tx.counterparty?.lowercase() ?: ""} ${tx.rawSmsBody.lowercase()}"
        return when {
            FOOD_KEYWORDS.any      { desc.contains(it) } -> InsightCategory.FOOD
            TRANSPORT_KEYWORDS.any { desc.contains(it) } -> InsightCategory.TRANSPORT
            UTILITY_KEYWORDS.any   { desc.contains(it) } -> InsightCategory.UTILITIES
            tx.type == TransactionType.BILL_PAYMENT       -> InsightCategory.UTILITIES
            tx.type == TransactionType.TRANSFER           -> InsightCategory.TRANSFER
            else                                          -> InsightCategory.GENERAL
        }
    }

    companion object {
        private val HIGH_SIGNAL_CATEGORIES = listOf(
            InsightCategory.FOOD, InsightCategory.TRANSPORT,
            InsightCategory.UTILITIES, InsightCategory.GENERAL,
        )
        private val FOOD_KEYWORDS      = listOf("food","restaurant","hotel","cafe","biryani","pizza","burger","kfc","mcdonald","eatery")
        private val TRANSPORT_KEYWORDS = listOf("uber","careem","fuel","petrol","transport","rickshaw","bus","metro")
        private val UTILITY_KEYWORDS   = listOf("lesco","kesc","ptcl","sui","gas","electric","bill","k-electric","ssgc")
    }
}
