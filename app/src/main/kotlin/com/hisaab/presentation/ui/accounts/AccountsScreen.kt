package com.hisaab.presentation.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.math.BigDecimal

/**
 * AccountsScreen — multi-institution unified net worth view (PRD F8).
 *
 * Shows:
 *   ① Net worth hero card (sum of all accounts)
 *   ② Institution account cards (HBL, JazzCash, Meezan, Easypaisa, Cash)
 *   ③ Privacy toggle (masks balances)
 *
 * Uses demo data seeded from DemoModeManager format.
 * Real app pulls from Room DB via AccountsViewModel.
 */
@Composable
fun AccountsScreen(
    accounts  : List<AccountSummary>,
    onBack    : () -> Unit,
) {
    var isPrivate by remember { mutableStateOf(false) }
    val totalBalance = remember(accounts) { accounts.sumOf { it.balance } }

    Column(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase)) {
        AccountsTopBar(onBack = onBack, isPrivate = isPrivate, onPrivacyToggle = { isPrivate = !isPrivate })

        LazyColumn(
            contentPadding      = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Net worth hero
            item(key = "net_worth") {
                NetWorthCard(
                    totalBalance = totalBalance,
                    isPrivate    = isPrivate,
                    accountCount = accounts.size,
                    modifier     = Modifier.padding(16.dp),
                )
            }

            item(key = "accounts_hdr") {
                Text(
                    "YOUR ACCOUNTS",
                    style = HisaabTheme.TypographyTrace.copy(
                        color = HisaabTheme.TextMuted, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }

            items(accounts, key = { it.institutionId }) { account ->
                AccountCard(
                    account   = account,
                    isPrivate = isPrivate,
                    modifier  = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun AccountsTopBar(onBack: () -> Unit, isPrivate: Boolean, onPrivacyToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgSecondary)
            .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back", tint = HisaabTheme.TextSecondary)
        }
        Text("Accounts", modifier = Modifier.weight(1f),
            style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold))
        IconButton(onClick = onPrivacyToggle) {
            Icon(
                if (isPrivate) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                "Toggle privacy", tint = HisaabTheme.TextSecondary,
            )
        }
    }
}

@Composable
private fun NetWorthCard(totalBalance: BigDecimal, isPrivate: Boolean, accountCount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(HisaabTheme.Purple.copy(alpha = 0.3f), HisaabTheme.Surface))
            )
            .border(1.dp, HisaabTheme.Purple.copy(alpha = 0.4f), RoundedCornerShape(HisaabTheme.RadiusXl))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Total Net Worth", style = HisaabTheme.TypographyCaption.copy(color = HisaabTheme.TextMuted))
        Text(
            if (isPrivate) "PKR ••••••" else "PKR ${"%,.0f".format(totalBalance)}",
            style = HisaabTheme.TypographyDisplay.copy(color = HisaabTheme.TextPrimary),
        )
        Text("$accountCount connected accounts",
            style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.Purple, fontSize = 12.sp))
    }
}

@Composable
private fun AccountCard(account: AccountSummary, isPrivate: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(HisaabTheme.BgSecondary)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Institution colour dot
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(account.brandColor.copy(alpha = 0.2f))
                .border(1.dp, account.brandColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(account.initials, style = HisaabTheme.TypographyTrace.copy(
                color = account.brandColor, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(account.institutionName,
                style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold))
            Text(account.accountType,
                style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted, fontSize = 11.sp))
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (isPrivate) "PKR ••••" else "PKR ${"%,.0f".format(account.balance)}",
                style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold),
            )
            Text(account.lastSynced,
                style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted, fontSize = 10.sp))
        }
    }
}

// ── Data model ────────────────────────────────────────────────────────────────

data class AccountSummary(
    val institutionId   : String,
    val institutionName : String,
    val accountType     : String,    // "Savings · HBL"
    val initials        : String,    // "HBL"
    val brandColor      : Color,
    val balance         : BigDecimal,
    val lastSynced      : String,    // "2 min ago"
)

/** Demo accounts matching PRD demo dataset */
val demoAccounts = listOf(
    AccountSummary("hbl",       "HBL",       "Savings · Main",         "HBL", Color(0xFF006B3C), BigDecimal("154_580"), "2 min ago"),
    AccountSummary("jazzcash",  "JazzCash",  "Mobile Wallet",          "JC",  Color(0xFFD4002A), BigDecimal("45_200"),  "2 min ago"),
    AccountSummary("meezan",    "Meezan",    "Savings · Islamic",      "MB",  Color(0xFF006747), BigDecimal("28_800"),  "2 min ago"),
    AccountSummary("easypaisa", "Easypaisa", "Mobile Wallet",          "EP",  Color(0xFF43B02A), BigDecimal("6_000"),   "2 min ago"),
    AccountSummary("cash",      "Cash",      "Cash on hand (manual)",  "₨",   Color(0xFF7B61FF), BigDecimal("0"),       "Manual"),
)
