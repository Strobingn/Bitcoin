package com.strobingn.bitcointracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class BitcoinPriceDataSource {
    
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    private val _priceFlow = MutableStateFlow<BitcoinPrice?>(null)
    val priceFlow: Flow<BitcoinPrice?> = _priceFlow.asStateFlow()
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()
    
    fun connect() {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/btcusdt@ticker")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket) {
                _connectionState.value = ConnectionState.CONNECTED
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val price = BitcoinPrice(
                        currentPrice = json.getString("c").toDouble(),
                        priceChange24h = json.getString("p").toDouble(),
                        priceChangePercent24h = json.getString("P").toDouble(),
                        high24h = json.getString("h").toDouble(),
                        low24h = json.getString("l").toDouble(),
                        volume24h = json.getString("v").toDouble(),
                        lastUpdate = json.getLong("E")
                    )
                    _priceFlow.value = price
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                _connectionState.value = ConnectionState.ERROR
            }
        })
    }
    
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    enum class ConnectionState {
        CONNECTED, DISCONNECTED, ERROR
    }
}

data class BitcoinPrice(
    val currentPrice: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val lastUpdate: Long
)