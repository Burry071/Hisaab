package com.hisaab.presentation.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.domain.model.Forecast
import com.hisaab.domain.model.ForecastType
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ForecastCard — displays an upcoming predicted transaction (PRD F6).
 *
 * Used in HomeScreen below the InsightCard.
 * Design: teal gradient border, 🔮 emoji, estimated range, confidence badge.
 * Taps navigate to InsightsScreen (full forecast context).
 */
@Composable
fun ForecastCard(
    forecast  : Forecast,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    // Subtle pulse animation for the confidence dot
    val infiniteTransition = rememberInfiniteTransition(label = "forecast_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label         = "pulse_alpha",
    )

    val fmt = remember { DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()) }

    // Confidence color: cyan for income, amber for bills/subscriptions
    val confidenceColor = when (forecast.type) {
        ForecastType.SALARY_CREDIT  -> HisaabTheme.Teal
        else                        -> HisaabTheme.Amber
    }
    // Border/accent color follows the same semantic
    val accentColor = confidenceColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusLg))
            .background(
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.08f), HisaabTheme.Surface.copy(alpha = 0.95f))
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(HisaabTheme.RadiusLg))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment  = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment  = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(forecastEmoji(forecast.type), fontSize = 18.sp)
                Column {
                    Text(
                        forecast.description,
                        style = HisaabTheme.TypographyBody.copy(
                            color      = HisaabTheme.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        "Expected ~${forecast.expectedDate.format(fmt)}",
                        style = HisaabTheme.TypographyTrace.copy(
                            color    = HisaabTheme.TextMuted,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            // Confidence badge — semantic color by forecast type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(HisaabTheme.RadiusSm))
                    .background(confidenceColor.copy(alpha = 0.12f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    "${(forecast.confidence * 100).toInt()}% confidence",
                    style = HisaabTheme.TypographyTrace.copy(
                        color    = confidenceColor,
                        fontSize = 11.sp,
                    ),
                )
            }
        }

        // Amount range
        Row(
            verticalAlignment  = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Animated pulse dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor.copy(alpha = pulseAlpha)),
            )
            Text(
                "Estimated PKR ${"%,d".format(forecast.estimatedAmountMin.toLong())}–${"%,d".format(forecast.estimatedAmountMax.toLong())}",
                style = HisaabTheme.TypographyTrace.copy(
                    color    = HisaabTheme.TextSecondary,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun forecastEmoji(type: ForecastType) = when (type) {
    ForecastType.UTILITY_BILL     -> "⚡"
    ForecastType.SALARY_CREDIT    -> "💰"
    ForecastType.SUBSCRIPTION     -> "📱"
    ForecastType.TRANSFER         -> "📤"
    ForecastType.GENERAL_EXPENSE  -> "🔮"
}
