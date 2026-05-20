package com.hisaab.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.presentation.ui.components.BrandLogoImage
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * BalanceCard — Refactored to premium "surveillance minimalism" design.
 *
 * DESIGN SPEC:
 *  - Canvas Background #0B0D11 (Screen Base)
 *  - Card Background #161920 (HisaabTheme.Surface)
 *  - 1px subtle outline #222733 (HisaabTheme.BorderSubtle)
 *  - desaturated lavender accent #D6C5F0 (HisaabTheme.Purple)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalanceCard(
    totalBalance    : BigDecimal,
    deltaPercent    : Double,
    institutions    : List<String>,
    onPrivacyToggle : () -> Unit,
    isPrivate       : Boolean,
    onLongPress     : () -> Unit = {},
    modifier        : Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Entrance animation
    var isAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAppeared = true
    }
    
    val entranceScale by animateFloatAsState(
        targetValue = if (isAppeared) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "entrance_scale"
    )
    
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isAppeared) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "entrance_alpha"
    )

    // Balance entrance: animate from 0 → actual on first composition
    val animatedBalance by animateFloatAsState(
        targetValue   = totalBalance.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label         = "balance_roll",
    )

    // Expansion chevron rotation
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 172.dp)
            .scale(entranceScale)
            .alpha(entranceAlpha)
            .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
            .background(HisaabTheme.Surface)
            .border(
                width = 1.dp,
                color = HisaabTheme.BorderSubtle,
                shape = RoundedCornerShape(HisaabTheme.RadiusXl)
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                // Subtle lavender ambient glow
                drawCircle(
                    color = HisaabTheme.Purple.copy(alpha = 0.05f), 
                    radius = w * 0.8f, 
                    center = Offset(w * 0.15f, h * 0.3f)
                )
            }
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = onLongPress
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Top Row: Greeting & Monthly Change Badge
            val greeting = run {
                val hour = java.util.Calendar.getInstance()
                    .get(java.util.Calendar.HOUR_OF_DAY)
                when {
                    hour < 12 -> "Good morning"
                    hour < 17 -> "Good afternoon"
                    else      -> "Good evening"
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time-aware greeting
                Text(
                    text = greeting,
                    style = HisaabTheme.TypographyCaption.copy(
                        color      = HisaabTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                // Monthly change badge
                val deltaPositive = deltaPercent >= 0
                val badgeBg = if (deltaPositive) HisaabTheme.Teal.copy(alpha = 0.1f) else HisaabTheme.Red.copy(alpha = 0.1f)
                val badgeTextTint = if (deltaPositive) HisaabTheme.Teal else HisaabTheme.Red
                val prefix = if (deltaPositive) "+" else ""
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (deltaPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = badgeTextTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text  = "${prefix}${"%.1f".format(deltaPercent)}%",
                        color = badgeTextTint,
                        style = HisaabTheme.TypographyCaption.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 12.sp
                        )
                    )
                }
            }

            // Balance amount & Toggles
            val displayBalance = if (isPrivate) "••••••"
                else "PKR ${BigDecimal(animatedBalance.toDouble()).setScale(0, RoundingMode.HALF_UP).toPlainString()}"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = HisaabTheme.TypographyCaption.copy(color = HisaabTheme.TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = displayBalance,
                        style = HisaabTheme.TypographyDisplay.copy(
                            color         = HisaabTheme.TextPrimary,
                            fontSize      = 36.sp,
                            letterSpacing = (-1).sp
                        )
                    )
                }
                
                // Privacy toggle & Expand Chevron
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrivacyToggle) {
                        Icon(
                            imageVector = if (isPrivate) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle privacy",
                            tint = HisaabTheme.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = HisaabTheme.Purple,
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }
            
            // Sparkline & Institutions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                // Sparkline
                Canvas(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(120.dp)
                        .height(50.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(0f, h * 0.8f)
                        quadraticBezierTo(w * 0.2f, h * 0.9f, w * 0.4f, h * 0.5f)
                        quadraticBezierTo(w * 0.6f, h * 0.1f, w * 0.8f, h * 0.4f)
                        quadraticBezierTo(w * 0.9f, h * 0.5f, w, h * 0.2f)
                    }
                    drawPath(
                        path = path,
                        color = HisaabTheme.Purple,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(HisaabTheme.Purple.copy(alpha = 0.15f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )
                }
                
                // Institution chips strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.Bottom,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    val displayed = institutions.take(3)
                    val overflow  = institutions.size - 3

                    displayed.forEach { institution ->
                        InstitutionChip(institution)
                    }
                    if (overflow > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(HisaabTheme.SurfaceElev)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                "+$overflow",
                                color = HisaabTheme.TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = HisaabTheme.BorderSubtle, modifier = Modifier.padding(bottom = 16.dp))
                    
                    // Expanded state contents
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            SummaryItem(label = "INCOME", value = "PKR 120,000")
                            SummaryItem(label = "SPENT", value = "PKR 45,000")
                        }
                        
                        // Currency Selector Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HisaabTheme.SurfaceElev)
                                .border(1.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { /* switch currency */ }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PKR",
                                color = HisaabTheme.Purple,
                                style = HisaabTheme.TypographyCaption.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = HisaabTheme.TypographyCaption.copy(
                color      = HisaabTheme.TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize   = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = HisaabTheme.TypographyBody.copy(
                color      = HisaabTheme.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        )
    }
}

// ── Institution chip ──────────────────────────────────────────────────────────

@Composable
fun InstitutionChip(
    institution: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HisaabTheme.SurfaceElev)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        BrandLogoImage(
            institutionName = institution,
            size            = 20.dp,
        )
        Text(
            text  = institution.take(4).uppercase(),
            style = HisaabTheme.TypographyCaption.copy(
                color      = HisaabTheme.TextSecondary,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
