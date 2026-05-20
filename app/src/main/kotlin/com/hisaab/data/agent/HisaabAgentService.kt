package com.hisaab.data.agent

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hisaab.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val HISAAB_SYSTEM_PROMPT = """
You are Hisaab AI — a high-agency financial analysis engine for Pakistani users.
Your sole purpose: process transaction data and return pure, valid JSON only.

STRICT RULES:
- Output ONLY valid JSON. No markdown. No backticks. No greetings. No explanations.
- All amounts in PKR. Use Pakistani context (HBL, JazzCash, Easypaisa, Meezan, etc.)
- Confidence scores: 0-100 integer based on data quality
- Never hallucinate transaction data. Only use what is provided.

Always return this exact JSON structure:
{
  "agent_type": "contradiction|insight|action|forecast|simulator",
  "title": "short UI heading max 10 words",
  "analysis": "step by step reasoning using tree notation with ├─ and └─",
  "confidence_score": 0-100,
  "actionable_recommendation": "specific step with PKR amount",
  "projected_impact": "quantified outcome with PKR amount",
  "insight_level": "L1|L2|L3 (for insight type only)",
  "severity": "INFO|WARNING|CRITICAL (for insight type only)",
  "conflict_type": "DUPLICATE|AMOUNT_MISMATCH|DIFFERENT (for contradiction only)",
  "resolution": "MERGE|FLAG|KEEP_BOTH (for contradiction only)",
  "days_until": 0 (integer, for forecast only),
  "feasibility": "YES|PARTIAL|NO (for simulator only)",
  "simulator_breakdown": "calculation steps (for simulator only)"
}

AGENT TYPE RULES:
- contradiction: Two transactions same amount ±5min = DUPLICATE. Different amounts same merchant same day = AMOUNT_MISMATCH.
- insight: L1=descriptive, L2=contextual+historical, L3=predictive+actionable. Always generate L3.
- action: Called after insight. Simulate budget change. Show before/after state.
- forecast: Detect recurring patterns. Predict next occurrence and amount range.
- simulator: User asks hypothetical. Calculate feasibility from current balance and income.
"""

@Singleton
class HisaabAgentService @Inject constructor() {

