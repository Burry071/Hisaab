package com.hisaab.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * HisaabBrandLogo — The "Ledger Matrix Node".
 *
 * A high-agency vector asset representing calculation, accounting, and data structures.
 * Designed for Material 3 Expressive (Android 16 style) with a tactical surveillance aesthetic.
 */
@Composable
fun HisaabBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    containerColor: Color = Color(0xFF1F232C),     // Expressive dark capsule surface
    gridLineColor: Color = Color(0xFF2A2F3A),      // Faint wireframe grid lines
    activeNodeColor: Color = Color(0xFFA8C7FA),    // Pixel Expressive soft blue accent
    statusGreen: Color = Color(0xFF81C784)         // Telemetry active status dot
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            // Strict Material 3 Expressive deep curvature (35% of size)
            .clip(RoundedCornerShape(size * 0.35f)) 
            .background(containerColor)
            // 1px technical wireframe border
            .border(1.dp, gridLineColor, RoundedCornerShape(size * 0.35f)) 
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.22f) // Maintains structural inner padding
        ) {
            val width = size.toPx() * (1f - 0.44f)
            val height = size.toPx() * (1f - 0.44f)
            
            // 1. Structural Grid Computations
            val columnOneX = width * 0.33f
            val columnTwoX = width * 0.66f
            val rowOneY = height * 0.33f
            val rowTwoY = height * 0.66f
            
            val strokeWidth = 1.5.dp.toPx()

            // Vertical Grid Channels
            drawLine(
                color = gridLineColor,
                start = Offset(x = columnOneX, y = 0f),
                end = Offset(x = columnOneX, y = height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = gridLineColor,
                start = Offset(x = columnTwoX, y = 0f),
                end = Offset(x = columnTwoX, y = height),
                strokeWidth = strokeWidth
            )

            // Horizontal Grid Channels
            drawLine(
                color = gridLineColor,
                start = Offset(x = 0f, y = rowOneY),
                end = Offset(x = width, y = rowOneY),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = gridLineColor,
                start = Offset(x = 0f, y = rowTwoY),
                end = Offset(x = width, y = rowTwoY),
                strokeWidth = strokeWidth
            )

            // 2. Telemetry Focus Points
            // Top-left intersection node
            drawCircle(
                color = activeNodeColor,
                radius = 2.5.dp.toPx(),
                center = Offset(x = columnOneX, y = rowOneY)
            )
            
            // Bottom-right intersection ring
            drawCircle(
                color = statusGreen,
                radius = 3.5.dp.toPx(),
                center = Offset(x = columnTwoX, y = rowTwoY),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}
