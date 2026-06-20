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
fun PortfolioScreen(viewModel: BitcoinViewModel) {
    val state by viewModel.uiState.collectAsState()
    var btcAmount by remember { mutableStateOf("0.05") }
    var entryPrice by remember { mutableStateOf(state.currentPrice.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Simple Portfolio Tracker", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Track your BTC position PnL (local only)", color = Color.Gray)

        OutlinedTextField(
            value = btcAmount,
            onValueChange = { btcAmount = it },
            label = { Text("BTC Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = entryPrice,
            onValueChange = { entryPrice = it },
            label = { Text("Entry Price USD") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { /* In full version save to DataStore via repo */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7931A))
        ) {
            Text("Update Position")
        }

        val amount = btcAmount.toDoubleOrNull() ?: 0.0
        val entry = entryPrice.toDoubleOrNull() ?: state.currentPrice
        val currentValue = amount * state.currentPrice
        val costBasis = amount * entry
        val pnl = currentValue - costBasis
        val pnlPercent = if (costBasis > 0) (pnl / costBasis) * 100 else 0.0

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Current Position Value", color = Color.Gray)
                Text(formatPrice(currentValue), fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(12.dp))
                Text("Unrealized PnL", color = Color.Gray)
                Text(
                    text = "${if (pnl >= 0) "+" else ""}${formatPrice(pnl)} (${String.format("%.1f", pnlPercent)}%)",
                    fontSize = 22.sp,
                    color = if (pnl >= 0) Color(0xFF00C853) else Color(0xFFFF1744),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            "Future: Connect to Kalshi for real prediction market positions + auto-trade signals from your bot.",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}