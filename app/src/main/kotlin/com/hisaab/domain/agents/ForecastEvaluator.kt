package com.hisaab.domain.agents

import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * ForecastEvaluator — implements "Evaluates" and "Adapts" verbs (PRD Decision Log, Grill Q3).
 *
 * Flow:
 *   1. ForecastAgent stores each prediction (type, expectedAmount, expectedDate) in Room DB.
 *   2. When actual transaction arrives matching a forecast, evaluatePrediction() is called.
 *   3. Computes predictionError (actual vs predicted amount/date).
 *   4. Logs a confidence delta: +ve if accurate, -ve if off.
 *   5. If sustained errors > 20%, adaptLookbackWindow() shifts from 3-month to 6-month window.
 *   6. All evaluations logged in AgentTrace so judges can see it.
 *
 * This is surfaced in AgentTraceView under ForecastAgent steps.
 */
class ForecastEvaluator {

    // ── Evaluation state ──────────────────────────────────────────────────────

    private val _evaluations = MutableStateFlow<List<ForecastEvaluation>>(emptyList())
    val evaluations: StateFlow<List<ForecastEvaluation>> = _evaluations.asStateFlow()

    /** Current lookback window (months). Adapts from 3 to 6 if sustained errors > 20% */
    private val _lookbackMonths = MutableStateFlow(DEFAULT_LOOKBACK_MONTHS)
    val lookbackMonths: StateFlow<Int> = _lookbackMonths.asStateFlow()

