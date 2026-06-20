package com.strobingn.bitcointracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.strobingn.bitcointracker.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bitcoin_prefs")

class BitcoinRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _uiState = MutableStateFlow(BitcoinUiState())
    val uiState: StateFlow<BitcoinUiState> = _uiState.asStateFlow()

    private val priceHistory = mutableListOf<Double>()
    private val recentTrades = mutableListOf<Trade>()
    private val orderBookBids = mutableListOf<OrderBookEntry>()
    private val orderBookAsks = mutableListOf<OrderBookEntry>()
    private val activeAlerts = mutableListOf<PriceAlert>()

    private var tickerWs: WebSocket? = null
    private var tradeWs: WebSocket? = null
    private var depthWs: WebSocket? = null
    private var reconnectJob: Job? = null

    private val prefs = context.dataStore
    private val ALERTS_KEY = stringSetPreferencesKey("price_alerts")
    private val PORTFOLIO_KEY = stringPreferencesKey("portfolio_btc_amount")
    private val ENTRY_PRICE_KEY = doublePreferencesKey("portfolio_entry_price")

    init {
        loadPersistedData()
        connectAllStreams()
    }

    private fun loadPersistedData() {
        // Load alerts and portfolio from DataStore in real impl (simplified here for demo)
        // In production: collect from prefs and populate activeAlerts + portfolio
    }

    fun connectAllStreams() {
        disconnectAll()
        connectTicker()
        connectTrades()
        connectOrderBook()
    }

    private fun connectTicker() {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/btcusdt@ticker")
            .build()

        tickerWs = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                updateConnection(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseTicker(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                updateConnection(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                updateConnection(false)
                scheduleReconnect()
            }
        })
    }

    private fun connectTrades() {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/btcusdt@trade")
            .build()

        tradeWs = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                parseTrade(text)
            }
        })
    }

    private fun connectOrderBook() {
        // Top 20 depth
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/btcusdt@depth20@100ms")
            .build()

        depthWs = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                parseDepth(text)
            }
        })
    }

    private fun parseTicker(json: String) {
        try {
            val obj = JSONObject(json)
            val price = obj.getDouble("c")
            val change = obj.getDouble("p")
            val changePercent = obj.getDouble("P")
            val high = obj.getDouble("h")
            val low = obj.getDouble("l")
            val volume = obj.getDouble("v")

            priceHistory.add(price)
            if (priceHistory.size > 120) priceHistory.removeAt(0)

            // Calculate simple indicators
            val rsi = calculateRSI(priceHistory)
            val sma20 = if (priceHistory.size >= 20) priceHistory.takeLast(20).average() else price
            val ema9 = calculateEMA(priceHistory, 9)

            val newState = _uiState.value.copy(
                currentPrice = price,
                priceChange24h = change,
                priceChangePercent24h = changePercent,
                high24h = high,
                low24h = low,
                volume24h = volume,
                priceHistory = priceHistory.toList(),
                rsi = rsi,
                sma20 = sma20,
                ema9 = ema9,
                isConnected = true
            )
            _uiState.value = newState

            checkAlerts(price)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseTrade(json: String) {
        try {
            val obj = JSONObject(json)
            val price = obj.getDouble("p")
            val qty = obj.getDouble("q")
            val isBuyerMaker = obj.getBoolean("m") // true = sell (taker buy? wait, m=true means buyer is maker i.e. sell pressure)

            val trade = Trade(
                price = price,
                quantity = qty,
                isBuyerMaker = isBuyerMaker,
                time = System.currentTimeMillis()
            )

            recentTrades.add(0, trade)
            if (recentTrades.size > 30) recentTrades.removeLast()

            _uiState.value = _uiState.value.copy(recentTrades = recentTrades.toList())
        } catch (e: Exception) {}
    }

    private fun parseDepth(json: String) {
        try {
            val obj = JSONObject(json)
            val bidsJson = obj.getJSONArray("bids")
            val asksJson = obj.getJSONArray("asks")

            orderBookBids.clear()
            orderBookAsks.clear()

            for (i in 0 until minOf(bidsJson.length(), 10)) {
                val arr = bidsJson.getJSONArray(i)
                orderBookBids.add(OrderBookEntry(arr.getDouble(0), arr.getDouble(1)))
            }
            for (i in 0 until minOf(asksJson.length(), 10)) {
                val arr = asksJson.getJSONArray(i)
                orderBookAsks.add(OrderBookEntry(arr.getDouble(0), arr.getDouble(1)))
            }

            _uiState.value = _uiState.value.copy(
                bids = orderBookBids.toList(),
                asks = orderBookAsks.toList()
            )
        } catch (e: Exception) {}
    }

    private fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
        if (prices.size < period + 1) return 50.0
        val changes = prices.takeLast(period + 1).zipWithNext { a, b -> b - a }
        val gains = changes.map { if (it > 0) it else 0.0 }
        val losses = changes.map { if (it < 0) abs(it) else 0.0 }
        val avgGain = gains.average()
        val avgLoss = losses.average()
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100 - (100 / (1 + rs))
    }

    private fun calculateEMA(prices: List<Double>, period: Int): Double {
        if (prices.size < period) return prices.lastOrNull() ?: 0.0
        val k = 2.0 / (period + 1)
        var ema = prices.take(period).average()
        for (price in prices.drop(period)) {
            ema = price * k + ema * (1 - k)
        }
        return ema
    }

    private fun checkAlerts(currentPrice: Double) {
        activeAlerts.forEach { alert ->
            if (!alert.triggered && 
                ((alert.isAbove && currentPrice >= alert.price) || 
                 (!alert.isAbove && currentPrice <= alert.price))) {
                alert.triggered = true
                triggerPriceAlertNotification(alert, currentPrice)
            }
        }
        // Clean triggered
        activeAlerts.removeAll { it.triggered }
    }

    private fun triggerPriceAlertNotification(alert: PriceAlert, currentPrice: Double) {
        // In real app: build notification and notify
        // For now we just update state so UI can show in-app alert
        val msg = "BTC ${if (alert.isAbove) "above" else "below"} $${"%.2f".format(alert.price)}! Current: $${"%.2f".format(currentPrice)}"
        // You would use NotificationCompat here with context
        println("ALERT TRIGGERED: $msg") // placeholder
    }

    fun addPriceAlert(price: Double, isAbove: Boolean) {
        activeAlerts.add(PriceAlert(price, isAbove))
        // Persist to DataStore in real version
    }

    fun removePriceAlert(alert: PriceAlert) {
        activeAlerts.remove(alert)
    }

    private fun updateConnection(connected: Boolean) {
        _uiState.value = _uiState.value.copy(isConnected = connected)
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            connectAllStreams()
        }
    }

    fun disconnectAll() {
        tickerWs?.close(1000, "User disconnect")
        tradeWs?.close(1000, "User disconnect")
        depthWs?.close(1000, "User disconnect")
        reconnectJob?.cancel()
    }

    // Portfolio & Alerts persistence would go here with DataStore.edit { ... }

    override fun onCleared() {
        disconnectAll()
    }
}

// Domain models
data class Trade(
    val price: Double,
    val quantity: Double,
    val isBuyerMaker: Boolean,
    val time: Long
)

data class OrderBookEntry(val price: Double, val quantity: Double)

data class PriceAlert(
    val price: Double,
    val isAbove: Boolean,
    var triggered: Boolean = false
)

data class BitcoinUiState(
    val currentPrice: Double = 0.0,
    val priceChange24h: Double = 0.0,
    val priceChangePercent24h: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val priceHistory: List<Double> = emptyList(),
    val recentTrades: List<Trade> = emptyList(),
    val bids: List<OrderBookEntry> = emptyList(),
    val asks: List<OrderBookEntry> = emptyList(),
    val rsi: Double = 50.0,
    val sma20: Double = 0.0,
    val ema9: Double = 0.0,
    val isConnected: Boolean = false,
    val portfolioBtc: Double = 0.0,
    val portfolioEntryPrice: Double = 0.0
)