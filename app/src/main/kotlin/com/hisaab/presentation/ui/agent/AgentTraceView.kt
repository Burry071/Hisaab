package com.hisaab.presentation.ui.agent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.model.AgentName
import com.hisaab.domain.model.AgentTaskStatus
import com.hisaab.domain.model.AgentTrace
import com.hisaab.domain.model.AgentTraceStep
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * AgentTraceView — the reusable terminal-style trace tree component.
 *
 * Renders the live [AgentTrace] as a JetBrains Mono tree:
 *
 *   ✅ IngestionAgent
 *   ├─ parse_transaction(sender=HBL)
 *   │   └─ DEBIT PKR 5,000  confidence=0.92
 *   ⚠️ ContradictionAgent
 *   ├─ detect_contradiction(id=a3f4b2…)
 *   │   └─ AMOUNT_MISMATCH severity=HIGH
 *   ⏳ InsightAgent  ← animated cursor when running
 *
 * Design rules from DESIGN.md:
 *   • JetBrains Mono for ALL text in this component
 *   • tree prefixes (├─, └─, │ ) in TextMuted
 *   • ✅ teal · ⚠️ amber · ❌ red · ⏳ muted
 *   • LIVE pill: teal fill, pulsing opacity
 *   • No shadows, no cards — bare #13131F background
 */
@Composable
fun AgentTraceView(
    trace: AgentTrace,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(HisaabTheme.BgSecondary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Group steps by agent name to render tree structure
        val grouped = trace.steps.groupBy { it.agentName }

        AgentName.values().forEach { agentName ->
            val steps = grouped[agentName]
            if (steps.isNullOrEmpty()) return@forEach

            val agentStatus = resolveAgentStatus(steps)
            val isRunning   = agentStatus == AgentTaskStatus.RUNNING

            // ── Agent header row ───────────────────────────────────────────
            AgentHeaderRow(
                agentName = agentName,
                status    = agentStatus,
                isLive    = isRunning && trace.isLive,
            )

            Spacer(Modifier.height(2.dp))

            // ── Step rows (tool calls + results) ──────────────────────────
            steps.forEachIndexed { index, step ->
                val isLast = index == steps.lastIndex
                val prefix = if (isLast) "└─" else "├─"

                // Tool call line
                if (step.toolCall != null) {
                    TraceTextRow(
                        treePrefix = "$prefix ",
                        content    = step.toolCall,
                        contentColor = HisaabTheme.TextSecondary,
                    )
                }

                // Task detail line (always show)
                TraceTextRow(
                    treePrefix   = if (step.toolCall != null) "│   " else "$prefix ",
                    content      = step.detail,
                    contentColor = when (step.status) {
                        AgentTaskStatus.DONE           -> HisaabTheme.Teal
                        AgentTaskStatus.CONFLICT_FOUND -> HisaabTheme.Amber
                        AgentTaskStatus.FAILED         -> HisaabTheme.Red
                        AgentTaskStatus.RUNNING        -> HisaabTheme.TextSecondary
                        else                           -> HisaabTheme.TextMuted
                    },
                    animated = step.status == AgentTaskStatus.RUNNING,
                )

                // Tool result line
                if (step.toolResult != null) {
                    TraceTextRow(
                        treePrefix   = "│   └─ ",
                        content      = step.toolResult,
                        contentColor = resultColor(step.toolResult),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Agent header row ───────────────────────────────────────────────────────────

@Composable
private fun AgentHeaderRow(
    agentName: AgentName,
    status: AgentTaskStatus,
    isLive: Boolean,
) {
    val statusIcon = when (status) {
        AgentTaskStatus.DONE           -> "✅"
        AgentTaskStatus.CONFLICT_FOUND -> "⚠️"
        AgentTaskStatus.FAILED         -> "❌"
        AgentTaskStatus.RUNNING        -> "⏳"
        AgentTaskStatus.PENDING        -> "○"
    }
    val headerColor = when (status) {
        AgentTaskStatus.DONE           -> HisaabTheme.Teal
        AgentTaskStatus.CONFLICT_FOUND -> HisaabTheme.Amber
        AgentTaskStatus.FAILED         -> HisaabTheme.Red
        AgentTaskStatus.RUNNING        -> HisaabTheme.Purple
        AgentTaskStatus.PENDING        -> HisaabTheme.TextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text  = "$statusIcon ${agentName.name.lowercase().replaceFirstChar { it.uppercase() }}Agent",
            style = HisaabTheme.TypographyTrace.copy(
                color      = headerColor,
                fontSize   = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            ),
        )

        if (isLive) {
            LivePill()
        }
    }
}

// ── Tree text row ──────────────────────────────────────────────────────────────

@Composable
private fun TraceTextRow(
    treePrefix: String,
    content: String,
    contentColor: Color,
    animated: Boolean = false,
) {
    var displayContent by remember(content) { mutableStateOf(if (animated) "" else content) }

    // Typing animation for running steps
    LaunchedEffect(content, animated) {
        if (!animated) { displayContent = content; return@LaunchedEffect }
        displayContent = ""
        content.forEachIndexed { i, c ->
            displayContent = content.take(i + 1)
            kotlinx.coroutines.delay(12)  // 12ms per char — subtle, not distracting
        }
    }

    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = HisaabTheme.TextMuted)) {
            append(treePrefix)
        }
        withStyle(SpanStyle(color = contentColor)) {
            append(displayContent)
        }
        if (animated && displayContent.length < content.length) {
            withStyle(SpanStyle(color = HisaabTheme.Purple)) {
                append("▋")  // cursor
            }
        }
    }

    Text(
        text     = annotated,
        style    = HisaabTheme.TypographyTrace,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── LIVE pulsing pill ─────────────────────────────────────────────────────────

@Composable
fun LivePill() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 0.35f,
        animationSpec  = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "live_alpha",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HisaabTheme.Teal.copy(alpha = alpha))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = "LIVE",
            style = HisaabTheme.TypographyTrace.copy(
                color    = Color.White,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
            ),
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun resolveAgentStatus(steps: List<AgentTraceStep>): AgentTaskStatus {
    return when {
        steps.any { it.status == AgentTaskStatus.RUNNING }        -> AgentTaskStatus.RUNNING
        steps.any { it.status == AgentTaskStatus.CONFLICT_FOUND } -> AgentTaskStatus.CONFLICT_FOUND
        steps.any { it.status == AgentTaskStatus.FAILED }         -> AgentTaskStatus.FAILED
        steps.all { it.status == AgentTaskStatus.DONE }           -> AgentTaskStatus.DONE
        else                                                        -> AgentTaskStatus.PENDING
    }
}

private fun resultColor(result: String): Color = when {
    result.startsWith("CLEAN")             -> HisaabTheme.Teal
    result.startsWith("DUPLICATE")        -> HisaabTheme.Amber
    result.startsWith("AMOUNT_MISMATCH")  -> HisaabTheme.Red
    result.startsWith("confidence=")      -> HisaabTheme.TextSecondary
    result.contains("tier3")              -> HisaabTheme.Purple
    result.startsWith("SKIP")             -> HisaabTheme.TextMuted
    else                                  -> HisaabTheme.TextSecondary
}
