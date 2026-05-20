package com.hisaab.domain.agents

import com.hisaab.domain.model.AgentTaskStatus
import com.hisaab.domain.model.AgentTrace
import com.hisaab.domain.model.AgentTraceStep
import com.hisaab.domain.model.AgentName
import com.hisaab.domain.model.ConflictResult
import com.hisaab.domain.model.Forecast
import com.hisaab.domain.model.Insight
import com.hisaab.domain.model.SimulationResult
import com.hisaab.parser.model.ParsedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Hisaab Agent Orchestrator — the Antigravity 5-agent DAG.
 *
 * Execution model:
 *   Wave 1 (parallel): IngestionAgent + ContradictionAgent
 *   Wave 2 (parallel): InsightAgent + ForecastAgent   ← depend on Wave 1
 *   Wave 3 (serial):   ActionAgent                   ← depends on Wave 2
 *
 * The [AgentTrace] is emitted as a StateFlow so the AgentScreen can
 * render live progress in JetBrains Mono tree format.
 *
 * Every tool call is logged to the trace so judges see real reasoning.
 */
class HisaabAgentOrchestrator(
    private val ingestionAgent: IngestionAgent,
    private val contradictionAgent: ContradictionAgent,
    private val insightAgent: InsightAgent,
    private val actionAgent: ActionAgent,
    private val forecastAgent: ForecastAgent,
) {
    private val _trace = MutableStateFlow(newTrace())
    /** Observe this in AgentViewModel to drive the live AgentScreen UI. */
    val trace: StateFlow<AgentTrace> = _trace.asStateFlow()

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Main entry point. Call when new SMS/notification arrives or on first app load.
     *
     * @param incomingSms    Raw parsed transactions from the current ingestion cycle.
     * @param existingTxns   All transactions already stored in Room DB.
     * @return [OrchestratorResult] with insights, conflicts, forecasts, and actions.
     */
    suspend fun runPipeline(
        incomingSms: List<ParsedTransaction>,
        existingTxns: List<ParsedTransaction>,
    ): OrchestratorResult = coroutineScope {

        val sessionTrace = newTrace()
        _trace.value = sessionTrace

        // ── WAVE 1: Ingestion + Contradiction (parallel) ──────────────────────
        log(sessionTrace, AgentName.INGESTION,
            if (incomingSms.isNotEmpty())
                "Running parse_transaction for ${incomingSms.size} incoming SMS"
            else
                "Checking ${existingTxns.size} existing records in database",
            status = AgentTaskStatus.RUNNING)

        val ingestionDeferred = async {
            ingestionAgent.run(incomingSms, sessionTrace, dbRecordCount = existingTxns.size)
        }
        val contradictionDeferred = async {
            contradictionAgent.run(incomingSms, existingTxns, sessionTrace)
        }

        val normalizedTxns  = ingestionDeferred.await()
        val conflicts       = contradictionDeferred.await()

        log(sessionTrace, AgentName.INGESTION,
            "Ingestion complete — ${normalizedTxns.size} transactions normalised",
            toolResult = "confidence_avg=${normalizedTxns.averageConfidence()}",
            status = AgentTaskStatus.DONE)

        log(sessionTrace, AgentName.CONTRADICTION,
            "Contradiction scan complete — ${conflicts.size} conflict(s) detected",
            toolResult = if (conflicts.isEmpty()) "CLEAN" else conflicts.joinToString { it.type.name },
            status = if (conflicts.isEmpty()) AgentTaskStatus.DONE else AgentTaskStatus.CONFLICT_FOUND)

        // ── WAVE 2: Insight + Forecast (parallel, depend on Wave 1) ──────────
        log(sessionTrace, AgentName.INSIGHT,
            "Analysing spending patterns", status = AgentTaskStatus.RUNNING)
        log(sessionTrace, AgentName.FORECAST,
            "Forecasting upcoming transactions", status = AgentTaskStatus.RUNNING)

        val allTxns = existingTxns + normalizedTxns

        val insightDeferred  = async { insightAgent.run(allTxns, sessionTrace) }
        val forecastDeferred = async { forecastAgent.run(allTxns, sessionTrace) }

        val insights   = insightDeferred.await()
        val forecasts  = forecastDeferred.await()

        log(sessionTrace, AgentName.INSIGHT,
            "${insights.size} insight(s) generated",
            toolResult = insights.joinToString { "L${it.level.name.last()} ${it.category}" },
            status = AgentTaskStatus.DONE)

        log(sessionTrace, AgentName.FORECAST,
            "${forecasts.size} forecast(s) generated",
            toolResult = forecasts.joinToString { it.type.name },
            status = AgentTaskStatus.DONE)

        // ── WAVE 3: Action (depends on Insight + Forecast) ───────────────────
        log(sessionTrace, AgentName.ACTION,
            "Generating action recommendations", status = AgentTaskStatus.RUNNING)

        val simulations = actionAgent.run(insights, forecasts, existingTxns, sessionTrace)

        log(sessionTrace, AgentName.ACTION,
            "${simulations.size} action(s) ready",
            toolResult = simulations.joinToString { it.action.type.name },
            status = AgentTaskStatus.DONE)

        // ── Complete trace ────────────────────────────────────────────────────
        sessionTrace.complete()
        _trace.value = sessionTrace

        OrchestratorResult(
            normalizedTransactions = normalizedTxns,
            conflicts              = conflicts,
            insights               = insights,
            simulations            = simulations,
            forecasts              = forecasts,
            trace                  = sessionTrace,
        )
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun newTrace() = AgentTrace(
        sessionId   = UUID.randomUUID().toString(),
        startedAtMs = System.currentTimeMillis(),
    )

    private fun log(
        trace: AgentTrace,
        agent: AgentName,
        taskName: String,
        detail: String = "",
        toolCall: String? = null,
        toolResult: String? = null,
        status: AgentTaskStatus = AgentTaskStatus.RUNNING,
    ) {
        trace.addStep(
            AgentTraceStep(
                agentName   = agent,
                taskName    = taskName,
                detail      = detail,
                status      = status,
                timestampMs = System.currentTimeMillis(),
                toolCall    = toolCall,
                toolResult  = toolResult,
            )
        )
        // Emit updated trace so UI reacts immediately
        _trace.value = trace.copy(steps = trace.steps.toMutableList())
    }

    private fun List<ParsedTransaction>.averageConfidence(): String =
        if (isEmpty()) "N/A"
        else "%.2f".format(sumOf { it.confidenceScore.toDouble() } / size)
}

/**
 * The complete output of one pipeline run.
 */
data class OrchestratorResult(
    val normalizedTransactions: List<ParsedTransaction>,
    val conflicts: List<ConflictResult>,
    val insights: List<Insight>,
    val simulations: List<SimulationResult>,
    val forecasts: List<Forecast>,
    val trace: AgentTrace,
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
    val topInsight: Insight? get() = insights.maxByOrNull { it.level.ordinal }
    val nextForecast: Forecast? get() = forecasts.minByOrNull { it.expectedDate.toEpochDay() }
}
