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

@Composable
fun OrderbookScreen(viewModel: BitcoinViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp)
    ) {
        Text("Live Order Book (Top 10)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            // Bids (green)
            Column(Modifier.weight(1f)) {
                Text("BIDS", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn {
                    items(state.bids) { bid ->
                        OrderRow(price = bid.price, qty = bid.quantity, isBid = true)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            // Asks (red)
            Column(Modifier.weight(1f)) {
                Text("ASKS", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn {
                    items(state.asks) { ask ->
                        OrderRow(price = ask.price, qty = ask.quantity, isBid = false)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text(
            "Depth data from Binance @100ms",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun OrderRow(price: Double, qty: Double, isBid: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF16213E)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$${"%.2f".format(price)}",
            color = if (isBid) Color(0xFF00C853) else Color(0xFFFF1744),
            fontWeight = FontWeight.Medium
        )
        Text(
            String.format("%.4f", qty),
            color = Color.White
        )
    }
}