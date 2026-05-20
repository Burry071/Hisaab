package com.hisaab.domain.agents

import com.hisaab.domain.model.ActionType
import com.hisaab.domain.model.AgentName
import com.hisaab.domain.model.AgentTaskStatus
import com.hisaab.domain.model.AgentTrace
import com.hisaab.domain.model.BudgetAction
import com.hisaab.domain.model.BudgetState
import com.hisaab.domain.model.Forecast
import com.hisaab.domain.model.Insight
import com.hisaab.domain.model.InsightCategory
import com.hisaab.domain.model.InsightLevel
import com.hisaab.domain.model.SimulationResult
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Wave 3 — ActionAgent (runs after Insight + Forecast complete)
 *
 * For each L3 insight, generates up to 3 ranked BudgetActions and simulates
 * the before/after state so the user can see the financial impact of each choice.
 *
 * Actions are ranked by: impact_score DESC, effort_score ASC
 *
 * Tool logged: simulate_action
 */
class ActionAgent {

    suspend fun run(
        insights: List<Insight>,
        forecasts: List<Forecast>,
        transactions: List<ParsedTransaction>,
        trace: AgentTrace,
    ): List<SimulationResult> {
        val l3Insights = insights.filter { it.level == InsightLevel.LEVEL_3 }
        if (l3Insights.isEmpty()) {
            trace.step(
                agent      = AgentName.ACTION,
                task       = "simulate_action",
                detail     = "No L3 insights — no simulations needed",
                toolResult = "SKIP: no_l3_insights",
                status     = AgentTaskStatus.DONE,
            )
            return emptyList()
        }

        val now           = ZonedDateTime.now(ZoneId.of("Asia/Karachi"))
        val currentState  = buildBudgetState(transactions, now, forecasts)
        val simulations   = mutableListOf<SimulationResult>()

        for (insight in l3Insights) {
            trace.step(
                agent    = AgentName.ACTION,
                task     = "simulate_action",
                detail   = "Generating actions for ${insight.category} (${insight.level})",
                toolCall = "simulate_action(category=${insight.category}, balance=PKR ${currentState.totalBalance.toInt()})",
                status   = AgentTaskStatus.RUNNING,
            )

            val actions = generateActions(insight, currentState)
            for (action in actions.take(MAX_ACTIONS_PER_INSIGHT)) {
                val simulation = simulate(action, currentState, insight)
                simulations.add(simulation)

                trace.step(
                    agent      = AgentName.ACTION,
                    task       = "simulate_action",
                    detail     = "✅ ${action.type} — save PKR ${simulation.projectedSaving.toInt()}/month",
                    toolResult = "impact=${action.impactScore} effort=${action.effortScore} " +
                        "newEOM=PKR ${simulation.newBalanceAtMonthEnd.toInt()}",
                    status     = AgentTaskStatus.DONE,
                )
            }
        }

        return simulations.sortedWith(
            compareByDescending<SimulationResult> { it.action.impactScore }
                .thenBy { it.action.effortScore }
        )
    }

    // ── Action generation ─────────────────────────────────────────────────────

    private fun generateActions(insight: Insight, state: BudgetState): List<BudgetAction> {
        val currentSpend = state.categorySpends[insight.category] ?: BigDecimal.ZERO
        val daysLeft     = state.daysRemainingInMonth.coerceAtLeast(1)

        return listOf(
            // Option A: Set a hard daily limit
            BudgetAction(
                type            = ActionType.SET_LIMIT,
                targetCategory  = insight.category,
                targetAmount    = currentSpend.multiply(BigDecimal("0.80")), // 20% cut
                rationale       = "Reduce ${insight.category.name.lowercase()} by 20% for the rest of the month",
                projectedSaving = currentSpend.multiply(BigDecimal("0.20")),
                effortScore     = 2,
                impactScore     = 4,
            ),
            // Option B: Reduce category gradually
            BudgetAction(
                type            = ActionType.REDUCE_CATEGORY,
                targetCategory  = insight.category,
                targetAmount    = currentSpend.multiply(BigDecimal("0.90")), // 10% cut
                rationale       = "Cut ${insight.category.name.lowercase()} spending by PKR ${(currentSpend * BigDecimal("0.10")).toInt()} (10%)",
                projectedSaving = currentSpend.multiply(BigDecimal("0.10")),
                effortScore     = 1,
                impactScore     = 3,
            ),
            // Option C: Reallocate from lowest-priority category
            BudgetAction(
                type            = ActionType.REALLOCATE,
                targetCategory  = insight.category,
                targetAmount    = currentSpend.multiply(BigDecimal("0.70")), // 30% cut
                rationale       = "Aggressive reallocate — cut ${insight.category.name.lowercase()} by 30% to close gap",
                projectedSaving = currentSpend.multiply(BigDecimal("0.30")),
                effortScore     = 4,
                impactScore     = 5,
            ),
        )
    }

