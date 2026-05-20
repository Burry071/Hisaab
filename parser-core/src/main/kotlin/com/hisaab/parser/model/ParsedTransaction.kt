package com.hisaab.parser.model

import java.math.BigDecimal

enum class TransactionType { DEBIT, CREDIT, TRANSFER, BILL_PAYMENT, TOP_UP, UNKNOWN }
enum class IngestionSource { SMS, NOTIFICATION, PDF, MANUAL }

/**
 * Canonical output produced by every wallet/bank parser.
 * Raw SMS fields are normalised into this contract so the
 * domain layer never needs to know about the originating source.
 */
data class ParsedTransaction(
    val id: String,                          // deterministic hash of (sender+body+timestamp)
    val source: IngestionSource,
    val institution: String,                 // e.g. "JazzCash", "Easypaisa"
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String = "PKR",
    val balanceAfter: BigDecimal?,           // null when not present in SMS
    val counterparty: String?,               // recipient name / merchant name
    val referenceNumber: String?,
    val rawSmsBody: String,
    val timestampEpochMs: Long,
    val confidenceScore: Float,              // 0.0 – 1.0; <0.6 → Tier 3 fallback
)
