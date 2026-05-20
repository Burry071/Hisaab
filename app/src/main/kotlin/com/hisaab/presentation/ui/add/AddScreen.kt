package com.hisaab.presentation.ui.add

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.navigation.NavHostController
import com.hisaab.presentation.viewmodels.AddTransactionViewModel
import com.hisaab.presentation.viewmodels.HomeViewModel
import com.hisaab.data.local.TransactionEntity

// ── Entry type ────────────────────────────────────────────────────────────────

enum class EntryType(val label: String, val emoji: String) {
    EXPENSE("Expense", "↑"),
    INCOME("Income",   "↓"),
    TRANSFER("Transfer","⇄"),
}

// ── Preset categories per type ────────────────────────────────────────────────

private val expenseCategories  = listOf("Food","Transport","Shopping","Utilities","Health","Entertainment","Other")
private val incomeCategories   = listOf("Salary","Freelance","Business","Gift","Refund","Other")
private val transferCategories = listOf("Bank Transfer","JazzCash","Easypaisa","SadaPay","NayaPay","Other")

private fun categoriesFor(type: EntryType) = when (type) {
    EntryType.EXPENSE  -> expenseCategories
    EntryType.INCOME   -> incomeCategories
    EntryType.TRANSFER -> transferCategories
}

/**
 * AddScreen — manual transaction entry.
 *
 * Three tabs: Expense / Income / Transfer.
 * Pre-fills today's date and current time.
 * FAB in HisaabBottomNav passes initialType = "EXPENSE" by default.
 *
 * Spendora design rules:
 *  - Amount field is LARGE and centred (like a native calculator)
 *  - Tab row uses HisaabTheme token colours — no Material defaults
 *  - Zero elevation, tonal surfaces only
 *  - CTA button has violet → indigo gradient fill
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    initialType  : EntryType = EntryType.EXPENSE,
    viewModel    : AddTransactionViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    onBack       : () -> Unit = {},
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var amount       by remember { mutableStateOf("") }
    var merchant     by remember { mutableStateOf("") }
    var category     by remember { mutableStateOf(categoriesFor(initialType)[0]) }
    var note         by remember { mutableStateOf("") }

    // Pre-fill current date/time
    val now         = remember { LocalDateTime.now() }
    val dateFmt     = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val timeFmt     = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    var displayDate by remember { mutableStateOf(now.format(dateFmt)) }
    var displayTime by remember { mutableStateOf(now.format(timeFmt)) }
    var selectedMs  by remember { mutableStateOf(System.currentTimeMillis()) }

    // Reset category when type switches
    LaunchedEffect(selectedType) {
        category = categoriesFor(selectedType)[0]
    }

    val accentColor = when (selectedType) {
        EntryType.EXPENSE  -> HisaabTheme.Red
        EntryType.INCOME   -> HisaabTheme.Teal
        EntryType.TRANSFER -> HisaabTheme.Purple
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HisaabTheme.BgBase)
            .verticalScroll(rememberScrollState()),
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
        AddTopBar(onBack = onBack)

        // ── Type selector tabs ────────────────────────────────────────────────
        TypeTabRow(
            selected  = selectedType,
            onSelect  = { selectedType = it },
        )

        Spacer(Modifier.height(32.dp))

        // ── Big amount field ──────────────────────────────────────────────────
        AmountInput(
            value       = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            accentColor = accentColor,
        )

        Spacer(Modifier.height(28.dp))

        // ── Form fields ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            HisaabTextField(
                value         = merchant,
                onValueChange = { merchant = it },
                label         = if (selectedType == EntryType.TRANSFER) "Recipient / Sender" else "Merchant / Description",
                leadingIcon   = Icons.Default.Store,
            )

            CategoryPicker(
                categories     = categoriesFor(selectedType),
                selected       = category,
                onSelect       = { category = it },
                accentColor    = accentColor,
            )

            // Date + Time row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HisaabTextField(
                    value         = displayDate,
                    onValueChange = { displayDate = it },
                    label         = "Date",
                    leadingIcon   = Icons.Default.CalendarToday,
                    modifier      = Modifier.weight(1f),
                )
                HisaabTextField(
                    value         = displayTime,
                    onValueChange = { displayTime = it },
                    label         = "Time",
                    leadingIcon   = Icons.Default.Schedule,
                    modifier      = Modifier.weight(1f),
                )
            }

            HisaabTextField(
                value         = note,
                onValueChange = { note = it },
                label         = "Note (optional)",
                leadingIcon   = Icons.Default.Notes,
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── Save CTA ──────────────────────────────────────────────────────────
        SaveButton(
            selectedType = selectedType,
            enabled      = amount.isNotBlank() && amount.toDoubleOrNull() != null,
            onClick      = {
                // 1. Construct the TransactionEntity data object
                val amountLong = amount.toDoubleOrNull()?.toLong() ?: 0L
                val manualExpense = TransactionEntity(
                    merchantName = merchant.ifBlank { selectedType.label },
                    amount       = amountLong,
                    type         = selectedType.name,
                    category     = category,
                    note         = note.ifBlank { null },
                    timestampMs  = selectedMs
                )

                // 2. Explicitly invoke the write method on the ViewModel
                viewModel.addNewManualTransaction(manualExpense) {
                    // 3. Call a forced update on the primary home view model to refresh transactions
                    homeViewModel.refresh()
                    
                    // 4. Safely execute navController.popBackStack()
                    navController.popBackStack()
                }
            },
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AddTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HisaabTheme.BgSecondary)
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, "Close", tint = HisaabTheme.TextSecondary)
        }
        Text(
            text     = "Add Transaction",
            modifier = Modifier.weight(1f),
            style    = HisaabTheme.TypographyTitle.copy(
                color      = HisaabTheme.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

// ── Type tab row ──────────────────────────────────────────────────────────────

@Composable
private fun TypeTabRow(
    selected : EntryType,
    onSelect : (EntryType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HisaabTheme.BgSecondary)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EntryType.values().forEach { type ->
            val isActive = type == selected
            val bgColor by animateColorAsState(
                targetValue = if (isActive) when (type) {
                    EntryType.EXPENSE  -> HisaabTheme.Red.copy(alpha = 0.18f)
                    EntryType.INCOME   -> HisaabTheme.Teal.copy(alpha = 0.18f)
                    EntryType.TRANSFER -> HisaabTheme.Purple.copy(alpha = 0.18f)
                } else HisaabTheme.Surface,
                animationSpec = tween(200),
                label         = "tab_bg",
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) when (type) {
                    EntryType.EXPENSE  -> HisaabTheme.Red
                    EntryType.INCOME   -> HisaabTheme.Teal
                    EntryType.TRANSFER -> HisaabTheme.Purple
                } else HisaabTheme.TextMuted,
                animationSpec = tween(200),
                label         = "tab_text",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
                    .background(bgColor)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = textColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(HisaabTheme.RadiusMd),
                        ) else Modifier
                    )
                    .clickable { onSelect(type) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "${type.emoji}  ${type.label}",
                    style = HisaabTheme.TypographyBody.copy(
                        color      = textColor,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize   = 13.sp,
                    ),
                )
            }
        }
    }
}

// ── Big amount input ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountInput(
    value        : String,
    onValueChange: (String) -> Unit,
    accentColor  : Color,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "PKR",
            style = HisaabTheme.TypographyCaption.copy(
                color    = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(Modifier.height(4.dp))

        val displayText = if (value.isBlank()) "0" else value
        TextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            textStyle     = LocalTextStyle.current.copy(
                fontSize  = 48.sp,
                fontWeight= FontWeight.Bold,
                color     = HisaabTheme.TextPrimary,
                textAlign = TextAlign.Center,
            ),
            placeholder = {
                Text(
                    "0",
                    modifier  = Modifier.fillMaxWidth(),
                    fontSize  = 48.sp,
                    fontWeight= FontWeight.Bold,
                    color     = HisaabTheme.TextMuted.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = accentColor,
                unfocusedIndicatorColor = HisaabTheme.BorderSubtle,
                cursorColor             = accentColor,
                focusedTextColor        = HisaabTheme.TextPrimary,
                unfocusedTextColor      = HisaabTheme.TextPrimary,
            ),
            singleLine = true,
        )
    }
}

// ── Generic text field ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HisaabTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    label        : String,
    leadingIcon  : androidx.compose.ui.graphics.vector.ImageVector,
    modifier     : Modifier = Modifier,
) {
    TextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 12.sp) },
        leadingIcon   = {
            Icon(leadingIcon, null, tint = HisaabTheme.TextMuted, modifier = Modifier.size(18.dp))
        },
        modifier      = modifier.fillMaxWidth(),
        singleLine    = true,
        colors        = TextFieldDefaults.colors(
            focusedContainerColor   = HisaabTheme.Surface,
            unfocusedContainerColor = HisaabTheme.Surface,
            focusedIndicatorColor   = HisaabTheme.Purple,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor             = HisaabTheme.Purple,
            focusedTextColor        = HisaabTheme.TextPrimary,
            unfocusedTextColor      = HisaabTheme.TextPrimary,
            focusedLabelColor       = HisaabTheme.Purple,
            unfocusedLabelColor     = HisaabTheme.TextMuted,
        ),
        shape = RoundedCornerShape(HisaabTheme.RadiusMd),
    )
}

// ── Category chips ────────────────────────────────────────────────────────────

@Composable
private fun CategoryPicker(
    categories  : List<String>,
    selected    : String,
    onSelect    : (String) -> Unit,
    accentColor : Color,
) {
    Column {
        Text(
            "Category",
            style    = HisaabTheme.TypographyCaption.copy(
                color = HisaabTheme.TextMuted, fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        // Wrap chips manually (FlowRow not needed for ≤8 items — just two rows of 4)
        val chunked = categories.chunked(4)
        chunked.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cat ->
                    val isActive = cat == selected
                    val bgColor by animateColorAsState(
                        targetValue = if (isActive) accentColor.copy(alpha = 0.2f) else HisaabTheme.Surface,
                        label = "cat_bg",
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isActive) accentColor else HisaabTheme.TextMuted,
                        label = "cat_text",
                    )
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(HisaabTheme.RadiusPill))
                            .background(bgColor)
                            .then(
                                if (isActive) Modifier.border(
                                    1.dp, accentColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(HisaabTheme.RadiusPill),
                                ) else Modifier
                            )
                            .clickable { onSelect(cat) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            cat,
                            style = HisaabTheme.TypographyCaption.copy(
                                color      = textColor,
                                fontSize   = 12.sp,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Save button ───────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(
    selectedType : EntryType,
    enabled      : Boolean,
    onClick      : () -> Unit,
) {
    val gradientColors = when (selectedType) {
        EntryType.EXPENSE  -> listOf(Color(0xFFE53935), Color(0xFFAD1457))
        EntryType.INCOME   -> listOf(Color(0xFF00BCD4), Color(0xFF00796B))
        EntryType.TRANSFER -> listOf(Color(0xFF7B61FF), Color(0xFF4527A0))
    }

    val alphaAnim by animateFloatAsState(
        targetValue   = if (enabled) 1f else 0.4f,
        animationSpec = tween(200),
        label         = "save_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(HisaabTheme.RadiusMd))
            .background(
                Brush.horizontalGradient(
                    gradientColors.map { it.copy(alpha = alphaAnim) }
                )
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(20.dp),
            )
            Text(
                text  = "Save ${selectedType.label}",
                style = HisaabTheme.TypographyBody.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                ),
            )
        }
    }
}
