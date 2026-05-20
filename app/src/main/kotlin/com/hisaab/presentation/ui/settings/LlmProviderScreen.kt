package com.hisaab.presentation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.llm.GeminiModel
import com.hisaab.domain.llm.LlmProvider
import com.hisaab.domain.llm.LlmProviderConfig
import com.hisaab.presentation.viewmodels.SettingsViewModel
import com.hisaab.presentation.viewmodels.VerifyUiState

// ── Hisaab Design Tokens ──────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFF0D0D14)
private val BgSurface    = Color(0xFF1C1C2E)
private val BgElevated   = Color(0xFF252538)
private val AccentPurple = Color(0xFF7B61FF)
private val AccentTeal   = Color(0xFF00D4AA)
private val AccentAmber  = Color(0xFFFFB830)
private val AccentRed    = Color(0xFFFF4B6E)
private val TextPrimary  = Color(0xFFFFFFFF)
private val TextSecondary= Color(0xFFA0A0C0)
private val TextMuted    = Color(0xFF60607A)

/**
 * LLM Provider Settings Screen.
 *
 * Three provider cards:
 *  1. Gemini     — API key + model picker
 *  2. OpenAI-compatible — API key + base URL + model
 *  3. Ollama     — base URL + model + Termux setup guide
 *
 * Each card expands on selection to show its configuration fields.
 * "Test Connection" fires verify() and shows a live latency badge.
 */
@Composable
fun LlmProviderScreen(
    viewModel: SettingsViewModel,
) {
    val activeConfig by viewModel.activeConfig.collectAsState()
    val draft        by viewModel.draft.collectAsState()
    val verifyState  by viewModel.verifyState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text  = "AI Model",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.SansSerif,
        )
        Text(
            text  = "Choose how Hisaab reasons about your finances.\nYour data, your choice.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )

        Spacer(Modifier.height(4.dp))

        // ── Provider Cards ────────────────────────────────────────────────────
        ProviderCard(
            title       = "Google Gemini",
            subtitle    = "Fast cloud inference. Best quality.",
            badge       = "RECOMMENDED",
            badgeColor  = AccentPurple,
            privacyNote = "Data sent to Google servers",
            icon        = Icons.Default.AutoAwesome,
            isSelected  = draft is LlmProvider.GeminiProvider,
            isActive    = activeConfig.provider is LlmProvider.GeminiProvider,
            onSelect    = { viewModel.selectProvider(LlmProvider.GeminiProvider()) },
        ) {
            GeminiConfigPanel(
                config      = draft as? LlmProvider.GeminiProvider ?: LlmProvider.GeminiProvider(),
                onKeyChange = viewModel::updateGeminiApiKey,
                onModelChange = viewModel::updateGeminiModel,
            )
        }

        ProviderCard(
            title       = "OpenAI / Custom Endpoint",
            subtitle    = "OpenAI, Groq, Together, or your own vLLM.",
            badge       = null,
            badgeColor  = AccentAmber,
            privacyNote = "Data sent to selected endpoint",
            icon        = Icons.Default.Cloud,
            isSelected  = draft is LlmProvider.OpenAICompatibleProvider,
            isActive    = activeConfig.provider is LlmProvider.OpenAICompatibleProvider,
            onSelect    = {
                viewModel.selectProvider(
                    LlmProvider.OpenAICompatibleProvider(apiKey = "", baseUrl = "https://api.openai.com/v1")
                )
            },
        ) {
            OpenAIConfigPanel(
                config          = draft as? LlmProvider.OpenAICompatibleProvider
                    ?: LlmProvider.OpenAICompatibleProvider(apiKey = ""),
                onKeyChange     = viewModel::updateOpenAIKey,
                onBaseUrlChange = viewModel::updateOpenAIBaseUrl,
                onModelChange   = viewModel::updateOpenAIModel,
            )
        }

        ProviderCard(
            title       = "Local Model (Ollama)",
            subtitle    = "100% offline. Zero data leaves your phone.",
            badge       = "PRIVATE",
            badgeColor  = AccentTeal,
            privacyNote = "Data never leaves device",
            icon        = Icons.Default.DevicesOther,
            isSelected  = draft is LlmProvider.OllamaProvider,
            isActive    = activeConfig.provider is LlmProvider.OllamaProvider,
            onSelect    = { viewModel.selectProvider(LlmProvider.OllamaProvider()) },
        ) {
            OllamaConfigPanel(
                config          = draft as? LlmProvider.OllamaProvider ?: LlmProvider.OllamaProvider(),
                onUrlChange     = viewModel::updateOllamaBaseUrl,
                onModelChange   = viewModel::updateOllamaModel,
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Test Connection Button ────────────────────────────────────────────
        Button(
            onClick  = viewModel::verifyCurrentProvider,
            enabled  = verifyState !is VerifyUiState.Testing,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape    = RoundedCornerShape(12.dp),
        ) {
            if (verifyState is VerifyUiState.Testing) {
                CircularProgressIndicator(
                    color  = TextPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text("Testing connection…", color = TextPrimary)
            } else {
                Icon(Icons.Default.NetworkCheck, null, tint = TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Test Connection", color = TextPrimary)
            }
        }

        // ── Verify result banner ──────────────────────────────────────────────
        VerifyBanner(state = verifyState)
    }
}

// ── Provider Card ─────────────────────────────────────────────────────────────

@Composable
private fun ProviderCard(
    title: String,
    subtitle: String,
    badge: String?,
    badgeColor: Color,
    privacyNote: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isActive: Boolean,
    onSelect: () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    val borderColor = when {
        isSelected -> AccentPurple
        isActive   -> AccentTeal.copy(alpha = 0.5f)
        else       -> BgElevated
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurface)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
    ) {
        // Card header row
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AccentPurple.copy(alpha = 0.15f) else BgElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AccentPurple else TextMuted,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    if (badge != null) {
                        Spacer(Modifier.width(6.dp))
                        BadgeChip(badge, badgeColor)
                    }
                    if (isActive) {
                        Spacer(Modifier.width(6.dp))
                        BadgeChip("ACTIVE", AccentTeal)
                    }
                }
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = TextMuted,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(privacyNote, color = TextMuted, fontSize = 11.sp)
                }
            }

            RadioButton(
                selected = isSelected,
                onClick  = onSelect,
                colors   = RadioButtonDefaults.colors(selectedColor = AccentPurple),
            )
        }

        // Expandable config panel
        AnimatedVisibility(
            visible = isSelected,
            enter   = expandVertically(),
            exit    = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgElevated.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                HorizontalDivider(color = TextMuted.copy(alpha = 0.3f), thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                expandedContent()
            }
        }
    }
}

