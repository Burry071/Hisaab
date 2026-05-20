package com.hisaab.data.llm

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hisaab.domain.llm.GeminiModel
import com.hisaab.domain.llm.LlmProvider
import com.hisaab.domain.llm.LlmProviderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Persists and reads the user's chosen LLM provider securely.
 *
 * Uses EncryptedSharedPreferences to prevent plaintext extraction of API keys
 * from rooted devices or adb backups.
 */
class LlmProviderRepository(private val context: Context) {

    companion object {
        private const val PREFS_FILENAME = "hisaab_secure_llm_prefs"
        private const val KEY_PROVIDER_JSON = "active_provider"
    }

    private val sharedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails (e.g. KeyStore corrupted)
            // Better to be functional with less security than to crash at startup.
            context.getSharedPreferences(PREFS_FILENAME + "_fallback", Context.MODE_PRIVATE)
        }
    }

    private val _activeProviderConfig = MutableStateFlow(readFromPrefs())

    /** Observable: emits whenever the user changes their provider in Settings. */
    val activeProviderConfig: Flow<LlmProviderConfig> = _activeProviderConfig.asStateFlow()

    /** Synchronous access to the current provider config. */
    val currentConfig: LlmProviderConfig get() = _activeProviderConfig.value

    suspend fun save(config: LlmProviderConfig) {
        sharedPrefs.edit().putString(KEY_PROVIDER_JSON, serialize(config)).apply()
        _activeProviderConfig.value = config
    }

    suspend fun getActiveProvider(): LlmProvider {
        return readFromPrefs().provider
    }

    private fun readFromPrefs(): LlmProviderConfig {
        val json = sharedPrefs.getString(KEY_PROVIDER_JSON, null)
        return if (json.isNullOrBlank()) LlmProviderConfig.Default
        else deserialize(json)
    }

    // ── Serialization (manual JSON — no Gson/Moshi to keep APK lean) ──────────

    private fun serialize(config: LlmProviderConfig): String {
        val p = config.provider
        val pObj = when (p) {
            is LlmProvider.GeminiProvider -> JSONObject().apply {
                put("type", "gemini")
                put("apiKey", p.apiKey)
                put("model", p.model.name)
            }
            is LlmProvider.OpenAICompatibleProvider -> JSONObject().apply {
                put("type", "openai")
                put("apiKey", p.apiKey)
                put("baseUrl", p.baseUrl)
                put("model", p.model)
            }
            is LlmProvider.OllamaProvider -> JSONObject().apply {
                put("type", "ollama")
                put("baseUrl", p.baseUrl)
                put("model", p.model)
            }
        }
        return JSONObject().apply {
            put("provider", pObj)
            put("verified", config.isVerified)
            put("verifiedAt", config.lastVerifiedAtMs ?: JSONObject.NULL)
        }.toString()
    }

    private fun deserialize(json: String): LlmProviderConfig {
        val root  = JSONObject(json)
        val pObj  = root.getJSONObject("provider")
        val provider = when (val type = pObj.getString("type")) {
            "gemini" -> LlmProvider.GeminiProvider(
                apiKey = pObj.optString("apiKey", ""),
                model  = try {
                    GeminiModel.valueOf(pObj.optString("model", GeminiModel.FLASH.name))
                } catch (e: Exception) {
                    GeminiModel.FLASH
                },
            )
            "openai" -> LlmProvider.OpenAICompatibleProvider(
                apiKey  = pObj.getString("apiKey"),
                baseUrl = pObj.optString("baseUrl", "https://api.openai.com/v1"),
                model   = pObj.optString("model", "gpt-4o-mini"),
            )
            "ollama" -> LlmProvider.OllamaProvider(
                baseUrl = pObj.optString("baseUrl", "http://127.0.0.1:11434"),
                model   = pObj.optString("model", "phi3:mini"),
            )
            else -> throw IllegalArgumentException("Unknown provider type: $type")
        }
        return LlmProviderConfig(
            provider         = provider,
            isVerified       = root.optBoolean("verified", false),
            lastVerifiedAtMs = if (root.isNull("verifiedAt")) null else root.getLong("verifiedAt"),
        )
    }
}
