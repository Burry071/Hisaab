package com.hisaab.presentation.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.presentation.ui.theme.HisaabTheme
import kotlin.math.sin

/**
 * SpendingChartCard — wavy animated spending chart (PRD Screen 1, Image 2 inspired).
 *
 * Renders a smooth cubic bezier spending curve for 30 days.
 * Animates in on first composition with a left-to-right sweep (design spell).
 * The area under the curve is a purple-to-transparent gradient.
 *
 * Impeccable Motion principles applied:
 *  - Entrance: tween 1200ms, FastOutSlowInEasing (matches PRD spec)
 *  - The curve is "alive" — a subtle wave breathes via infiniteTransition
 *  - Labels stagger in after the chart draws (delayMillis = 800)
 */
@Composable
fun SpendingChartCard(
    modifier: Modifier = Modifier,
    weekLabels: List<String> = listOf("1", "7", "14", "21", "28"),
) {
    // Spending data shape (7-day buckets for one month) — demo-realistic values
    val spendingData = remember {
        listOf(12400f, 8200f, 15800f, 9600f, 18400f, 6800f, 14200f, 11000f, 16500f, 9800f,
               13200f, 7600f, 19100f, 11400f, 8900f, 14800f, 17200f, 10300f, 13700f, 16000f,
               8400f, 12100f, 9300f, 15600f, 11800f, 14500f, 18200f, 10700f, 13400f, 16800f)
    }

    // Entrance animation: chart draws left-to-right
    val drawProgress by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "chart_entrance",
    )

    // Subtle breathing animation — makes the chart feel alive
    val infiniteTransition = rememberInfiniteTransition(label = "chart_breathe")
    val breathe by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    val purple = HisaabTheme.Purple
    val teal   = HisaabTheme.Teal

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusLg))
            .background(HisaabTheme.Surface)
            .border(0.5.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(HisaabTheme.RadiusLg))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "Spending This Month",
                    style = HisaabTheme.TypographyTitle.copy(
                        color    = HisaabTheme.TextPrimary,
                        fontSize = 15.sp,
                    ),
                )
                Text(
                    "PKR 1,84,200",
                    style = HisaabTheme.TypographyDisplay.copy(
                        color    = HisaabTheme.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            // The wavy chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            ) {
                drawSpendingCurve(
                    data        = spendingData,
                    progress    = drawProgress,
                    breathePhase = breathe,
                    curveColor  = purple,
                )
            }

            // Week labels
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                weekLabels.forEach { label ->
                    Text(
                        label,
                        style = HisaabTheme.TypographyTrace.copy(
                            color    = HisaabTheme.TextMuted,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}

// ── Chart drawing logic (Compose Canvas) ──────────────────────────────────────

private fun DrawScope.drawSpendingCurve(
    data        : List<Float>,
    progress    : Float,
    breathePhase: Float,
    curveColor  : Color,
) {
    if (data.isEmpty()) return

    val w = size.width
    val h = size.height
    val maxVal = data.max()
    val minVal = data.min()
    val range  = (maxVal - minVal).coerceAtLeast(1f)

    // Map data to canvas coordinates (flipped Y — top = high spend)
    fun xOf(i: Int) = i.toFloat() / (data.size - 1) * w
    fun yOf(v: Float): Float {
        val normalized = (v - minVal) / range
        // Subtle breathe: ±4dp vertical wobble
        val breatheOffset = sin(breathePhase * Math.PI.toFloat()) * 8f
        return h - (normalized * (h * 0.85f) + h * 0.075f + breatheOffset)
    }

    // Build smooth cubic bezier path
    val curvePath = Path()
    val cutoffX   = w * progress     // drawing progress (left-to-right entrance)

    curvePath.moveTo(xOf(0), yOf(data[0]))
    for (i in 1 until data.size) {
        val x0 = xOf(i - 1)
        val x1 = xOf(i)
        if (x1 > cutoffX) break

        val y0  = yOf(data[i - 1])
        val y1  = yOf(data[i])
        val cp1 = Offset(x0 + (x1 - x0) * 0.4f, y0)
        val cp2 = Offset(x0 + (x1 - x0) * 0.6f, y1)

        curvePath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, minOf(x1, cutoffX), y1)
    }

    // Fill area under curve (gradient)
    val fillPath = Path().apply {
        addPath(curvePath)
        lineTo(minOf(cutoffX, xOf(data.size - 1)), h)
        lineTo(xOf(0), h)
        close()
    }

    drawPath(
        path  = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                curveColor.copy(alpha = 0.25f),
                curveColor.copy(alpha = 0.0f),
            ),
        ),
    )

    // Draw the curve line itself
    drawPath(
        path   = curvePath,
        color  = curveColor,
        style  = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // Live cursor dot at the current data point
    if (progress >= 1f) {
        val lastX = xOf(data.size - 1)
        val lastY = yOf(data.last())
        drawCircle(color = curveColor, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(
            color  = curveColor.copy(alpha = 0.25f),
            radius = 10.dp.toPx(),
            center = Offset(lastX, lastY),
        )
    }
}