// ── Config panels ─────────────────────────────────────────────────────────────

@Composable
private fun GeminiConfigPanel(
    config: LlmProvider.GeminiProvider,
    onKeyChange: (String) -> Unit,
    onModelChange: (GeminiModel) -> Unit,
) {
    var showKey by remember { mutableStateOf(false) }

    ConfigSectionLabel("API Key (optional — leave blank to use Hisaab's shared key)")
    OutlinedTextField(
        value         = config.apiKey,
        onValueChange = onKeyChange,
        placeholder   = { Text("AIza…  (your own key gets priority quota)", color = TextMuted, fontSize = 12.sp) },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon  = {
            IconButton(onClick = { showKey = !showKey }) {
                Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(12.dp))
    ConfigSectionLabel("Model")
    GeminiModel.values().forEach { model ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onModelChange(model) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = config.model == model,
                onClick  = { onModelChange(model) },
                colors   = RadioButtonDefaults.colors(selectedColor = AccentPurple),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(model.label, color = TextPrimary, fontSize = 13.sp)
                Text(model.modelId, color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }

    Spacer(Modifier.height(4.dp))
    Text(
        "Get a free key at aistudio.google.com/apikey",
        color = AccentPurple,
        fontSize = 12.sp,
    )
}

@Composable
private fun OpenAIConfigPanel(
    config: LlmProvider.OpenAICompatibleProvider,
    onKeyChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
) {
    var showKey by remember { mutableStateOf(false) }

    ConfigSectionLabel("Base URL")
    OutlinedTextField(
        value         = config.baseUrl,
        onValueChange = onBaseUrlChange,
        placeholder   = { Text("https://api.openai.com/v1", color = TextMuted, fontSize = 12.sp) },
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(10.dp))
    ConfigSectionLabel("API Key")
    OutlinedTextField(
        value         = config.apiKey,
        onValueChange = onKeyChange,
        placeholder   = { Text("sk-…", color = TextMuted, fontSize = 12.sp) },
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon  = {
            IconButton(onClick = { showKey = !showKey }) {
                Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(10.dp))
    ConfigSectionLabel("Model ID")
    OutlinedTextField(
        value         = config.model,
        onValueChange = onModelChange,
        placeholder   = { Text("gpt-4o-mini  /  llama-3.1-70b-versatile", color = TextMuted, fontSize = 12.sp) },
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(8.dp))
    // Common presets
    Text("Popular endpoints:", color = TextMuted, fontSize = 11.sp)
    listOf(
        "Groq (free, fast)"   to "https://api.groq.com/openai/v1",
        "Together AI"          to "https://api.together.xyz/v1",
        "Local LM Studio"      to "http://localhost:1234/v1",
    ).forEach { (label, url) ->
        TextButton(onClick = { onBaseUrlChange(url) }, contentPadding = PaddingValues(0.dp)) {
            Text("↳ $label", color = AccentPurple, fontSize = 12.sp)
        }
    }
}

@Composable
private fun OllamaConfigPanel(
    config: LlmProvider.OllamaProvider,
    onUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
) {
    ConfigSectionLabel("Ollama Server URL")
    OutlinedTextField(
        value         = config.baseUrl,
        onValueChange = onUrlChange,
        placeholder   = { Text("http://127.0.0.1:11434", color = TextMuted, fontSize = 12.sp) },
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(10.dp))
    ConfigSectionLabel("Model")
    OutlinedTextField(
        value         = config.model,
        onValueChange = onModelChange,
        placeholder   = { Text("phi3:mini", color = TextMuted, fontSize = 12.sp) },
        modifier      = configFieldModifier(),
        colors        = configFieldColors(),
        singleLine    = true,
    )

    Spacer(Modifier.height(12.dp))

    // Termux setup guide
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgPrimary)
            .border(0.5.dp, TextMuted.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text("Termux Setup", color = AccentTeal, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            listOf(
                "pkg install ollama",
                "ollama pull phi3:mini",
                "ollama serve",
            ).forEachIndexed { i, cmd ->
                Text(
                    text     = "${i + 1}. $cmd",
                    color    = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text("Recommended: phi3:mini (3.8B) for 4GB RAM", color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ── Verify Banner ─────────────────────────────────────────────────────────────

@Composable
private fun VerifyBanner(state: VerifyUiState) {
    AnimatedVisibility(visible = state !is VerifyUiState.Idle) {
        val (bgColor, icon, text) = when (state) {
            is VerifyUiState.Testing -> Triple(BgElevated, Icons.Default.HourglassTop, "Testing…")
            is VerifyUiState.Success -> Triple(
                AccentTeal.copy(alpha = 0.15f),
                Icons.Default.CheckCircle,
                "✅ Connected to ${state.modelId} — ${state.latencyMs}ms",
            )
            is VerifyUiState.Failed  -> Triple(
                AccentRed.copy(alpha = 0.15f),
                Icons.Default.Error,
                "❌ ${state.reason}",
            )
            else                     -> return@AnimatedVisibility
        }

        val textColor = when (state) {
            is VerifyUiState.Success -> AccentTeal
            is VerifyUiState.Failed  -> AccentRed
            else                     -> TextSecondary
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(0.5.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = textColor, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────

@Composable
private fun BadgeChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(label, color = color, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun ConfigSectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun configFieldModifier() = Modifier.fillMaxWidth()

@Composable
private fun configFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AccentPurple,
    unfocusedBorderColor = BgElevated,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    cursorColor          = AccentPurple,
    focusedContainerColor   = BgElevated.copy(alpha = 0.5f),
    unfocusedContainerColor = BgElevated.copy(alpha = 0.3f),
)
