package com.hisaab.presentation.ui.agent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.hisaab.domain.model.ConflictResult
import com.hisaab.domain.model.ConflictSeverity
import com.hisaab.domain.model.ConflictType
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * ConflictDetailScreen — PRD Screen 3 (Contradiction View).
 *
 * Shows the agent's side-by-side analysis of a cross-source conflict:
 *   • SOURCE A card (institution + amount + time)
 *   • SOURCE B card (institution + amount + time)
 *   • Agent decision (LLM or heuristic reasoning)
 *   • Accept / Manual Review actions
 *
 * Design notes:
 *   • Amber banner at top — always (DESIGN.md Conflict Banner spec)
 *   • LLM reasoning in JetBrains Mono tree format
 *   • "Resolved by AI" badge if LLM arbitrated
 */
@Composable
fun ConflictDetailScreen(
    conflict: ConflictResult,
    onAcceptResolution: () -> Unit,
    onReviewManually: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HisaabTheme.BgBase),
    ) {
        // ── Amber conflict banner (pinned, full-width) ─────────────────────────
        ConflictBanner(conflict.severity)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Screen title ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back",
                        tint = HisaabTheme.TextSecondary)
                }
                Column {
                    Text(
                        "Transaction Conflict",
                        style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary),
                    )
                    Text(
                        conflictTypeLabel(conflict.type),
                        style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Amber),
                    )
                }
            }

            // ── Conflict description ────────────────────────────────────────────
            Text(
                conflict.description,
                style   = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary),
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            // ── Source cards (only for AMOUNT_MISMATCH — show both sides) ──────
            if (conflict.type == ConflictType.AMOUNT_MISMATCH) {
                Text(
                    "SOURCE COMPARISON",
                    style = HisaabTheme.TypographyTrace.copy(
                        color         = HisaabTheme.TextMuted,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SourceCard(
                        label      = "SOURCE A",
                        txnId      = conflict.incomingId.take(8),
                        isCanonical= conflict.canonicalAmount != null,
                        modifier   = Modifier.weight(1f),
                    )
                    SourceCard(
                        label      = "SOURCE B",
                        txnId      = conflict.conflictingId?.take(8) ?: "—",
                        isCanonical= false,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }

            // ── Agent decision card ─────────────────────────────────────────────
            AgentDecisionCard(conflict = conflict)

            // ── Action buttons ──────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onAcceptResolution,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = HisaabTheme.Purple),
                    shape    = RoundedCornerShape(HisaabTheme.RadiusMd),
                ) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Accept Resolution", color = Color.White,
                        style = HisaabTheme.TypographyBody)
                }

                OutlinedButton(
                    onClick  = onReviewManually,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                    ),
                    shape    = RoundedCornerShape(HisaabTheme.RadiusMd),
                ) {
                    Icon(Icons.Default.Edit, null,
                        tint = HisaabTheme.TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Review Manually", color = HisaabTheme.TextSecondary,
                        style = HisaabTheme.TypographyBody)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Conflict banner (DESIGN.md spec) ─────────────────────────────────────────

@Composable
private fun ConflictBanner(severity: ConflictSeverity) {
    val (bg, icon, text) = when (severity) {
        ConflictSeverity.HIGH   -> Triple(HisaabTheme.Amber, "⚠️", "High-severity conflict detected")
        ConflictSeverity.MEDIUM -> Triple(HisaabTheme.Amber.copy(alpha = 0.8f), "⚠️", "Potential duplicate found")
        ConflictSeverity.LOW    -> Triple(HisaabTheme.TextMuted, "ℹ️", "Minor discrepancy flagged")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(icon, fontSize = 20.sp)
        Text(
            text  = text,
            style = HisaabTheme.TypographyBody.copy(
                color      = HisaabTheme.BgBase,     // black text on amber — max contrast
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

// ── Source comparison card ────────────────────────────────────────────────────

@Composable
private fun SourceCard(
    label: String,
    txnId: String,
    isCanonical: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isCanonical) HisaabTheme.Teal else HisaabTheme.BorderSubtle

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.Surface)
            .border(1.dp, borderColor, RoundedCornerShape(HisaabTheme.RadiusMd))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                label,
                style = HisaabTheme.TypographyTrace.copy(
                    color     = if (isCanonical) HisaabTheme.Teal else HisaabTheme.TextMuted,
                    fontSize  = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            if (isCanonical) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HisaabTheme.Teal.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("CANONICAL", style = HisaabTheme.TypographyTrace.copy(
                        color = HisaabTheme.Teal, fontSize = 8.sp,
                    ))
                }
            }
        }
        Text(
            "ID: $txnId…",
            style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextSecondary),
        )
    }
}

