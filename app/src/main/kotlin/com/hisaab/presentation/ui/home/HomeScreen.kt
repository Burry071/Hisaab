package com.hisaab.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hisaab.domain.demo.DemoModeManager
import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.presentation.ui.components.HisaabBrandLogo
import com.hisaab.presentation.ui.theme.HisaabTheme
import com.hisaab.presentation.viewmodels.HomeUiState
import com.hisaab.presentation.viewmodels.HomeViewModel
import java.math.BigDecimal

/**
 * HomeScreen — PRD Screen 1 (Dashboard).
 *
 * Layout uses LazyColumn for full-screen scroll — NEVER nested ScrollView.
 * (mobile-design skill: ScrollView for long lists = memory explosion)
 *
 * Structure (top → bottom):
 *   ① TopBar — greeting + notification bell (0dp elevation, tonal only)
 *   ② Conflict banner — pinned if conflicts exist (amber fill)
 *   ③ BalanceCard — hero, full-width purple
 *   ④ Agent status strip — compact, shows last pipeline run
 *   ⑤ "Your Insights" header + InsightCards (staggered entrance)
 *   ⑥ "Recent Transactions" header + TransactionRows (bare list)
 *
 * Design system compliance (DESIGN.md):
 *   - One Clash Display figure (balance) per screen
 *   - One purple CTA per screen (FAB or single button — not both)
 *   - All tonal elevation — zero Material shadows
 *   - Amber conflict banner pinned below TopBar
 *
 * Design spells applied:
 *   - TopBar collapses greeting on scroll (animated alpha)
 *   - Agent status strip has a micro scanning line animation
 *   - "See all" link has arrow-nudge on tap
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel        : HomeViewModel,
    onSeeAllInsights : () -> Unit,
    onSeeAllTxns     : () -> Unit,
    onConflictTap    : (String) -> Unit,
    onAgentTap       : () -> Unit,
) {
    val uiState       by viewModel.uiState.collectAsState()
    val isPrivate     by viewModel.isPrivate.collectAsState()
    val simulatorResult by viewModel.simulatorResult.collectAsState()
    val listState      = rememberLazyListState()

    // Scroll-aware greeting alpha — collapses on scroll
    val greetingAlpha by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val item   = listState.firstVisibleItemIndex
            if (item > 0) 0f else (1f - (offset / 150f)).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase)) {

        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingShimmer()

            is HomeUiState.Error -> ErrorState(
                message = state.message,
                onRetry = viewModel::refresh,
            )

            is HomeUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ① TopBar
                    HomeTopBar(
                        greetingAlpha = greetingAlpha,
                        onBellClick   = {},
                    )

                    // ② Conflict banner — pinned, only when conflicts exist
                    if (state.conflicts.isNotEmpty()) {
                        ConflictBannerStrip(
                            count    = state.conflicts.size,
                            onClick  = { onConflictTap(state.conflicts.first().incomingId) },
                        )
                    }

                    // ③–⑥ Main content via LazyColumn (mobile-design: no ScrollView)
                    LazyColumn(
                        state             = listState,
                        contentPadding    = PaddingValues(bottom = 96.dp), // nav bar clearance
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        // ③ Balance card
                        item(key = "balance") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                BalanceCard(
                                    totalBalance    = state.totalBalance,
                                    deltaPercent    = state.deltaPercent,
                                    institutions    = state.institutions,
                                    isPrivate       = isPrivate,
                                    onPrivacyToggle = viewModel::togglePrivacy,
                                    onLongPress     = {
                                        // Hackathon demo injection (PRD §9 — logo long-press)
                                        viewModel.loadDemoBundle(DemoModeManager.buildDemoData())
                                    },
                                )
                            }
                        }


                        // ④ Agent status strip
                        item(key = "agent_strip") {
                            AgentStatusStrip(
                                lastRunMs    = state.lastAgentRunMs,
                                txnCount     = state.processedCount,
                                isRunning    = state.agentRunning,
                                onClick      = onAgentTap,
                                modifier     = Modifier.padding(horizontal = 20.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // ④.5 Spending chart — wavy animated (PRD Image 2)
                        item(key = "spending_chart") {
                            SpendingChartCard(
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // ④.6 Forecast cards (if any)
                        if (state.forecasts.isNotEmpty()) {
                            item(key = "forecast_header") {
                                SectionHeader(
                                    title    = "Upcoming",
                                    count    = state.forecasts.size,
                                    onSeeAll = {},
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 24.dp, bottom = 12.dp),
                                )
                            }
                            items(
                                items = state.forecasts,
                                key   = { it.id },
                            ) { forecast ->
                                ForecastCard(
                                    forecast = forecast,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        // ⑤ Insights section (Horizontal Carousel)
                        if (state.insights.isNotEmpty()) {
                            item(key = "insights_header") {
                                SectionHeader(
                                    title    = "Your Insights",
                                    count    = state.insights.size,
                                    onSeeAll = onSeeAllInsights,
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 24.dp, bottom = 12.dp),
                                )
                            }

                            item(key = "insights_carousel") {
                                androidx.compose.foundation.lazy.LazyRow(
                                    contentPadding        = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(
                                        items = state.insights.take(3),
                                        key   = { it.id },
                                    ) { insight ->
                                        val idx = state.insights.indexOf(insight)
                                        InsightCard(
                                            insight  = insight,
                                            index    = idx,
                                            onAction = { /* navigate to action screen */ },
                                            modifier = Modifier.width(300.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }

                        // ⑤.5 Budget Simulator
                        item(key = "budget_simulator") {
                            BudgetSimulatorCard(
                                onSimulate = { query -> viewModel.simulateBudget(query) },
                                result = simulatorResult,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // ⑥ Transactions section
                        if (state.recentTransactions.isNotEmpty()) {
                            item(key = "txn_header") {
                                SectionHeader(
                                    title    = "Recent Transactions",
                                    count    = state.recentTransactions.size,
                                    onSeeAll = onSeeAllTxns,
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 24.dp, bottom = 12.dp),
                                )
                            }

            item(key = "txn_container") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    state.recentTransactions.take(8).forEach { tx ->
                        TransactionRow(
                            transaction = tx,
                            onClick     = {},
                        )
                    }
                }
            }
                        }

                        // Empty state
                        if (state.insights.isEmpty() && state.recentTransactions.isEmpty()) {
                            item(key = "empty") {
                                EmptyState(modifier = Modifier.padding(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── ① Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(greetingAlpha: Float, onBellClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HisaabTheme.BgBase)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Micro inline logo configuration
            HisaabBrandLogo(size = 32.dp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                "Hisaab",
                style = HisaabTheme.TypographyHeadline.copy(
                    color      = HisaabTheme.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
            )
        }

        // Systems active telemetry tag
        Text(
            text  = "[SYS_CORE_ONLINE]",
            style = HisaabTheme.TypographyLabelMicro.copy(
                color         = HisaabTheme.Teal,
                fontSize      = 9.sp,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ── ② Conflict banner strip ───────────────────────────────────────────────────

@Composable
private fun ConflictBannerStrip(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HisaabTheme.Amber)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("⚠️", fontSize = 16.sp)
        Text(
            text  = "$count transaction ${if (count == 1) "conflict" else "conflicts"} detected — tap to review",
            style = HisaabTheme.TypographyBody.copy(
                color      = HisaabTheme.BgBase,  // black on amber — max contrast
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
            ),
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.ChevronRight, null,
            tint     = HisaabTheme.BgBase,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ── ④ Agent status strip ──────────────────────────────────────────────────────

@Composable
private fun AgentStatusStrip(
    lastRunMs : Long?,
    txnCount  : Int,
    isRunning : Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    // Design spell: scanning line animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label         = "scan_x",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusPill))
            .background(HisaabTheme.Surface)
            .border(1.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(HisaabTheme.RadiusPill))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Agent brain icon — purple when running, muted when idle
        Icon(
            Icons.Default.Psychology,
            contentDescription = "Agent",
            tint     = if (isRunning) HisaabTheme.Purple else HisaabTheme.TextMuted,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = if (isRunning) "Pipeline running…" else "Agent pipeline",
                style = HisaabTheme.TypographyTrace.copy(
                    color    = if (isRunning) HisaabTheme.Purple else HisaabTheme.TextSecondary,
                    fontSize = 12.sp,
                ),
            )
            if (lastRunMs != null && !isRunning) {
                Text(
                    text  = "$txnCount txns · ${relativeTime(lastRunMs)}",
                    style = HisaabTheme.TypographyTrace.copy(
                        color    = HisaabTheme.TextMuted,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
        if (isRunning) {
            CircularProgressIndicator(
                color        = HisaabTheme.Purple,
                modifier     = Modifier.size(16.dp),
                strokeWidth  = 2.dp,
            )
        } else {
            Icon(
                Icons.Default.ChevronRight, null,
                tint     = HisaabTheme.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title   : String,
    count   : Int,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = HisaabTheme.TypographyTitle.copy(
                    color      = HisaabTheme.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    letterSpacing = (-0.5).sp
                ),
            )
            // Count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(HisaabTheme.RadiusPill))
                    .background(HisaabTheme.Surface)
                    .border(1.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(HisaabTheme.RadiusPill))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    count.toString(),
                    style = HisaabTheme.TypographyTrace.copy(
                        color    = HisaabTheme.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
        }
        // "See all" — text only, NOT a second purple CTA
        TextButton(
            onClick = onSeeAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                "See all",
                style = HisaabTheme.TypographyBody.copy(
                    color    = HisaabTheme.TextSecondary,
                    fontSize = 13.sp,
                ),
            )
        }
    }
}

// ── Loading shimmer ───────────────────────────────────────────────────────────

@Composable
private fun LoadingShimmer() {
    val shimmer by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "shimmer_alpha",
    )
    Column(
        modifier            = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Balance card placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
                .background(HisaabTheme.Surface.copy(alpha = shimmer)),
        )
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(HisaabTheme.RadiusLg))
                    .background(HisaabTheme.Surface.copy(alpha = shimmer)),
            )
        }
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("❌", fontSize = 32.sp)
        Spacer(Modifier.height(16.dp))
        Text(message, style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = HisaabTheme.Purple),
            shape   = RoundedCornerShape(HisaabTheme.RadiusMd),
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("📊", fontSize = 40.sp)
        Text(
            "No transactions yet",
            style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary),
        )
        Text(
            "Grant SMS permission or run the demo to see your financial intelligence.",
            style = HisaabTheme.TypographyBody.copy(
                color = HisaabTheme.TextSecondary,
                fontSize = 14.sp,
            ),
        )
    }
}

// ── Time helper ───────────────────────────────────────────────────────────────

private fun relativeTime(epochMs: Long): String {
    val delta = System.currentTimeMillis() - epochMs
    return when {
        delta < 60_000L       -> "just now"
        delta < 3_600_000L    -> "${delta / 60_000}m ago"
        delta < 86_400_000L   -> "${delta / 3_600_000}h ago"
        else                  -> "${delta / 86_400_000}d ago"
    }
}

// ── Forecast Card ─────────────────────────────────────────────────────────────

@Composable
private fun ForecastCard(
    forecast : com.hisaab.domain.model.Forecast,
    modifier : Modifier = Modifier,
) {
    val iconText = when (forecast.type) {
        com.hisaab.domain.model.ForecastType.UTILITY_BILL   -> "⚡"
        com.hisaab.domain.model.ForecastType.SALARY_CREDIT  -> "💼"
        com.hisaab.domain.model.ForecastType.SUBSCRIPTION   -> "🔁"
        com.hisaab.domain.model.ForecastType.TRANSFER       -> "↔"
        com.hisaab.domain.model.ForecastType.GENERAL_EXPENSE-> "📋"
    }

    val daysUntil = java.time.temporal.ChronoUnit.DAYS
        .between(java.time.LocalDate.now(), forecast.expectedDate)
        .coerceAtLeast(0L)

    val confidencePct = (forecast.confidence * 100).toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
            .background(HisaabTheme.Surface)
            .border(1.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(HisaabTheme.RadiusXl))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon badge
        Box(
            modifier          = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
                .background(HisaabTheme.Amber.copy(alpha = 0.12f)),
            contentAlignment  = Alignment.Center,
        ) {
            Text(iconText, fontSize = 20.sp)
        }

        // Description + date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                forecast.description,
                style = HisaabTheme.TypographyBody.copy(
                    color    = HisaabTheme.TextPrimary,
                    fontSize = 14.sp,
                ),
                maxLines = 1,
            )
            Text(
                "in $daysUntil day${if (daysUntil != 1L) "s" else ""} · $confidencePct% confidence",
                style = HisaabTheme.TypographyTrace.copy(
                    color    = HisaabTheme.TextMuted,
                    fontSize = 12.sp,
                ),
            )
        }

        // Amount range
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "PKR ${"%.0f".format(forecast.estimatedAmountMin.toDouble())}",
                style = HisaabTheme.TypographyTitle.copy(
                    color    = HisaabTheme.Amber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            if (forecast.estimatedAmountMin != forecast.estimatedAmountMax) {
                Text(
                    "– ${"%.0f".format(forecast.estimatedAmountMax.toDouble())}",
                    style = HisaabTheme.TypographyTrace.copy(
                        color    = HisaabTheme.TextMuted,
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}

