package com.hisaab.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaab.domain.agents.InsightAgent
import com.hisaab.domain.model.*
import com.hisaab.domain.llm.LlmService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * InsightsViewModel — supplies Insight list to InsightsScreen.
 *
 * In production: driven by InsightAgent output stored in Room DB.
 * Demo mode: seeds from DemoModeManager demo dataset.
 *
 * Exposes:
 *   insights     : StateFlow<List<Insight>>
 *   isLoading    : StateFlow<Boolean>
 *   refreshInsights() — re-runs InsightAgent or re-seeds demo
 */
@HiltViewModel
class InsightsViewModel @Inject constructor() : ViewModel() {

    private val _insights  = MutableStateFlow<List<Insight>>(emptyList())
    val insights: StateFlow<List<Insight>> = _insights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        seedDemoInsights()
    }

    /** Re-seed or re-run agent (called by pull-to-refresh or AgentOrchestrator callback) */
    fun refreshInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            seedDemoInsights()
            _isLoading.value = false
        }
    }

    /** Called by AgentOrchestrator when InsightAgent completes — updates live insights */
    fun onInsightsGenerated(newInsights: List<Insight>) {
        _insights.value = newInsights.sortedByDescending { it.level.ordinal }
    }

    // ── Demo data ─────────────────────────────────────────────────────────────

    private fun seedDemoInsights() {
        _insights.value = listOf(
            Insight(
                id          = UUID.randomUUID().toString(),
                level       = InsightLevel.LEVEL_3,
                category    = InsightCategory.FOOD,
                headline    = "Food spending 39% above average",
                reasoning   = """3-month average: PKR 13,200
That's 39% above normal.
This is the 3rd consecutive month of increase.
At this pace: budget exhausted in 6 days.
Last 3 months show end-of-month spikes.
Recommended: cut daily spend by PKR 300.""",
                actionPrompt = "Budget exhausted in 6 days → Cut PKR 300/day",
                confidence  = 0.91f,
                generatedAtMs = System.currentTimeMillis(),
            ),
            Insight(
                id          = UUID.randomUUID().toString(),
                level       = InsightLevel.LEVEL_2,
                category    = InsightCategory.UTILITIES,
                headline    = "Utility bill expected in 2 days",
                reasoning   = """Pattern: LESCO bill arrives on 15th–17th every month.
Last 3 months: PKR 3,200 / PKR 3,450 / PKR 3,100.
Forecast range: PKR 3,400–3,800.
Confidence: 84%.""",
                actionPrompt = "Set aside PKR 3,600 before the 17th",
                confidence  = 0.84f,
                generatedAtMs = System.currentTimeMillis(),
            ),
            Insight(
                id          = UUID.randomUUID().toString(),
                level       = InsightLevel.LEVEL_1,
                category    = InsightCategory.SALARY,
                headline    = "Salary received: PKR 80,000",
                reasoning   = """HBL credit: PKR 80,000 from PAYROLL.
Consistent with last 3 months (±2%).
No anomalies detected.""",
                actionPrompt = null,
                confidence  = 0.99f,
                generatedAtMs = System.currentTimeMillis(),
            ),
        )
    }
}
