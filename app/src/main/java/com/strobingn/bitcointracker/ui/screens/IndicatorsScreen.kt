package com.strobingn.bitcointracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strobingn.bitcointracker.viewmodel.BitcoinViewModel

@Composable
fun IndicatorsScreen(viewModel: BitcoinViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Technical Indicators", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        IndicatorCard(
            title = "RSI (14)",
            value = String.format("%.1f", state.rsi),
            interpretation = when {
                state.rsi > 70 -> "Overbought - Consider taking profit"
                state.rsi < 30 -> "Oversold - Potential bounce"
                else -> "Neutral"
            },
            color = when {
                state.rsi > 70 -> Color(0xFFFF1744)
                state.rsi < 30 -> Color(0xFF00C853)
                else -> Color(0xFFF7931A)
            }
        )

        IndicatorCard(
            title = "SMA 20",
            value = formatPrice(state.sma20),
            interpretation = if (state.currentPrice > state.sma20) "Price above SMA - Bullish" else "Price below SMA - Bearish",
            color = if (state.currentPrice > state.sma20) Color(0xFF00C853) else Color(0xFFFF1744)
        )

        IndicatorCard(
            title = "EMA 9",
            value = formatPrice(state.ema9),
            interpretation = if (state.currentPrice > state.ema9) "Above short-term EMA" else "Below short-term EMA",
            color = if (state.currentPrice > state.ema9) Color(0xFF00C853) else Color(0xFFFF1744)
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "Indicators calculated live from Binance ticker stream. For production trading add more periods + volume profile.",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
fun IndicatorCard(title: String, value: String, interpretation: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Color.Gray, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(interpretation, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}