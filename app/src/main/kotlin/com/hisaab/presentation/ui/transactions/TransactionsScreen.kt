package com.hisaab.presentation.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.presentation.ui.home.TransactionRow
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * TransactionsScreen — full paginated transaction timeline.
 *
 * Groups transactions by date. Each group is a sticky header
 * with the daily total beneath. TransactionRow is reused from home.
 * Filter chips for: ALL | INCOME | EXPENSE | TRANSFER.
 */
@kotlin.OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    transactions : List<ParsedTransaction>,
    onBack       : () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(TxnFilter.ALL) }

    val filtered = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            TxnFilter.ALL      -> transactions
            TxnFilter.INCOME   -> transactions.filter { it.type == TransactionType.CREDIT }
            TxnFilter.EXPENSE  -> transactions.filter { it.type == TransactionType.DEBIT }
            TxnFilter.TRANSFER -> transactions.filter { it.type == TransactionType.TRANSFER }
        }
    }

    val grouped = remember(filtered) {
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        filtered.sortedByDescending { it.timestampEpochMs }
            .groupBy { fmt.format(Date(it.timestampEpochMs)) }
            .toList()
    }

    Column(modifier = Modifier.fillMaxSize().background(HisaabTheme.BgBase)) {
        TxnTopBar(onBack = onBack, count = filtered.size)

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgSecondary)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TxnFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick  = { selectedFilter = filter },
                    label    = { Text(filter.label, fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HisaabTheme.Purple.copy(alpha = 0.2f),
                        selectedLabelColor     = HisaabTheme.Purple,
                        containerColor         = HisaabTheme.Surface,
                        labelColor             = HisaabTheme.TextSecondary,
                    ),
                )
            }
        }

        if (filtered.isEmpty()) {
            TxnEmptyState()
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                grouped.forEach { (date, txns) ->
                    val dailyTotal = txns.sumOf {
                        if (it.type == TransactionType.CREDIT) it.amount.toLong()
                        else -it.amount.toLong()
                    }
                    stickyHeader(key = "header_$date") {
                        DateGroupHeader(date = date, netAmount = dailyTotal)
                    }
                    item(key = "group_$date") {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
                                .background(HisaabTheme.BgSecondary),
                        ) {
                            txns.forEachIndexed { idx, tx ->
                                TransactionRow(
                                    transaction = tx,
                                    showDivider = idx < txns.lastIndex,
                                    onClick     = {},
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TxnTopBar(onBack: () -> Unit, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgSecondary)
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back", tint = HisaabTheme.TextSecondary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Transactions", style = HisaabTheme.TypographyTitle.copy(
                color = HisaabTheme.TextPrimary, fontWeight = FontWeight.SemiBold))
            Text("$count transactions", style = HisaabTheme.TypographyTrace.copy(color = HisaabTheme.TextMuted))
        }
        Icon(Icons.Default.Search, "Search", tint = HisaabTheme.TextSecondary)
    }
}

@Composable
private fun DateGroupHeader(date: String, netAmount: Long) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HisaabTheme.BgBase)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(date, style = HisaabTheme.TypographyTrace.copy(
            color = HisaabTheme.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold))
        Text(
            if (netAmount >= 0) "+PKR ${"%,d".format(netAmount)}" else "PKR ${"%,d".format(-netAmount)}",
            style = HisaabTheme.TypographyTrace.copy(
                color = if (netAmount >= 0) HisaabTheme.Teal else HisaabTheme.Red,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
private fun TxnEmptyState() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("📭", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("No transactions", style = HisaabTheme.TypographyTitle.copy(color = HisaabTheme.TextPrimary))
        Text("Grant SMS permission or run demo mode.", style = HisaabTheme.TypographyBody.copy(color = HisaabTheme.TextSecondary))
    }
}

enum class TxnFilter(val label: String) {
    ALL("All"), INCOME("Income"), EXPENSE("Expense"), TRANSFER("Transfer")
}
