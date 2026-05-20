package com.hisaab.data.agent

import com.google.gson.annotations.SerializedName

/**
 * Unified response from all 5 Hisaab agents.
 * Gemini always returns this exact structure — no exceptions.
 */
data class HisaabAgentResponse(
    // Which agent produced this: ingestion|contradiction|insight|action|forecast|simulator|fallback
    @SerializedName("agent_type")
    val agentType: String,

    // Short UI heading e.g. "Food spending 39% above average"
    val title: String,

    // Full reasoning shown in Agent Reasoning block
    val analysis: String,

    // 0-100 integer
    @SerializedName("confidence_score")
    val confidenceScore: Int,

    // Specific actionable step e.g. "Cut daily food by PKR 300"
    @SerializedName("actionable_recommendation")
    val actionableRecommendation: String,

    // Quantified outcome e.g. "+PKR 2,400 to month-end balance"
    @SerializedName("projected_impact")
    val projectedImpact: String,

    // INSIGHT only: L1/L2/L3
    @SerializedName("insight_level")
    val insightLevel: String? = null,

    // INSIGHT only: INFO/WARNING/CRITICAL
    val severity: String? = null,

    // CONTRADICTION only: DUPLICATE/AMOUNT_MISMATCH/DIFFERENT
    @SerializedName("conflict_type")
    val conflictType: String? = null,

    // CONTRADICTION only: MERGE/FLAG/KEEP_BOTH
    val resolution: String? = null,

    // FORECAST only: days until expected transaction
    @SerializedName("days_until")
    val daysUntil: Int? = null,

    // SIMULATOR only: YES/PARTIAL/NO
    val feasibility: String? = null,

    // SIMULATOR only: breakdown of simulator calculation
    @SerializedName("simulator_breakdown")
    val simulatorBreakdown: String? = null
)

data class TransactionContext(
    val id: String,
    val source: String,          // HBL, JazzCash, etc.
    val amount: Double,
    val timestamp: String,       // readable
    val timestampMs: Long,       // for duplicate detection
    val description: String,
    val reference: String? = null
)

data class MonthlyStats(
    val totalIncome: Double,
    val totalSpending: Double,
    val daysRemaining: Int,
    val categoryBreakdown: List<CategoryStats>
)

data class CategoryStats(
    val name: String,
    val currentAmount: Double,
    val threeMonthAverage: Double,
    val deviation: Int,          // percentage
    val budgetRemaining: Double,
    val historicalPattern: String
)

data class BudgetState(
    val category: String,
    val dailyLimit: Double,
    val remaining: Double,
    val daysLeft: Int,
    val projectedBalance: Double,
    val status: String           // RISK / SAFE / CRITICAL
)

data class FinancialContext(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val monthlySavings: Double,
    val existingEmis: Double = 0.0
)
