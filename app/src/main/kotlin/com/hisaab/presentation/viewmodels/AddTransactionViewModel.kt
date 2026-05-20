package com.hisaab.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaab.data.local.TransactionDao
import com.hisaab.data.local.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {

    fun addNewManualTransaction(manualExpense: TransactionEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            transactionDao.insert(manualExpense)
            onComplete()
        }
    }
}
