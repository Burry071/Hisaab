package com.hisaab.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

import com.hisaab.data.local.TransactionDao
import com.hisaab.data.local.TransactionEntity

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(
        val totalBalance        : BigDecimal,
        val deltaPercent        : Double,
        val institutions        : List<String>,
        val insights            : List<Insight>,
        val recentTransactions  : List<ParsedTransaction>,
        val conflicts           : List<ConflictResult>,
        val forecasts           : List<com.hisaab.domain.model.Forecast> = emptyList(),
        val processedCount      : Int,
        val lastAgentRunMs      : Long?,
        val agentRunning        : Boolean,
    ) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val llmService: com.hisaab.domain.llm.CachedLlmService,
    private val agentService: com.hisaab.data.agent.HisaabAgentService,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _uiState   = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isPrivate = MutableStateFlow(false)
    val isPrivate: StateFlow<Boolean> = _isPrivate.asStateFlow()

    private val _simulatorResult = MutableStateFlow<com.hisaab.data.agent.HisaabAgentResponse?>(null)
    val simulatorResult: StateFlow<com.hisaab.data.agent.HisaabAgentResponse?> = _simulatorResult.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val dbEntities = transactionDao.getAllTransactions()
                var balance = BigDecimal.ZERO
                val parsedTxns = dbEntities.map { entity ->
                    val txType = when (entity.type) {
                        "INCOME" -> com.hisaab.parser.model.TransactionType.CREDIT
                        "EXPENSE" -> com.hisaab.parser.model.TransactionType.DEBIT
                        "TRANSFER" -> com.hisaab.parser.model.TransactionType.TRANSFER
                        else -> com.hisaab.parser.model.TransactionType.UNKNOWN
                    }
                    val amt = BigDecimal(entity.amount)
                    when (txType) {
                        com.hisaab.parser.model.TransactionType.CREDIT -> balance = balance.add(amt)
                        com.hisaab.parser.model.TransactionType.DEBIT, com.hisaab.parser.model.TransactionType.TRANSFER -> balance = balance.subtract(amt)
                        else -> {}
                    }

                    ParsedTransaction(
                        id = entity.id.toString(),
                        source = com.hisaab.parser.model.IngestionSource.MANUAL,
                        institution = "Manual",
                        type = txType,
                        amount = amt,
                        currency = "PKR",
                        balanceAfter = null,
                        counterparty = entity.merchantName,
                        referenceNumber = null,
                        rawSmsBody = entity.note ?: "",
                        timestampEpochMs = entity.timestampMs,
                        confidenceScore = 1.0f
                    )
                }

                _uiState.value = HomeUiState.Success(
                    totalBalance       = balance,
                    deltaPercent       = 8.2, // standard demo metric
                    institutions       = parsedTxns.map { it.institution }.distinct(),
                    insights           = emptyList(),
                    recentTransactions = parsedTxns,
                    conflicts          = emptyList(),
                    forecasts          = emptyList(),
                    processedCount     = parsedTxns.size,
                    lastAgentRunMs     = System.currentTimeMillis(),
                    agentRunning       = false,
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    fun togglePrivacy() {
        _isPrivate.value = !_isPrivate.value
    }

    /** Called by AgentViewModel when a pipeline run completes. */
    fun onPipelineComplete(
        insights    : List<Insight>,
        transactions: List<ParsedTransaction>,
        conflicts   : List<ConflictResult>,
    ) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = current.copy(
            insights           = insights,
            recentTransactions = transactions,
            conflicts          = conflicts,
            processedCount     = transactions.size,
            lastAgentRunMs     = System.currentTimeMillis(),
            agentRunning       = false,
        )
    }

    /** Shortcut for pipeline result that includes forecasts. */
    fun onPipelineCompleteWithForecasts(
        insights    : List<Insight>,
        transactions: List<ParsedTransaction>,
        conflicts   : List<ConflictResult>,
        forecasts   : List<com.hisaab.domain.model.Forecast>,
    ) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = current.copy(
            insights           = insights,
            recentTransactions = transactions,
            conflicts          = conflicts,
            forecasts          = forecasts,
            processedCount     = transactions.size,
            lastAgentRunMs     = System.currentTimeMillis(),
            agentRunning       = false,
        )
    }

    /** Remove a resolved conflict from the dashboard banner. */
    fun resolveConflict(conflictId: String, accepted: Boolean) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = current.copy(
            conflicts = current.conflicts.filterNot { it.incomingId == conflictId }
        )
    }

    /** Apply a simulation result — updates balance, clears conflicts, and clears agent-running flag. */
    fun applySimulation(result: com.hisaab.domain.model.SimulationResult) {
        val current = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = current.copy(
            totalBalance = result.stateAfter.totalBalance,
            conflicts    = emptyList(),
            agentRunning = false,
        )
    }

    /** Seed demo dataset for hackathon presentation via DemoModeManager long-press. */
    fun loadDemoBundle(bundle: com.hisaab.domain.demo.DemoBundle) {
        _uiState.value = HomeUiState.Success(
            totalBalance       = bundle.totalBalance,
            deltaPercent       = 8.2,            // "up 8.2% this month" for the demo
            institutions       = bundle.institutions,
            insights           = bundle.insights,
            recentTransactions = bundle.transactions.sortedByDescending { it.timestampEpochMs },
            conflicts          = bundle.conflicts,
            forecasts          = bundle.forecasts,
            processedCount     = bundle.transactions.size,
            lastAgentRunMs     = System.currentTimeMillis(),
            agentRunning       = false,
        )
    }

    suspend fun simulateBudget(query: String): String {
        return try {
            // In a real app, we'd pull this from a repository
            val financialContext = com.hisaab.data.agent.FinancialContext(
                totalBalance = 45000.0,
                monthlyIncome = 150000.0,
                monthlyExpenses = 95000.0,
                monthlySavings = 10000.0
            )
            val result = agentService.runSimulator(query, financialContext)
            _simulatorResult.value = result
            result.actionableRecommendation
        } catch (e: Exception) {
            "Simulation failed. Please try again."
        }
    }
}
