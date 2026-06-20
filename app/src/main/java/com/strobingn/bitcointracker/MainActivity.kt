package com.strobingn.bitcointracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strobingn.bitcointracker.ui.theme.BitcoinTrackerTheme
import java.text.NumberFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BitcoinTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BitcoinTrackerScreen()
                }
            }
        }
    }
}

@Composable
fun BitcoinTrackerScreen(viewModel: BitcoinViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Bitcoin Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF7931A),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Connection Status
        ConnectionStatusIndicator(isConnected = uiState.isConnected)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Price Display
        PriceCard(
            price = uiState.currentPrice,
            priceChange = uiState.priceChange24h,
            priceChangePercent = uiState.priceChangePercent24h
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Price History Chart (Simple visualization)
        Text(
            text = "Price History",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        PriceHistoryChart(priceHistory = uiState.priceHistory)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent Trades
        Text(
            text = "Recent Trades",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        RecentTradesList(trades = uiState.recentTrades)
    }
}

@Composable
fun ConnectionStatusIndicator(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isConnected) Color(0xFF00C853) else Color(0xFFFF1744),
                    shape = MaterialTheme.shapes.small
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isConnected) "Live" else "Disconnected",
            color = if (isConnected) Color(0xFF00C853) else Color(0xFFFF1744),
            fontSize = 14.sp
        )
    }
}

@Composable
fun PriceCard(price: Double, priceChange: Double, priceChangePercent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BTC / USD",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = formatPrice(price),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isPositive = priceChange >= 0
                val color = if (isPositive) Color(0xFF00C853) else Color(0xFFFF1744)
                val sign = if (isPositive) "+" else ""
                
                Text(
                    text = "$sign${formatPrice(priceChange)} ($sign${String.format("%.2f", priceChangePercent)}%)",
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PriceHistoryChart(priceHistory: List<Double>) {
    if (priceHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF16213E)),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading data...", color = Color.Gray)
        }
        return
    }
    
    val minPrice = priceHistory.minOrNull() ?: 0.0
    val maxPrice = priceHistory.maxOrNull() ?: 0.0
    val range = maxPrice - minPrice
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(0xFF16213E))
            .padding(8.dp)
    ) {
        // Simple bar chart visualization
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            priceHistory.takeLast(30).forEach { price ->
                val normalizedHeight = if (range > 0) {
                    ((price - minPrice) / range).toFloat()
                } else 0.5f
                
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(normalizedHeight.coerceIn(0.1f, 1f))
                        .background(Color(0xFFF7931A))
                )
            }
        }
    }
}

@Composable
fun RecentTradesList(trades: List<Trade>) {
    if (trades.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Waiting for trades...", color = Color.Gray)
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(trades.take(10)) { trade ->
            TradeItem(trade = trade)
        }
    }
}

@Composable
fun TradeItem(trade: Trade) {
    val isBuy = trade.isBuyerMaker
    val color = if (isBuy) Color(0xFF00C853) else Color(0xFFFF1744)
    val type = if (isBuy) "BUY" else "SELL"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16213E))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        
        Text(
            text = formatPrice(trade.price),
            color = Color.White,
            fontSize = 14.sp
        )
        
        Text(
            text = String.format("%.6f", trade.quantity),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price)
}
