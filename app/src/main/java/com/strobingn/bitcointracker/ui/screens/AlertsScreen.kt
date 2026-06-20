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
import com.strobingn.bitcointracker.data.PriceAlert
import com.strobingn.bitcointracker.viewmodel.BitcoinViewModel

@Composable
fun AlertsScreen(viewModel: BitcoinViewModel) {
    val state by viewModel.uiState.collectAsState()
    var alertPrice by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp)
    ) {
        Text("Price Alerts", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Get notified when BTC crosses your target", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        // Add new alert
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = alertPrice,
                onValueChange = { alertPrice = it },
                label = { Text("Target Price USD") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    alertPrice.toDoubleOrNull()?.let {
                        viewModel.addAlert(it, isAbove)
                        alertPrice = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7931A))
            ) {
                Text("Add")
            }
        }

        Row {
            FilterChip(
                selected = isAbove,
                onClick = { isAbove = true },
                label = { Text("Above") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = !isAbove,
                onClick = { isAbove = false },
                label = { Text("Below") }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Active Alerts", fontSize = 16.sp, color = Color.White)

        // In real version load from repo state. For demo show empty or note
        LazyColumn {
            // Placeholder - in full version bind to activeAlerts from state
            item {
                Text(
                    "Alerts will trigger local notifications when price crosses target.\n\nDemo: Add alert then watch price action.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}