package com.hisaab.domain.llm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors LLM provider health in the background.
 *
 * Runs a lightweight verify() ping every [pingIntervalMs].
 * Exposes [healthState] as a StateFlow — consumed by:
 *   • SettingsScreen status badge
 *   • AgentTraceView provider indicator
 *   • AgentOrchestrator (skip LLM calls if UNHEALTHY)
 *
 * Auto-recovers: when a previously UNHEALTHY provider passes verify(), 
 * state transitions back to HEALTHY automatically.
 */
@Singleton
class LlmHealthMonitor @Inject constructor(
    private val llmService: CachedLlmService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _healthState = MutableStateFlow<LlmHealthState>(LlmHealthState.Unknown)
    val healthState: StateFlow<LlmHealthState> = _healthState.asStateFlow()

    /** Last successful verify latency in ms */
    private val _lastLatencyMs = MutableStateFlow<Long?>(null)
    val lastLatencyMs: StateFlow<Long?> = _lastLatencyMs.asStateFlow()

    /**
     * Start background health pings.
     * Call from Application.onCreate() or MainActivity.onResume().
     */
    fun startMonitoring(pingIntervalMs: Long = 5 * 60 * 1000) {
        scope.launch {
            while (true) {
                ping()
                delay(pingIntervalMs)
            }
        }
    }

    /** Single on-demand ping — call after user changes provider in Settings. */
    suspend fun pingOnce() = ping()

    private suspend fun ping() {
        _healthState.value = LlmHealthState.Checking
        when (val result = llmService.verify()) {
            is VerificationResult.Success -> {
                _healthState.value  = LlmHealthState.Healthy(result.modelId)
                _lastLatencyMs.value = result.latencyMs
            }
            is VerificationResult.Failure -> {
                _healthState.value = LlmHealthState.Unhealthy(result.reason)
            }
        }
    }
}

sealed class LlmHealthState {
    object Unknown  : LlmHealthState()
    object Checking : LlmHealthState()
    data class Healthy(val modelId: String) : LlmHealthState()
    data class Unhealthy(val reason: String) : LlmHealthState()

    val isHealthy: Boolean get() = this is Healthy
    val label: String get() = when (this) {
        is Unknown   -> "Not checked"
        is Checking  -> "Checking…"
        is Healthy   -> "Online · $modelId"
        is Unhealthy -> "Offline"
    }
}
