package com.hisaab.presentation.ui.simulation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.model.*
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.math.BigDecimal
import kotlinx.coroutines.launch

/**
 * SimulationScreen — PRD Screen 5 (Before/After Action Simulation).
 *
 * Shows the full impact of applying a budget action:
 *   ① Action title card (purple border)
 *   ② BEFORE state table (risk-colored)
 *   ③ AFTER state table (safe-colored, animates in when agent applied)
 *   ④ "Agent will:" checklist of committed steps
 *   ⑤ Apply / Try Another CTA pair
 *
 * Design: zero shadow, tonal elevation only. One purple CTA (Apply).
 */
@Composable
fun SimulationScreen(
    action         : BudgetAction,
    currentState   : BudgetState,
    onApply        : (SimulationResult) -> Unit,
    onTryAnother   : () -> Unit,
    onBack         : () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize list of alternative budget scenario cards
    val mockActions = remember(action) {
        listOf(
            action,
            BudgetAction(
                type            = ActionType.REALLOCATE,
                targetCategory  = InsightCategory.SHOPPING,
                targetAmount    = BigDecimal("2000"),
                rationale       = "Move PKR 2,000 from shopping budget",
                projectedSaving = BigDecimal("2000"),
                effortScore     = 1,
                impactScore     = 3,
            ),
            BudgetAction(
                type            = ActionType.SET_LIMIT,
                targetCategory  = InsightCategory.TRANSPORT,
                targetAmount    = BigDecimal("500"),
                rationale       = "Reduce transport limits by PKR 500",
                projectedSaving = BigDecimal("3000"),
                effortScore     = 3,
                impactScore     = 4,
            )
        )
    }

    var activeInsightIndex by remember { mutableIntStateOf(0) }
    val currentAction = mockActions[activeInsightIndex]
    val result = remember(currentAction, currentState) { computeSimulation(currentAction, currentState) }

    // Animate AFTER panel reveal
    var showAfter by remember { mutableStateOf(false) }
    LaunchedEffect(currentAction) {
        showAfter = false
        kotlinx.coroutines.delay(300)
        showAfter = true
    }

    Column(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase)) {
        SimTopBar(action = currentAction, onBack = onBack)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ① Action title
            ActionTitleCard(action = currentAction)

            // ② BEFORE
            StateTable(
                title      = "──── BEFORE ────",
                accentColor = HisaabTheme.Amber,
                rows = buildBeforeRows(result.stateBefore, currentAction),
                statusLabel = "⚠️ RISK",
                statusColor = HisaabTheme.Amber,
            )

            // ③ AFTER (animated)
            AnimatedVisibility(
                visible     = showAfter,
                enter       = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 },
            ) {
                StateTable(
                    title       = "──── AFTER ────",
                    accentColor = HisaabTheme.Teal,
                    rows        = buildAfterRows(result.stateAfter, currentAction),
                    statusLabel = "✅ SAFE",
                    statusColor = HisaabTheme.Teal,
                )
            }

            // ④ Agent checklist
            AgentChecklist()

            // ⑤ CTAs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = {
                        scope.launch {
                            android.widget.Toast.makeText(
                                context,
                                "🤖 AI Agent: Daily spend target optimized safely.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            onApply(result)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = HisaabTheme.Purple),
                    shape    = RoundedCornerShape(HisaabTheme.RadiusMd),
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apply This Action", color = Color.White, style = HisaabTheme.TypographyBody)
                }
                OutlinedButton(
                    onClick  = {
                        activeInsightIndex = (activeInsightIndex + 1) % mockActions.size
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(HisaabTheme.RadiusMd),
                ) {
                    Text("Try Another Option", color = HisaabTheme.TextSecondary, style = HisaabTheme.TypographyBody)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SimTopBar(action: BudgetAction, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgSecondary)
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back", tint = HisaabTheme.TextSecondary)
        }
        Text(
            "Simulate Action",
            style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ActionTitleCard(action: BudgetAction) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.Surface)
            .border(1.dp, HisaabTheme.Purple.copy(alpha = 0.4f), RoundedCornerShape(HisaabTheme.RadiusMd))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Bolt, null, tint = HisaabTheme.Purple, modifier = Modifier.size(20.dp))
            Text(
                "\"${action.rationale}\"",
                style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScorePill("Impact ${action.impactScore}/5", HisaabTheme.Teal)
            ScorePill("Effort ${action.effortScore}/5", HisaabTheme.TextMuted)
        }
    }
}

