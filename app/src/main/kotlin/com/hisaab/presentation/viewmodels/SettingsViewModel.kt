package com.hisaab.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaab.data.llm.LlmProviderRepository
import com.hisaab.data.llm.LlmServiceFactory
import com.hisaab.domain.llm.GeminiModel
import com.hisaab.domain.llm.LlmProvider
import com.hisaab.domain.llm.LlmProviderConfig
import com.hisaab.domain.llm.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * SettingsViewModel — drives the LLM provider picker UI.
 *
 * Compose usage example:
 *
 *   val config by viewModel.activeConfig.collectAsState()
 *   val verifyState by viewModel.verifyState.collectAsState()
 *
 *   LlmProviderPicker(
 *     config        = config,
 *     onSelect      = viewModel::selectProvider,
 *     onVerify      = viewModel::verifyCurrentProvider,
 *     verifyState   = verifyState,
 *   )
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: LlmProviderRepository,
) : ViewModel() {

    // ── Active config (live from DataStore) ───────────────────────────────────
    val activeConfig: StateFlow<LlmProviderConfig> = repo.activeProviderConfig.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = LlmProviderConfig.Default,
    )

    // ── Verify state ──────────────────────────────────────────────────────────
    private val _verifyState = MutableStateFlow<VerifyUiState>(VerifyUiState.Idle)
    val verifyState: StateFlow<VerifyUiState> = _verifyState.asStateFlow()

    // ── Draft provider (what the user is currently editing, before saving) ────
    private val _draft = MutableStateFlow<LlmProvider>(LlmProvider.GeminiProvider())
    val draft: StateFlow<LlmProvider> = _draft.asStateFlow()

    // ── Public actions ────────────────────────────────────────────────────────

    /** Called when user taps a provider card in the picker */
    fun selectProvider(provider: LlmProvider) {
        _draft.value       = provider
        _verifyState.value = VerifyUiState.Idle
    }

    /** Called when user taps "Test Connection" */
    fun verifyCurrentProvider() {
        val provider = _draft.value
        viewModelScope.launch {
            _verifyState.value = VerifyUiState.Testing

            val service = LlmServiceFactory.create(provider)
            val result  = service.verify()

            _verifyState.value = when (result) {
                is VerificationResult.Success -> {
                    // Auto-save on success
                    val config = LlmProviderConfig(
                        provider         = provider,
                        isVerified       = true,
                        lastVerifiedAtMs = System.currentTimeMillis(),
                    )
                    repo.save(config)
                    VerifyUiState.Success(
                        modelId   = result.modelId,
                        latencyMs = result.latencyMs,
                    )
                }
                is VerificationResult.Failure -> VerifyUiState.Failed(result.reason)
            }
        }
    }

    /** Called when user edits Gemini API key field */
    fun updateGeminiApiKey(key: String) {
        val current = _draft.value
        if (current is LlmProvider.GeminiProvider) {
            _draft.value = current.copy(apiKey = key)
        }
    }

    /** Called when user edits Gemini model dropdown */
    fun updateGeminiModel(model: GeminiModel) {
        val current = _draft.value
        if (current is LlmProvider.GeminiProvider) {
            _draft.value = current.copy(model = model)
        }
    }

    /** Called when user edits OpenAI endpoint URL */
    fun updateOpenAIBaseUrl(url: String) {
        val current = _draft.value
        if (current is LlmProvider.OpenAICompatibleProvider) {
            _draft.value = current.copy(baseUrl = url)
        }
    }

    /** Called when user edits OpenAI API key */
    fun updateOpenAIKey(key: String) {
        val current = _draft.value
        if (current is LlmProvider.OpenAICompatibleProvider) {
            _draft.value = current.copy(apiKey = key)
        }
    }

    /** Called when user edits OpenAI model name */
    fun updateOpenAIModel(model: String) {
        val current = _draft.value
        if (current is LlmProvider.OpenAICompatibleProvider) {
            _draft.value = current.copy(model = model)
        }
    }

    /** Called when user edits Ollama URL */
    fun updateOllamaBaseUrl(url: String) {
        val current = _draft.value
        if (current is LlmProvider.OllamaProvider) {
            _draft.value = current.copy(baseUrl = url)
        }
    }

    /** Called when user edits Ollama model name (e.g., "phi3:mini") */
    fun updateOllamaModel(model: String) {
        val current = _draft.value
        if (current is LlmProvider.OllamaProvider) {
            _draft.value = current.copy(model = model)
        }
    }
}

// ── Verify UI state ───────────────────────────────────────────────────────────

sealed class VerifyUiState {
    object Idle    : VerifyUiState()
    object Testing : VerifyUiState()
    data class Success(val modelId: String, val latencyMs: Long) : VerifyUiState()
    data class Failed(val reason: String) : VerifyUiState()
}
