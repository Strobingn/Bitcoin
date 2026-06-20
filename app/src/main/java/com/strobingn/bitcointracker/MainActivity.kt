package com.strobingn.bitcointracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strobingn.bitcointracker.ui.theme.BitcoinTrackerProTheme
import com.strobingn.bitcointracker.viewmodel.BitcoinViewModel
import com.strobingn.bitcointracker.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitcoinTrackerProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BitcoinTrackerApp()
                }
            }
        }
    }
}

@Composable
fun BitcoinTrackerApp(viewModel: BitcoinViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Price", "Orderbook", "Indicators", "Alerts", "Portfolio")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    "BTC Sentinel", 
                    color = Color(0xFFF7931A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ) 
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0D0D1A)
            ),
            actions = {
                val state by viewModel.uiState.collectAsState()
                ConnectionBadge(isConnected = state.isConnected)
            }
        )

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> LivePriceScreen(viewModel)
                1 -> OrderbookScreen(viewModel)
                2 -> IndicatorsScreen(viewModel)
                3 -> AlertsScreen(viewModel)
                4 -> PortfolioScreen(viewModel)
            }
        }

        // Bottom Navigation
        NavigationBar(
            containerColor = Color(0xFF1A1A2E),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.ShowChart
                                1 -> Icons.Default.TableChart
                                2 -> Icons.Default.Analytics
                                3 -> Icons.Default.Notifications
                                else -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = title
                        )
                    },
                    label = { Text(title, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF7931A),
                        selectedTextColor = Color(0xFFF7931A),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun ConnectionBadge(isConnected: Boolean) {
    Row(
        modifier = Modifier
            .background(
                color = if (isConnected) Color(0xFF00C853).copy(alpha = 0.2f) 
                       else Color(0xFFFF1744).copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isConnected) Color(0xFF00C853) else Color(0xFFFF1744),
                    shape = MaterialTheme.shapes.small
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (isConnected) "LIVE" else "OFFLINE",
            color = if (isConnected) Color(0xFF00C853) else Color(0xFFFF1744),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}