@Composable
private fun ScorePill(label: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(HisaabTheme.RadiusPill))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, style = HisaabTheme.TypographyTrace.copy(color = color, fontSize = 11.sp))
    }
}

@Composable
private fun StateTable(
    title       : String,
    accentColor : Color,
    rows        : List<Pair<String, String>>,
    statusLabel : String,
    statusColor : Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.BgSecondary)
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(HisaabTheme.RadiusMd))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = HisaabTheme.TypographyTrace.copy(
            color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))

        HorizontalDivider(color = accentColor.copy(alpha = 0.2f), thickness = 0.5.dp)

        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted))
                Text(value, style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextSecondary))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Status", style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted))
            Text(statusLabel, style = HisaabTheme.TypographyTrace.copy(color = statusColor, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun AgentChecklist() {
    val steps = listOf(
        "Update daily category limit in Room DB",
        "Draft spending reminder notification",
        "Schedule 3-day progress checkpoint",
    )
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.BgSecondary)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Agent will:", style = HisaabTheme.TypographyTrace.copy(
            color = HisaabTheme.Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
        steps.forEach { step ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = HisaabTheme.Teal, modifier = Modifier.size(16.dp))
                Text(step, style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextSecondary))
            }
        }
    }
}

// ── Simulation logic ──────────────────────────────────────────────────────────

private fun computeSimulation(action: BudgetAction, state: BudgetState): SimulationResult {
    val currentSpend  = state.categorySpends[action.targetCategory] ?: BigDecimal.ZERO
    val saving        = action.projectedSaving
    val newSpend      = currentSpend - saving
    val newSpends     = state.categorySpends.toMutableMap()
        .also { it[action.targetCategory] = newSpend.coerceAtLeast(BigDecimal.ZERO) }
    val stateAfter    = state.copy(
        totalBalance   = state.totalBalance + saving,
        categorySpends = newSpends,
    )
    val totalSpendBefore  = state.categorySpends.values.fold(BigDecimal.ZERO) { a, b -> a.add(b) }
    val monthEnd = state.totalBalance.subtract(totalSpendBefore.subtract(saving))
    return SimulationResult(
        action                 = action,
        stateBefore            = state,
        stateAfter             = stateAfter,
        projectedSaving        = saving,
        newBalanceAtMonthEnd   = monthEnd,
        reasoning              = "Applying ${action.rationale} saves PKR ${saving.toLong()} by month-end",
    )
}

private fun buildBeforeRows(state: BudgetState, action: BudgetAction): List<Pair<String, String>> {
    val categorySpend = state.categorySpends[action.targetCategory] ?: BigDecimal.ZERO
    val totalSpend    = state.categorySpends.values.fold(BigDecimal.ZERO) { a, b -> a.add(b) }
    val projectedDeficit = totalSpend.subtract(state.monthlyIncome).coerceAtLeast(BigDecimal.ZERO)
    
    return listOf(
        "Category spend remaining" to "PKR ${"%,d".format(categorySpend.toLong())}",
        "Daily limit (current)"    to "PKR ${"%,d".format((categorySpend / BigDecimal(state.daysRemainingInMonth.coerceAtLeast(1))).toLong())}",
        "Projected month-end"      to "PKR -${"%,d".format(projectedDeficit.toLong())}",
    )
}

private fun buildAfterRows(state: BudgetState, action: BudgetAction) = listOf(
    "Category spend remaining" to "PKR ${"%,d".format((state.categorySpends[action.targetCategory] ?: BigDecimal.ZERO).toLong())}",
    "Daily limit (new)"        to "PKR ${"%,d".format(((state.categorySpends[action.targetCategory] ?: BigDecimal.ZERO) / BigDecimal(state.daysRemainingInMonth.coerceAtLeast(1))).toLong())}",
    "Projected month-end"      to "+PKR ${"%,d".format(action.projectedSaving.toLong())}",
)
