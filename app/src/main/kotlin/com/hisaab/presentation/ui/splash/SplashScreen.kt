package com.hisaab.presentation.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    // Impeccable Motion: Spring-based scaling and fluid alpha
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    val alpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "LogoAlpha"
    )

    // Staggered text reveal
    val textAlpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 600, easing = LinearOutSlowInEasing),
        label = "TextAlpha"
    )

    // Endless rotation for the logo rings
    val infiniteTransition = rememberInfiniteTransition(label = "RingRotation")
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation1"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation2"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2600) // Hold for 2.6s before navigating to allow animations to shine
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF16161D), // SurfaceGlass equivalent
                        Color(0xFF0A0A0E)  // DeepBackground equivalent
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Impeccable Motion: High-fidelity Animated Canvas Logo
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00FF66).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width / 1.5f
                    )
                }

                // Rotating dynamic rings
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 6.dp.toPx()
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Inner ring
                    drawArc(
                        color = Color(0xFF00FF66), // NeonGreen
                        startAngle = rotation1,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )

                    // Outer ring
                    drawArc(
                        color = Color(0xFF00FF66).copy(alpha = 0.5f),
                        startAngle = rotation2,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round),
                        size = Size(size.width + strokeWidth * 2, size.height + strokeWidth * 2),
                        topLeft = Offset(-strokeWidth, -strokeWidth)
                    )
                }
                
                // Center core
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawCircle(
                        color = Color(0xFFFFFFFF), // TextPrimary
                        radius = size.width / 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Hisaab",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF),
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FINANCIAL INTELLIGENCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00FF66), // NeonGreen
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(textAlpha.value * 0.8f)
            )
        }
    }
}
