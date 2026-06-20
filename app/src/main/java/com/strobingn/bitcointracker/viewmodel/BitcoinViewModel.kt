package com.strobingn.bitcointracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strobingn.bitcointracker.data.BitcoinRepository
import com.strobingn.bitcointracker.data.PriceAlert
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BitcoinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BitcoinRepository(application)

    val uiState: StateFlow<com.strobingn.bitcointracker.data.BitcoinUiState> = repository.uiState

    fun addAlert(price: Double, isAbove: Boolean) {
        repository.addPriceAlert(price, isAbove)
    }

    fun removeAlert(alert: PriceAlert) {
        repository.removePriceAlert(alert)
    }

    fun reconnect() {
        repository.connectAllStreams()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectAll()
    }
}