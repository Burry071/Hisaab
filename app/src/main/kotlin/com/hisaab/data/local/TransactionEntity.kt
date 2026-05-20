package com.hisaab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantName: String,
    val amount: Long,
    val type: String,
    val category: String? = null,
    val note: String? = null,
    val timestampMs: Long
)
