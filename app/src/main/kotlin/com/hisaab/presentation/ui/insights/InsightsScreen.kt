package com.hisaab.presentation.ui.insights

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.model.*
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * InsightsScreen — PRD Screen 2 (The money screen for the demo).
 * Shows featured insight, agent reasoning (JetBrains Mono tree),
 * 3-month bar chart, and ranked recommended actions.
 */
@Composable
fun InsightsScreen(
    insights   : List<Insight>,
    onBack     : () -> Unit,
    onSimulate : (BudgetAction) -> Unit,
) {
    if (insights.isEmpty()) {
        InsightsEmptyState(onBack = onBack)
        return
    }

    val featured = insights.maxByOrNull { it.level.ordinal } ?: insights.first()

    Column(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase)) {
        InsightsTopBar(insight = featured, onBack = onBack)

        LazyColumn(
            contentPadding      = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "metric") {
                Spacer(Modifier.height(4.dp))
                InsightMetricCard(featured, Modifier.padding(horizontal = 16.dp))
            }
            item(key = "reasoning") {
                AgentReasoningBlock(featured, Modifier.padding(horizontal = 16.dp))
            }
            item(key = "chart") {
                ThreeMonthChart(Modifier.padding(horizontal = 16.dp))
            }
            item(key = "actions_hdr") {
                Text(
                    "── Recommended Actions ──",
                    style    = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(demoActions(featured), key = { it.type.name + it.targetCategory.name }) { action ->
                ActionCard(
                    action     = action,
                    isPrimary  = action == demoActions(featured).first(),
                    onSimulate = { onSimulate(action) },
                    modifier   = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "spacer") { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun InsightsTopBar(insight: Insight, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgSecondary)
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back", tint = HisaabTheme.TextSecondary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(categoryLabel(insight.category),
                style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold))
            Text(levelLabel(insight.level),
                style = HisaabTheme.TypographyTrace.copy(color = levelColor(insight.level)))
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(HisaabTheme.RadiusSm))
                .background(HisaabTheme.Purple.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text("${(insight.confidence * 100).toInt()}% conf",
                style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Purple, fontSize = 11.sp))
        }
    }
}

