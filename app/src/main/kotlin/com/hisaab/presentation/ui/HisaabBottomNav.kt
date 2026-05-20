package com.hisaab.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hisaab.presentation.navigation.BottomNavItem
import com.hisaab.presentation.ui.theme.HisaabTheme

/**
 * HisaabBottomNav — 5-item bar: Home, Transactions, [+ FAB], Insights, Agent.
 *
 * DESIGN.md rules:
 *  - Background: BgSecondary with 1px top border in border-subtle
 *  - Active icon + label: Purple (#7B61FF) — only active item
 *  - Inactive: TextMuted
 *  - FAB center: 56dp purple circle, NO shadow (tonal only)
 *  - Zero Material shadow (elevation = 0)
 *
 * Design spells:
 *  - Active tab: scale pulse (1.0→1.08→1.0) on selection
 *  - FAB: rotation spring on tap (0→15°→0°)
 *  - Active indicator: pill underline slides between tabs
 */
@Composable
fun HisaabBottomNav(
    navController  : NavHostController,
    onFabClick     : () -> Unit,
    modifier       : Modifier = Modifier,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val items = BottomNavItem.values()

    Surface(
        modifier   = modifier.fillMaxWidth(),
        color      = HisaabTheme.BgSecondary,
        tonalElevation = 0.dp,    // NO Material shadow
        shadowElevation = 0.dp,
    ) {
        Column {
            // 1px top border (design token)
            HorizontalDivider(
                color     = HisaabTheme.BorderSubtle,
                thickness = 1.dp,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .selectableGroup()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left two tabs
                items.take(2).forEach { item ->
                    NavTab(
                        item      = item,
                        isActive  = currentRoute == item.route,
                        onClick   = { navController.navigateSingleTop(item.route) },
                        modifier  = Modifier.weight(1f),
                    )
                }

                // Centre FAB slot
                FabSlot(
                    onClick  = onFabClick,
                    modifier = Modifier.weight(1f),
                )

                // Right two tabs
                items.takeLast(2).forEach { item ->
                    NavTab(
                        item     = item,
                        isActive = currentRoute == item.route,
                        onClick  = { navController.navigateSingleTop(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── Nav tab ───────────────────────────────────────────────────────────────────

@Composable
private fun NavTab(
    item    : BottomNavItem,
    isActive: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = navIcon(item)

    // Design spell: scale pulse on activation
    val scale by animateFloatAsState(
        targetValue   = if (isActive) 1.0f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "tab_scale",
    )

    val tint = if (isActive) HisaabTheme.Purple else HisaabTheme.TextMuted

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Active indicator pill (underline, slides in)
        AnimatedVisibility(
            visible = isActive,
            enter   = scaleIn(tween(150)) + fadeIn(tween(150)),
            exit    = scaleOut(tween(100)) + fadeOut(tween(100)),
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(HisaabTheme.RadiusPill))
                    .background(HisaabTheme.Purple),
            )
        }
        if (!isActive) Spacer(Modifier.height(3.dp))

        Spacer(Modifier.height(4.dp))

        Icon(
            imageVector        = icon,
            contentDescription = item.label,
            tint               = tint,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = item.label,
            style = HisaabTheme.TypographyCaption.copy(
                color    = tint,
                fontSize = 10.sp,
            ),
        )
    }
}

// ── Centre FAB ────────────────────────────────────────────────────────────────

@Composable
private fun FabSlot(onClick: () -> Unit, modifier: Modifier = Modifier) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue   = if (rotated) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label         = "fab_rotation",
        finishedListener = { rotated = false },
    )

    Box(
        modifier          = modifier.fillMaxHeight(),
        contentAlignment  = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(HisaabTheme.Purple)
                .clickable {
                    rotated = true
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Quick add",
                tint     = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotation },
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun navIcon(item: BottomNavItem): ImageVector = when (item) {
    BottomNavItem.HOME         -> Icons.Default.Home
    BottomNavItem.TRANSACTIONS -> Icons.Default.Receipt
    BottomNavItem.INSIGHTS     -> Icons.Default.Lightbulb
    BottomNavItem.AGENT        -> Icons.Default.Psychology
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
}