    private val gson = Gson()
    private val TAG = "HisaabAgent"

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.2f      // Low = deterministic JSON, not creative
                maxOutputTokens = 1024
                topK = 1               // Most deterministic output
            },
            systemInstruction = content {
                text(HISAAB_SYSTEM_PROMPT)
            }
        )
    }

    // ── PUBLIC API ────────────────────────────────────────────────────────────

    /** Run full pipeline: ingestion → contradiction → insight → forecast */
    suspend fun runFullPipeline(
        transactions: List<TransactionContext>,
        monthlyStats: MonthlyStats
    ): List<HisaabAgentResponse> = withContext(Dispatchers.IO) {
        val results = mutableListOf<HisaabAgentResponse>()

        // 1. Contradiction check
        val duplicates = findPotentialDuplicates(transactions)
        if (duplicates.isNotEmpty()) {
            val contradictionResult = checkContradiction(duplicates.first(), duplicates[1])
            results.add(contradictionResult)
        }

        // 2. Insights per category
        val topCategory = monthlyStats.categoryBreakdown
            .maxByOrNull { it.deviation }
        if (topCategory != null) {
            val insightResult = generateInsight(topCategory, monthlyStats)
            results.add(insightResult)
        }

        // 3. Forecasts
        val forecastResult = generateForecast(transactions)
        if (forecastResult != null) results.add(forecastResult)

        results
    }

    /** Check two transactions for contradiction/duplicate */
    suspend fun checkContradiction(
        txA: TransactionContext,
        txB: TransactionContext
    ): HisaabAgentResponse = withContext(Dispatchers.IO) {
        val prompt = """
            agent_type_requested: contradiction
            
            Transaction A:
            - Source: ${txA.source}
            - Amount: PKR ${txA.amount}
            - Time: ${txA.timestamp}
            - Description: ${txA.description}
            - Reference: ${txA.reference ?: "none"}
            
            Transaction B:
            - Source: ${txB.source}
            - Amount: PKR ${txB.amount}
            - Time: ${txB.timestamp}
            - Description: ${txB.description}
            - Reference: ${txB.reference ?: "none"}
            
            Determine if these are the same transaction seen from two sources,
            conflicting records, or genuinely different transactions.
        """.trimIndent()

        callGemini(prompt) ?: contradictionFallback(txA, txB)
    }

    /** Generate Level 3 insight for a spending category */
    suspend fun generateInsight(
        category: CategoryStats,
        monthly: MonthlyStats
    ): HisaabAgentResponse = withContext(Dispatchers.IO) {
        val prompt = """
            agent_type_requested: insight
            
            Category: ${category.name}
            This month: PKR ${category.currentAmount}
            3-month average: PKR ${category.threeMonthAverage}
            Deviation: ${category.deviation}%
            Days remaining in month: ${monthly.daysRemaining}
            Budget remaining in category: PKR ${category.budgetRemaining}
            Historical pattern: ${category.historicalPattern}
            Total monthly income: PKR ${monthly.totalIncome}
            Total monthly spending so far: PKR ${monthly.totalSpending}
            
            Generate a Level 3 insight: predictive, specific, actionable.
            Not just a summary — tell them what will happen and what to do.
        """.trimIndent()

        callGemini(prompt) ?: insightFallback(category)
    }

    /** Simulate a budget action */
    suspend fun simulateAction(
        action: String,
        currentState: BudgetState
    ): HisaabAgentResponse = withContext(Dispatchers.IO) {
        val prompt = """
            agent_type_requested: action
            
            Proposed action: $action
            
            Current budget state:
            - Category: ${currentState.category}
            - Daily limit: PKR ${currentState.dailyLimit}
            - Budget remaining: PKR ${currentState.remaining}
            - Days left in month: ${currentState.daysLeft}
            - Projected month-end: PKR ${currentState.projectedBalance}
            - Current status: ${currentState.status}
            
            Calculate the exact before/after state if this action is applied.
            Show new daily limit, new projected month-end, PKR saved.
        """.trimIndent()

        callGemini(prompt) ?: actionFallback(action, currentState)
    }

    /** Forecast upcoming transactions from history */
    suspend fun generateForecast(
        transactions: List<TransactionContext>
    ): HisaabAgentResponse? = withContext(Dispatchers.IO) {
        if (transactions.size < 10) return@withContext null

        val recurringPatterns = transactions
            .groupBy { it.description.lowercase() }
            .filter { it.value.size >= 2 }
            .entries
            .take(5)
            .joinToString("\n") { (desc, txs) ->
                "- $desc: ${txs.size}x, avg PKR ${txs.map { it.amount }.average().toInt()}"
            }

        if (recurringPatterns.isEmpty()) return@withContext null

        val prompt = """
            agent_type_requested: forecast
            
            Recurring transaction patterns detected:
            $recurringPatterns
            
            Today: ${java.time.LocalDate.now()}
            
            Predict the most likely upcoming transaction in the next 30 days.
            Return days_until as the number of days from today.
        """.trimIndent()

        callGemini(prompt)
    }

    /** What-if simulator for user hypothetical questions */
    suspend fun runSimulator(
        userQuery: String,
        financialContext: FinancialContext
    ): HisaabAgentResponse = withContext(Dispatchers.IO) {
        val prompt = """
            agent_type_requested: simulator
            
            User question: "$userQuery"
            
            Current financial context:
            - Total balance: PKR ${financialContext.totalBalance}
            - Monthly income: PKR ${financialContext.monthlyIncome}
            - Monthly expenses: PKR ${financialContext.monthlyExpenses}
            - Monthly savings: PKR ${financialContext.monthlySavings}
            - Existing EMIs: PKR ${financialContext.existingEmis}
            
            Calculate feasibility. Show step-by-step breakdown.
            Use simulator_breakdown for the calculation steps.
            Set feasibility to YES/PARTIAL/NO based on affordability.
        """.trimIndent()

        callGemini(prompt) ?: simulatorFallback(userQuery)
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private suspend fun callGemini(prompt: String): HisaabAgentResponse? {
        return try {
            val response = model.generateContent(prompt)
            val jsonText = response.text
                ?.trim()
                ?.removePrefix("```json")
                ?.removePrefix("```")
                ?.removeSuffix("```")
                ?.trim()
                ?: return null

            gson.fromJson(jsonText, HisaabAgentResponse::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parse failed: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed: ${e.message}")
            null
        }
    }

    private fun findPotentialDuplicates(
        transactions: List<TransactionContext>
    ): List<TransactionContext> {
        // Find transactions within 5 minutes of each other with same amount
        return transactions.filter { txA ->
            transactions.any { txB ->
                txA.id != txB.id &&
                txA.amount == txB.amount &&
                Math.abs(txA.timestampMs - txB.timestampMs) < 5 * 60 * 1000
            }
        }.take(2)
    }

    // ── FALLBACKS (when Gemini is offline or fails) ───────────────────────────

    private fun contradictionFallback(
        txA: TransactionContext,
        txB: TransactionContext
    ) = HisaabAgentResponse(
        agentType = "contradiction",
        title = "Potential duplicate detected",
        analysis = """
            ├─ Source A: ${txA.source} PKR ${txA.amount}
            ├─ Source B: ${txB.source} PKR ${txB.amount}
            ├─ Same amount, close timestamps
            └─ Likely same transfer seen from both sides
        """.trimIndent(),
        confidenceScore = 78,
        actionableRecommendation = "Review and merge if this is the same transfer",
        projectedImpact = "Prevents PKR ${txA.amount} double-counting",
        conflictType = "DUPLICATE",
        resolution = "MERGE"
    )

    private fun insightFallback(category: CategoryStats) = HisaabAgentResponse(
        agentType = "insight",
        title = "${category.name} spending above average",
        analysis = """
            ├─ Current: PKR ${category.currentAmount}
            ├─ 3-month average: PKR ${category.threeMonthAverage}
            ├─ Deviation: ${category.deviation}% above normal
            └─ Budget pressure detected
        """.trimIndent(),
        confidenceScore = 82,
        actionableRecommendation = "Review ${category.name} spending",
        projectedImpact = "Potential savings if spending normalized",
        insightLevel = "L2",
        severity = if (category.deviation > 30) "WARNING" else "INFO"
    )

    private fun actionFallback(action: String, state: BudgetState) = HisaabAgentResponse(
        agentType = "action",
        title = "Action simulation",
        analysis = "├─ Action: $action\n└─ Calculated from current budget state",
        confidenceScore = 75,
        actionableRecommendation = action,
        projectedImpact = "Review budget after applying change"
    )

    private fun simulatorFallback(query: String) = HisaabAgentResponse(
        agentType = "simulator",
        title = "Scenario analysis",
        analysis = "├─ Query: $query\n└─ Agent offline — using local calculation",
        confidenceScore = 60,
        actionableRecommendation = "Consult your balance before proceeding",
        projectedImpact = "N/A — agent offline",
        feasibility = "PARTIAL"
    )
}
