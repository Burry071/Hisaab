package com.hisaab.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.presentation.ui.components.BrandLogoImage
import com.hisaab.presentation.ui.theme.HisaabTheme
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * TransactionRow — Refactored to premium card-based layout.
 *
 * DESIGN SPEC:
 *  - Pill profile RoundedCornerShape(24.dp)
 *  - Card background HisaabTheme.Surface (#161920)
 *  - 1px outline HisaabTheme.BorderSubtle (#222733)
 *  - Leading BrandLogoImage (42dp)
 *  - Transaction status/amount on the right
 */
@Composable
fun TransactionRow(
    transaction: ParsedTransaction,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier,
    showDivider: Boolean = true, // Ignored in card-based layout
) {
    val isCredit     = transaction.type == TransactionType.CREDIT
    val amountColor  = if (isCredit) HisaabTheme.Teal else HisaabTheme.TextPrimary
    val amountPrefix = if (isCredit) "+" else "−"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(HisaabTheme.RadiusXl))
            .background(HisaabTheme.Surface)
            .border(1.dp, HisaabTheme.BorderSubtle, RoundedCornerShape(HisaabTheme.RadiusXl))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.weight(1f)
        ) {
            // Live BrandLogoImage container
            BrandLogoImage(
                institutionName = transaction.institution,
                size            = 42.dp,
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text     = displayName(transaction),
                    style    = HisaabTheme.TypographyBody.copy(
                        color      = HisaabTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp,
                    ),
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = "${transaction.institution} · ${formatTimestamp(transaction.timestampEpochMs)}",
                    style = HisaabTheme.TypographyCaption.copy(
                        color    = HisaabTheme.TextSecondary,
                        fontSize = 12.sp
                    ),
                )
            }
        }

        // Crisp right-aligned transaction parameters
        Text(
            text  = "$amountPrefix PKR ${formatAmount(transaction.amount)}",
            style = HisaabTheme.TypographyBody.copy(
                color      = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
            ),
            textAlign = TextAlign.End,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun displayName(tx: ParsedTransaction): String =
    tx.counterparty?.takeIf { it.isNotBlank() }
        ?: when (tx.type) {
            TransactionType.BILL_PAYMENT    -> "Bill Payment"
            TransactionType.TRANSFER        -> "Transfer"
            TransactionType.CREDIT          -> "Credit Received"
            TransactionType.DEBIT           -> "Debit"
            else                            -> tx.institution
        }

private fun formatAmount(amount: BigDecimal): String {
    val long = amount.toLong()
    return when {
        long >= 1_000_000 -> "${"%.2f".format(long / 1_000_000.0)}M"
        long >= 1_000     -> "%,d".format(long)
        else              -> long.toString()
    }
}

private val TIME_FMT  = SimpleDateFormat("HH:mm", Locale.getDefault())
private val DATE_FMT  = SimpleDateFormat("MMM d", Locale.getDefault())

private fun formatTimestamp(epochMs: Long): String {
    val now   = System.currentTimeMillis()
    val delta = now - epochMs
    return when {
        delta < 24 * 60 * 60 * 1000L -> TIME_FMT.format(Date(epochMs))
        delta < 7  * 24 * 60 * 60 * 1000L -> DATE_FMT.format(Date(epochMs))
        else -> DATE_FMT.format(Date(epochMs))
    }
}