// ── Agent decision card ───────────────────────────────────────────────────────
@Composable
private fun AgentDecisionCard(conflict: ConflictResult) {
    var traceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.BgSecondary)
            .border(0.5.dp, HisaabTheme.Teal.copy(alpha = 0.3f), RoundedCornerShape(HisaabTheme.RadiusMd))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🤖", fontSize = 14.sp)
                Text(
                    "AI AUTO-ARBITRATION",
                    style = HisaabTheme.TypographyTitle.copy(
                        color = HisaabTheme.Teal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                )
            }
            if (conflict.resolvedByLlm) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HisaabTheme.Purple.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        "AI Arbitrated",
                        style = HisaabTheme.TypographyTrace.copy(
                            color = HisaabTheme.Purple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = HisaabTheme.BorderSubtle, thickness = 0.5.dp)

        val reasoning = conflict.arbitrationReasoning
            ?: conflict.suggestedResolution
            ?: "No resolution available — manual review required."

        // 1. Suggested Resolution box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HisaabTheme.RadiusSm))
                .background(HisaabTheme.Surface)
                .padding(12.dp)
        ) {
            Text(
                text = "RECOMMENDED ACTION",
                style = HisaabTheme.TypographyLabelMicro,
                color = HisaabTheme.TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conflict.suggestedResolution ?: "FLAG_MANUAL",
                style = HisaabTheme.TypographyBody.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // 2. Canonical Amount badge if mismatch
        if (conflict.canonicalAmount != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HisaabTheme.Teal.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(HisaabTheme.Teal)
                )
                Text(
                    text = "Canonical Amount: PKR ${conflict.canonicalAmount.toLong()}",
                    style = HisaabTheme.TypographyBody.copy(
                        color = HisaabTheme.Teal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // 3. Detailed Reasoning
        Column {
            Text(
                text = "DECISION LOGIC",
                style = HisaabTheme.TypographyLabelMicro,
                color = HisaabTheme.TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reasoning,
                style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            )
        }

        // 4. Collapsible Developer diagnostics
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            HorizontalDivider(color = HisaabTheme.BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { traceExpanded = !traceExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Developer Telemetry Trace",
                    color = HisaabTheme.Purple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (traceExpanded) "▼" else "▲",
                    color = HisaabTheme.Purple,
                    fontSize = 10.sp
                )
            }

            if (traceExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HisaabTheme.RadiusSm))
                        .background(Color(0xFF090A0D))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "├─ resolution: ${conflict.suggestedResolution ?: "FLAG_MANUAL"}",
                            style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Teal, fontSize = 11.sp),
                        )
                        if (conflict.canonicalAmount != null) {
                            Text(
                                text = "├─ canonical_amount: PKR ${conflict.canonicalAmount.toLong()}",
                                style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Teal, fontSize = 11.sp),
                            )
                        }
                        Text(
                            text = "└─ reasoning: $reasoning",
                            style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Teal, fontSize = 11.sp),
                        )
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun conflictTypeLabel(type: ConflictType) = when (type) {
    ConflictType.DUPLICATE               -> "Duplicate Transaction"
    ConflictType.AMOUNT_MISMATCH         -> "Cross-Source Amount Conflict"
    ConflictType.BALANCE_INCONSISTENCY   -> "Balance Inconsistency"
    ConflictType.CLEAN                   -> "No Conflict"
}
