package com.hisaab.domain.model

import java.math.BigDecimal
import java.time.LocalDate

// ─── Insight Model ────────────────────────────────────────────────────────────

enum class InsightLevel { LEVEL_1, LEVEL_2, LEVEL_3 }
enum class InsightCategory { FOOD, TRANSPORT, UTILITIES, SHOPPING, HEALTH, SALARY, TRANSFER, GENERAL }

data class Insight(
    val id: String,
    val level: InsightLevel,
    val category: InsightCategory,
    val headline: String,           // Short — shown on card title
    val reasoning: String,          // Full agent reasoning — shown in JetBrains Mono
    val actionPrompt: String?,      // CTA text e.g. "Cut PKR 300/day to close gap"
    val confidence: Float,          // 0.0–1.0
    val generatedAtMs: Long,
)

// ─── Conflict Model ───────────────────────────────────────────────────────────

enum class ConflictType { DUPLICATE, AMOUNT_MISMATCH, BALANCE_INCONSISTENCY, CLEAN }
enum class ConflictSeverity { LOW, MEDIUM, HIGH }

data class ConflictResult(
    val type: ConflictType,
    val severity: ConflictSeverity,
    val description: String,
    val incomingId: String,
    val conflictingId: String?,         // null when CLEAN
    val suggestedResolution: String?,
    val canonicalAmount: java.math.BigDecimal? = null,  // LLM-selected correct amount
    val arbitrationReasoning: String? = null,           // LLM's one-line explanation
    val resolvedByLlm: Boolean = false,                 // true if LLM arbitrated
)

// ─── Pattern Analysis Model ───────────────────────────────────────────────────

data class PatternAnalysis(
    val category: InsightCategory,
    val currentMonthAmount: BigDecimal,
    val threeMonthAverage: BigDecimal,
    val percentageDeviation: Double,    // positive = above avg
    val consecutiveMonthsAbove: Int,
    val daysRemainingInMonth: Int,
    val projectedMonthTotal: BigDecimal,
    val trend: Trend,
)

enum class Trend { RISING, FALLING, STABLE }

// ─── Budget Action Model ──────────────────────────────────────────────────────

enum class ActionType { REDUCE_CATEGORY, SET_LIMIT, DEFER_PURCHASE, REALLOCATE }

data class BudgetAction(
    val type: ActionType,
    val targetCategory: InsightCategory,
    val targetAmount: BigDecimal,
    val rationale: String,
    val projectedSaving: BigDecimal,
    val effortScore: Int,           // 1–5 (1 = easy)
    val impactScore: Int,           // 1–5 (5 = high impact)
)

data class BudgetState(
    val totalBalance: BigDecimal,
    val monthlyIncome: BigDecimal,
    val categorySpends: Map<InsightCategory, BigDecimal>,
    val daysRemainingInMonth: Int,
)

data class SimulationResult(
    val action: BudgetAction,
    val stateBefore: BudgetState,
    val stateAfter: BudgetState,
    val projectedSaving: BigDecimal,
    val newBalanceAtMonthEnd: BigDecimal,
    val reasoning: String,
)

// ─── Forecast Model ───────────────────────────────────────────────────────────

enum class ForecastType { UTILITY_BILL, SALARY_CREDIT, SUBSCRIPTION, TRANSFER, GENERAL_EXPENSE }

data class Forecast(
    val id: String,
    val type: ForecastType,
    val description: String,
    val estimatedAmountMin: BigDecimal,
    val estimatedAmountMax: BigDecimal,
    val expectedDate: LocalDate,
    val confidence: Float,
    val basedOnMonths: Int,         // how many months of data drove this forecast
)

// ─── Agent Trace Model ────────────────────────────────────────────────────────

enum class AgentTaskStatus { PENDING, RUNNING, DONE, FAILED, CONFLICT_FOUND }
enum class AgentName { INGESTION, CONTRADICTION, INSIGHT, ACTION, FORECAST, SIMULATOR }

data class WhatIfResult(
    val title: String,
    val feasibility: String,
    val breakdown: String,
    val impact: String,
    val recommendation: String
)

data class AgentTraceStep(
    val agentName: AgentName,
    val taskName: String,
    val detail: String,
    val status: AgentTaskStatus,
    val timestampMs: Long,
    val toolCall: String? = null,   // e.g. "parse_transaction(sender=HBL)"
    val toolResult: String? = null, // e.g. "DEBIT PKR 5,000 confidence=0.92"
)

data class AgentTrace(
    val sessionId: String,
    val startedAtMs: Long,
    var completedAtMs: Long? = null,
    val steps: MutableList<AgentTraceStep> = mutableListOf(),
    var isLive: Boolean = true,
) {
    fun addStep(step: AgentTraceStep) { steps.add(step) }
    fun complete() { completedAtMs = System.currentTimeMillis(); isLive = false }
}