@Composable
private fun InsightMetricCard(insight: Insight, modifier: Modifier = Modifier) {
    val accent = if (insight.category == InsightCategory.SALARY) HisaabTheme.Teal else HisaabTheme.Amber
    Box(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
            .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.18f), HisaabTheme.Surface.copy(alpha = 0.95f))))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(HisaabTheme.RadiusXl))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(insight.headline,
                style = HisaabTheme.TypographyHeadline.copy(color = HisaabTheme.TextPrimary))
            Text(insight.reasoning.lines().firstOrNull() ?: "",
                style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary, fontSize = 14.sp),
                maxLines = 2)
            insight.actionPrompt?.let { prompt ->
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.TrendingUp, null, tint = accent, modifier = Modifier.size(16.dp))
                    Text(prompt, style = HisaabTheme.TypographyBody.copy(color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}


@Composable
private fun AgentReasoningBlock(insight: Insight, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }
    var showRawTrace by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.BgSecondary)
            .border(0.5.dp, HisaabTheme.Purple.copy(alpha = 0.25f), RoundedCornerShape(HisaabTheme.RadiusMd)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖", fontSize = 14.sp)
                Text("AI Reasoning Flow", style = HisaabTheme.TypographyTitle.copy(
                    color = HisaabTheme.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, tint = HisaabTheme.TextMuted, modifier = Modifier.size(18.dp))
        }
        
        AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                HorizontalDivider(color = HisaabTheme.BorderSubtle, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                val lines = insight.reasoning.split("\n").filter { it.isNotBlank() }

                if (!showRawTrace) {
                    // Beautiful Visual Stepper Timeline
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        lines.forEachIndexed { idx, line ->
                            val cleanLine = line
                                .replace("├─", "")
                                .replace("└─", "")
                                .trim()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left timeline track
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(24.dp)
                                ) {
                                    // Bullet Point Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(if (idx == lines.lastIndex) HisaabTheme.Purple else HisaabTheme.Teal)
                                    )
                                    // Vertical connection line
                                    if (idx != lines.lastIndex) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .height(24.dp)
                                                .background(HisaabTheme.BorderSubtle)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Step text
                                Text(
                                    text = cleanLine,
                                    style = HisaabTheme.TypographyBody.copy(
                                        color = HisaabTheme.TextPrimary,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // Monospace developer console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HisaabTheme.RadiusSm))
                            .background(Color(0xFF090A0D))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            lines.forEachIndexed { idx, line ->
                                val prefix = if (idx == lines.lastIndex) "└─" else "├─"
                                Text(
                                    text = "$prefix $line",
                                    style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Teal, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                // Toggle Button for Raw Log Trace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (showRawTrace) "Show Visual Path" else "Inspect Antigravity Trace Logs",
                        color = HisaabTheme.Purple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showRawTrace = !showRawTrace }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreeMonthChart(modifier: Modifier = Modifier) {
    data class Bar(val label: String, val amount: Long, val current: Boolean)
    val bars = listOf(Bar("Jan", 12_400L, false), Bar("Feb", 14_800L, false), Bar("Mar", 18_400L, true))
    val maxVal = bars.maxOf { it.amount }.toFloat()
    val progress by animateFloatAsState(1f, tween(800, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)), label = "bar")

    Column(modifier = modifier.fillMaxWidth()
        .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
        .background(HisaabTheme.BgSecondary).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("3-Month Comparison", style = HisaabTheme.TypographyTrace.copy(
            color = HisaabTheme.TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
        bars.forEach { bar ->
            val fraction = (bar.amount / maxVal) * progress
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()) {
                Text(bar.label, style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted),
                    modifier = Modifier.width(30.dp))
                Box(modifier = Modifier.weight(1f).height(22.dp).clip(RoundedCornerShape(4.dp))
                    .background(HisaabTheme.SurfaceElev)) {
                    Box(modifier = Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(RoundedCornerShape(4.dp))
                        .background(if (bar.current) HisaabTheme.Amber else HisaabTheme.TextMuted.copy(alpha = 0.4f)))
                }
                Text("PKR ${"%,d".format(bar.amount)}", modifier = Modifier.width(92.dp),
                    style = HisaabTheme.TypographyTrace.copy(
                        color = if (bar.current) HisaabTheme.Amber else HisaabTheme.TextSecondary,
                        fontWeight = if (bar.current) FontWeight.SemiBold else FontWeight.Normal))
                if (bar.current) Text("←now",
                    style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Amber, fontSize = 10.sp))
            }
        }
    }
}

@Composable
private fun ActionCard(action: BudgetAction, isPrimary: Boolean, onSimulate: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()
        .clip(RoundedCornerShape(HisaabTheme.RadiusMd)).background(HisaabTheme.Surface)
        .border(0.5.dp, if (isPrimary) HisaabTheme.Purple.copy(alpha = 0.4f) else HisaabTheme.BorderSubtle,
            RoundedCornerShape(HisaabTheme.RadiusMd)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
            .background(if (isPrimary) HisaabTheme.Purple else HisaabTheme.SurfaceElev),
            contentAlignment = Alignment.Center) {
            Text(if (isPrimary) "①" else "②",
                style = HisaabTheme.TypographyTrace.copy(
                    color = if (isPrimary) Color.White else HisaabTheme.TextMuted, fontSize = 13.sp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(action.rationale, style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextPrimary))
            Text("Saves PKR ${"%,d".format(action.projectedSaving.toLong())} · Impact ${action.impactScore}/5",
                style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted, fontSize = 11.sp))
        }
        if (isPrimary) {
            Button(onClick = onSimulate,
                colors = ButtonDefaults.buttonColors(containerColor = HisaabTheme.Purple),
                shape = RoundedCornerShape(HisaabTheme.RadiusSm),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Text("Simulate →", color = Color.White, fontSize = 12.sp)
            }
        } else {
            TextButton(onClick = onSimulate) {
                Text("Try →", color = HisaabTheme.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InsightsEmptyState(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("💡", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("No insights yet", style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary))
        Text("Run the agent pipeline to generate AI-powered financial insights.",
            style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary))
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) { Text("← Go Back", color = HisaabTheme.TextSecondary) }
    }
}

private fun demoActions(insight: Insight) = listOf(
    BudgetAction(ActionType.REDUCE_CATEGORY, insight.category, java.math.BigDecimal("300"),
        "Cut daily ${categoryLabel(insight.category).lowercase()} by PKR 300",
        java.math.BigDecimal("2400"), effortScore = 2, impactScore = 5),
    BudgetAction(ActionType.REALLOCATE, InsightCategory.SHOPPING, java.math.BigDecimal("2000"),
        "Move PKR 2,000 from shopping budget", java.math.BigDecimal("2000"), effortScore = 3, impactScore = 4),
)

private fun categoryLabel(c: InsightCategory) = when (c) {
    InsightCategory.FOOD -> "Food Spending"; InsightCategory.TRANSPORT -> "Transport"
    InsightCategory.UTILITIES -> "Utilities"; InsightCategory.SHOPPING -> "Shopping"
    InsightCategory.HEALTH -> "Health"; InsightCategory.SALARY -> "Salary"
    InsightCategory.TRANSFER -> "Transfers"; InsightCategory.GENERAL -> "General"
}
private fun levelLabel(l: InsightLevel) = when (l) {
    InsightLevel.LEVEL_1 -> "Level 1 · Summary"
    InsightLevel.LEVEL_2 -> "Level 2 · Trend"
    InsightLevel.LEVEL_3 -> "Level 3 · AI Prediction"
}
private fun levelColor(l: InsightLevel) = when (l) {
    InsightLevel.LEVEL_1 -> HisaabTheme.TextMuted
    InsightLevel.LEVEL_2 -> HisaabTheme.Amber
    InsightLevel.LEVEL_3 -> HisaabTheme.Purple
}