    // ── Simulation ────────────────────────────────────────────────────────────

    private fun simulate(
        action: BudgetAction,
        state: BudgetState,
        insight: Insight,
    ): SimulationResult {
        val newCategorySpends = state.categorySpends.toMutableMap()
        newCategorySpends[action.targetCategory] = action.targetAmount

        val stateAfter = state.copy(
            categorySpends = newCategorySpends,
            totalBalance   = state.totalBalance.add(action.projectedSaving),
        )

        val monthlyIncome     = state.monthlyIncome
        val totalSpendBefore  = state.categorySpends.values.fold(BigDecimal.ZERO) { a, b -> a.add(b) }
        val totalSpendAfter   = newCategorySpends.values.fold(BigDecimal.ZERO) { a, b -> a.add(b) }
        val newMonthEndBal    = state.totalBalance
            .add(monthlyIncome)
            .subtract(totalSpendAfter)
            .max(BigDecimal.ZERO)

        val reasoning = buildString {
            appendLine("Current ${action.targetCategory.name.lowercase()} spend: PKR ${state.categorySpends[action.targetCategory]?.toInt()}")
            appendLine("After action: PKR ${action.targetAmount.toInt()}")
            appendLine("Projected saving: PKR ${action.projectedSaving.toInt()}")
            appendLine("New end-of-month balance: PKR ${newMonthEndBal.toInt()}")
            appendLine("Effort: ${action.effortScore}/5  Impact: ${action.impactScore}/5")
        }

        return SimulationResult(
            action               = action,
            stateBefore          = state,
            stateAfter           = stateAfter,
            projectedSaving      = action.projectedSaving,
            newBalanceAtMonthEnd = newMonthEndBal,
            reasoning            = reasoning,
        )
    }

    // ── Budget state builder ──────────────────────────────────────────────────

    private fun buildBudgetState(
        transactions: List<ParsedTransaction>,
        now: ZonedDateTime,
        forecasts: List<Forecast>,
    ): BudgetState {
        val startOfMonth = now.withDayOfMonth(1).toInstant().toEpochMilli()
        val monthTxns    = transactions.filter { it.timestampEpochMs >= startOfMonth }

        val totalBalance = transactions
            .filter { it.type == TransactionType.CREDIT }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
            .subtract(
                transactions.filter { it.type == TransactionType.DEBIT }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
            )

        val monthlyIncome = monthTxns
            .filter { it.type == TransactionType.CREDIT }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

        val categorySpends = InsightCategory.values().associateWith { cat ->
            monthTxns
                .filter { it.type == TransactionType.DEBIT }
                .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
        }

        val daysRemaining = now.toLocalDate().lengthOfMonth() - now.dayOfMonth

        return BudgetState(
            totalBalance        = totalBalance.max(BigDecimal.ZERO),
            monthlyIncome       = monthlyIncome,
            categorySpends      = categorySpends,
            daysRemainingInMonth = daysRemaining,
        )
    }

    private fun BigDecimal.toInt() = this.setScale(0, RoundingMode.HALF_UP).toInt()

    companion object {
        private const val MAX_ACTIONS_PER_INSIGHT = 3
    }
}
