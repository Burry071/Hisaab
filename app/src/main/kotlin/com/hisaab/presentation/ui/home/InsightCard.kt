package com.hisaab.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.model.Insight
import com.hisaab.domain.model.InsightLevel
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * InsightCard — communicates the core AI value.
 *
 * DESIGN SPEC:
 *  - Pill profile RoundedCornerShape(24.dp)
 *  - Card background HisaabTheme.Surface (#161920)
 *  - 1px subtle outline HisaabTheme.BorderSubtle (#222733)
 */
@Composable
fun InsightCard(
    insight    : Insight,
    index      : Int = 0,
    onAction   : (() -> Unit)? = null,
    modifier   : Modifier = Modifier,
) {
    val accentColor = when (insight.level) {
        InsightLevel.LEVEL_3 -> HisaabTheme.Amber
        InsightLevel.LEVEL_2 -> HisaabTheme.Amber
        InsightLevel.LEVEL_1 -> HisaabTheme.Teal
    }

    // Entrance: staggered slide-up per card index
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 120L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { it / 2 },
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
                .background(HisaabTheme.Surface)
                .border(
                    width = 1.dp,
                    color = HisaabTheme.BorderSubtle,
                    shape = RoundedCornerShape(HisaabTheme.RadiusXl),
                )
                // Subtle lavender top-edge glow
                .drawBehind {
                    drawCircle(
                        brush  = Brush.radialGradient(
                            colors = listOf(HisaabTheme.Purple.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width / 2f, 0f),
                            radius = size.width * 0.7f,
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width / 2f, 0f),
                    )
                }
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Level badge + category row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LevelBadge(insight.level, accentColor)
                    Text(
                        text  = insight.category.name.lowercase().capitalize(),
                        style = HisaabTheme.TypographyTrace.copy(
                            color    = HisaabTheme.TextSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    // Confidence pill
                    ConfidencePill(insight.confidence, accentColor)
                }

                // Headline
                Text(
                    text  = insight.headline,
                    style = HisaabTheme.TypographyTitle.copy(
                        color      = HisaabTheme.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                    ),
                )

                // Reasoning
                Text(
                    text  = insight.reasoning,
                    style = HisaabTheme.TypographyBody.copy(
                        color    = HisaabTheme.TextSecondary,
                        fontSize = 13.sp,
                    ),
                )

                // CTA
                if (insight.actionPrompt != null && onAction != null) {
                    Spacer(Modifier.height(4.dp))
                    ActionLink(
                        text    = insight.actionPrompt,
                        onClick = onAction,
                    )
                }
            }
        }
    }
}

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }

@Composable
private fun LevelBadge(level: InsightLevel, color: Color) {
    val label = when (level) {
        InsightLevel.LEVEL_3 -> "Critical"
        InsightLevel.LEVEL_2 -> "Warning"
        InsightLevel.LEVEL_1 -> "Info"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = label,
            style = HisaabTheme.TypographyTrace.copy(
                color      = color,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun ConfidencePill(confidence: Float, accentColor: Color) {
    val pct = (confidence * 100).toInt()
    Text(
        text  = "$pct% match",
        style = HisaabTheme.TypographyTrace.copy(
            color    = HisaabTheme.TextMuted,
            fontSize = 10.sp,
        ),
    )
}

@Composable
private fun ActionLink(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(HisaabTheme.RadiusSm))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = "$text →",
            style = HisaabTheme.TypographyBody.copy(
                color      = HisaabTheme.Purple,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
            ),
        )
    }
}