    /** Running accuracy rate (0.0–1.0) */
    private val _accuracy = MutableStateFlow(1.0f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    // ── Core API ──────────────────────────────────────────────────────────────

    /**
     * Called when an actual transaction arrives that matches a pending forecast.
     * Computes prediction error and logs the delta.
     *
     * @param forecast    The original ForecastAgent prediction
     * @param actual      The actual ParsedTransaction that arrived
     * @param traceLogger Callback to append an AgentTraceStep (visible in AgentTraceView)
     */
    fun evaluatePrediction(
        forecast    : Forecast,
        actual      : ParsedTransaction,
        traceLogger : (AgentTraceStep) -> Unit,
    ) {
        val actualAmount = actual.amount
        val forecastMid  = (forecast.estimatedAmountMin + forecast.estimatedAmountMax)
            .divide(BigDecimal("2"), 2, RoundingMode.HALF_UP)

        // Amount error (percentage)
        val amountError = if (forecastMid > BigDecimal.ZERO)
            ((actualAmount - forecastMid).abs() / forecastMid * BigDecimal("100"))
                .toDouble()
        else 0.0

        // Date error (days off)
        val today      = LocalDate.now()
        val dateError  = ChronoUnit.DAYS.between(forecast.expectedDate, today).toInt()

        // Confidence delta: decrease for large errors, increase for accurate predictions
        val confidenceDelta = when {
            amountError < 5.0  && kotlin.math.abs(dateError) <= 1 -> +0.05f
            amountError < 15.0 && kotlin.math.abs(dateError) <= 2 -> +0.02f
            amountError < 25.0                                     -> -0.03f
            else                                                   -> -0.08f
        }

        val evaluation = ForecastEvaluation(
            id               = UUID.randomUUID().toString(),
            forecastId       = forecast.id,
            forecastType     = forecast.type,
            predictedAmount  = forecastMid,
            actualAmount     = actualAmount,
            amountErrorPct   = amountError,
            dateDeltaDays    = dateError,
            confidenceDelta  = confidenceDelta,
            evaluatedAtMs    = System.currentTimeMillis(),
        )

        val updated = _evaluations.value + evaluation
        _evaluations.value = updated

        // Recompute running accuracy
        recomputeAccuracy(updated)

        // Log to agent trace (visible in judge screen)
        traceLogger(
            AgentTraceStep(
                agentName  = AgentName.FORECAST,
                taskName   = "evaluate_prediction",
                detail     = buildTraceDetail(evaluation),
                status     = if (amountError < 25.0) AgentTaskStatus.DONE else AgentTaskStatus.CONFLICT_FOUND,
                timestampMs = System.currentTimeMillis(),
                toolCall   = "forecast_evaluator(type=${forecast.type})",
                toolResult = "error=${String.format("%.1f", amountError)}% | delta=${String.format("%+.2f", confidenceDelta)}",
            )
        )

        // Adapt lookback window if needed
        adaptLookbackWindow(updated, traceLogger)
    }

    // ── Internal logic ────────────────────────────────────────────────────────

    private fun recomputeAccuracy(evals: List<ForecastEvaluation>) {
        if (evals.isEmpty()) return
        val recent = evals.takeLast(ACCURACY_WINDOW)
        val accurate = recent.count { it.amountErrorPct < ERROR_THRESHOLD_PCT }
        _accuracy.value = accurate.toFloat() / recent.size.toFloat()
    }

    /**
     * Adapt: if sustained prediction errors > 20% over the last ADAPT_WINDOW evaluations,
     * shift lookback from 3 months to 6 months. Revert to 3 months if accuracy recovers.
     */
    private fun adaptLookbackWindow(
        evals       : List<ForecastEvaluation>,
        traceLogger : (AgentTraceStep) -> Unit,
    ) {
        if (evals.size < ADAPT_WINDOW) return

        val recent       = evals.takeLast(ADAPT_WINDOW)
        val highErrorRate = recent.count { it.amountErrorPct > ERROR_THRESHOLD_PCT }
            .toFloat() / ADAPT_WINDOW.toFloat()

        val currentWindow  = _lookbackMonths.value
        val targetWindow   = if (highErrorRate > ADAPT_TRIGGER_RATE) EXTENDED_LOOKBACK_MONTHS
                            else DEFAULT_LOOKBACK_MONTHS

        if (targetWindow != currentWindow) {
            _lookbackMonths.value = targetWindow

            val action = if (targetWindow > currentWindow)
                "EXTENDED (3m→6m): ${String.format("%.0f", highErrorRate * 100)}% recent errors > $ERROR_THRESHOLD_PCT%"
            else
                "RESET (6m→3m): accuracy recovered to ${String.format("%.0f", (1f - highErrorRate) * 100)}%"

            traceLogger(
                AgentTraceStep(
                    agentName   = AgentName.FORECAST,
                    taskName    = "adapt_lookback_window",
                    detail      = "▲ ADAPT: lookback window $action",
                    status      = AgentTaskStatus.DONE,
                    timestampMs = System.currentTimeMillis(),
                    toolCall    = "forecast_evaluator.adapt()",
                    toolResult  = "lookback=${targetWindow}m",
                )
            )
        }
    }

    private fun buildTraceDetail(e: ForecastEvaluation) = buildString {
        append("${e.forecastType} · ")
        append("predicted=PKR ${"%,d".format(e.predictedAmount.toLong())} ")
        append("actual=PKR ${"%,d".format(e.actualAmount.toLong())} ")
        append("error=${String.format("%.1f", e.amountErrorPct)}% ")
        append("delta=${String.format("%+.2f", e.confidenceDelta)}")
    }

    companion object {
        private const val DEFAULT_LOOKBACK_MONTHS  = 3
        private const val EXTENDED_LOOKBACK_MONTHS = 6
        private const val ACCURACY_WINDOW          = 10  // evaluate over last 10 predictions
        private const val ADAPT_WINDOW             = 5   // check adaptation over last 5
        private const val ERROR_THRESHOLD_PCT      = 20.0
        private const val ADAPT_TRIGGER_RATE       = 0.60f  // 60% error rate triggers adapt
    }
}

// ── Evaluation data model ─────────────────────────────────────────────────────

data class ForecastEvaluation(
    val id              : String,
    val forecastId      : String,
    val forecastType    : ForecastType,
    val predictedAmount : BigDecimal,
    val actualAmount    : BigDecimal,
    val amountErrorPct  : Double,     // % deviation
    val dateDeltaDays   : Int,        // actual - predicted days (+ = late, - = early)
    val confidenceDelta : Float,      // applied to next forecast confidence
    val evaluatedAtMs   : Long,
)
