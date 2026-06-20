package com.strobingn.bitcointracker.ui.screens

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
import com.strobingn.bitcointracker.viewmodel.BitcoinViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun LivePriceScreen(viewModel: BitcoinViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp)
    ) {
        // Price Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BTC / USD", fontSize = 16.sp, color = Color.Gray)
                Text(
                    text = formatPrice(state.currentPrice),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                val isPositive = state.priceChange24h >= 0
                Text(
                    text = "${if (isPositive) "+" else ""}${formatPrice(state.priceChange24h)} (${String.format("%.2f", state.priceChangePercent24h)}%)",
                    fontSize = 20.sp,
                    color = if (isPositive) Color(0xFF00C853) else Color(0xFFFF1744),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 24h Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("24h High", formatPrice(state.high24h))
            StatItem("24h Low", formatPrice(state.low24h))
            StatItem("Volume", "${String.format("%.0f", state.volume24h / 1000)}k BTC")
        }

        Spacer(Modifier.height(24.dp))

        Text("Recent Trades", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFF16213E))
        ) {
            items(state.recentTrades.take(15)) { trade ->
                val color = if (trade.isBuyerMaker) Color(0xFFFF1744) else Color(0xFF00C853)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (trade.isBuyerMaker) "SELL" else "BUY", color = color, fontWeight = FontWeight.Bold)
                    Text(formatPrice(trade.price), color = Color.White)
                    Text(String.format("%.4f", trade.quantity), color = Color.Gray)
                }
                HorizontalDivider(color = Color(0xFF2A2A4A))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price)
}