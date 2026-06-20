package com.strobingn.bitcointracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

data class Trade(
    val price: Double,
    val quantity: Double,
    val isBuyerMaker: Boolean,
    val time: Long
)

data class BitcoinUiState(
    val currentPrice: Double = 0.0,
    val priceChange24h: Double = 0.0,
    val priceChangePercent24h: Double = 0.0,
    val priceHistory: List<Double> = emptyList(),
    val recentTrades: List<Trade> = emptyList(),
    val isConnected: Boolean = false
)

class BitcoinViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(BitcoinUiState())
    val uiState: StateFlow<BitcoinUiState> = _uiState.asStateFlow()
    
    private var webSocketClient: WebSocketClient? = null
    private val priceHistory = mutableListOf<Double>()
    private val recentTrades = mutableListOf<Trade>()
    
    // Using Binance WebSocket API for BTC/USDT
    private val wsUrl = "wss://stream.binance.com:9443/ws/btcusdt@ticker"
    private val tradeWsUrl = "wss://stream.binance.com:9443/ws/btcusdt@trade"
    
    init {
        connectToWebSocket()
    }
    
    private fun connectToWebSocket() {
        viewModelScope.launch {
            try {
                // Connect to ticker stream for price data
                val tickerClient = object : WebSocketClient(URI(wsUrl)) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        updateConnectionStatus(true)
                    }
                    
                    override fun onMessage(message: String?) {
                        message?.let { parseTickerData(it) }
                    }
                    
                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        updateConnectionStatus(false)
                        // Reconnect after delay
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(5000)
                            connectToWebSocket()
                        }
                    }
                    
                    override fun onError(ex: Exception?) {
                        updateConnectionStatus(false)
                    }
                }
                
                // Connect to trade stream for recent trades
                val tradeClient = object : WebSocketClient(URI(tradeWsUrl)) {
                    override fun onOpen(handshakedata: ServerHandshake?) {}
                    
                    override fun onMessage(message: String?) {
                        message?.let { parseTradeData(it) }
                    }
                    
                    override fun onClose(code: Int, reason: String?, remote: Boolean) {}
                    
                    override fun onError(ex: Exception?) {}
                }
                
                tickerClient.connect()
                tradeClient.connect()
                
                webSocketClient = tickerClient
                
            } catch (e: Exception) {
                e.printStackTrace()
                updateConnectionStatus(false)
            }
        }
    }
    
    private fun parseTickerData(message: String) {
        try {
            // Parse Binance ticker data
            // Format: {"e":"24hrTicker","E":123456789,"s":"BTCUSDT","p":"-94.99950200","P":"-0.152","w":"13727.53868950","x":"62800.00","c":"62700.00","Q":"0.5","b":"62700.00","B":"0.5","a":"62701.00","A":"0.5","o":"62800.00","h":"64000.00","l":"62000.00","v":"1000.00","q":"13727538.68"}
            
            val priceRegex = """"c":"([^"]+)"""".toRegex()
            val priceChangeRegex = """"p":"([^"]+)"""".toRegex()
            val priceChangePercentRegex = """"P":"([^"]+)"""".toRegex()
            
            val price = priceRegex.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: return
            val priceChange = priceChangeRegex.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val priceChangePercent = priceChangePercentRegex.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            
            priceHistory.add(price)
            if (priceHistory.size > 100) {
                priceHistory.removeAt(0)
            }
            
            _uiState.value = _uiState.value.copy(
                currentPrice = price,
                priceChange24h = priceChange,
                priceChangePercent24h = priceChangePercent,
                priceHistory = priceHistory.toList()
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun parseTradeData(message: String) {
        try {
            // Parse Binance trade data
            // Format: {"e":"trade","E":123456789,"s":"BTCUSDT","t":12345,"p":"62700.00","q":"0.5","b":123,"a":124,"T":123456789,"m":true}
            
            val priceRegex = """"p":"([^"]+)"""".toRegex()
            val quantityRegex = """"q":"([^"]+)"""".toRegex()
            val isBuyerMakerRegex = """"m":(true|false)"""".toRegex()
            val timeRegex = """"T":(\d+)"""".toRegex()
            
            val price = priceRegex.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: return
            val quantity = quantityRegex.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val isBuyerMaker = isBuyerMakerRegex.find(message)?.groupValues?.get(1) == "true"
            val time = timeRegex.find(message)?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()
            
            val trade = Trade(price, quantity, isBuyerMaker, time)
            
            recentTrades.add(0, trade)
            if (recentTrades.size > 50) {
                recentTrades.removeAt(recentTrades.size - 1)
            }
            
            _uiState.value = _uiState.value.copy(
                recentTrades = recentTrades.toList()
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        _uiState.value = _uiState.value.copy(isConnected = connected)
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketClient?.close()
    }
}
