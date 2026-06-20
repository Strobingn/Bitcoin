# Bitcoin Tracker - Android App

Real-time Bitcoin price tracking using WebSocket connections.

## Features
- Live BTC/USD price updates via Binance WebSocket API
- 24h price change tracking
- Price history visualization
- Recent trades feed
- Dark theme with Bitcoin orange accents
- Auto-reconnect on connection loss

## Architecture
- **UI Layer**: Jetpack Compose with Material Design 3
- **Data Layer**: WebSocket client for real-time data
- **State Management**: ViewModel with StateFlow

## Tech Stack
- Kotlin
- Jetpack Compose
- Coroutines & Flow
- Java-WebSocket library
- Binance WebSocket API

## Setup
1. Open in Android Studio
2. Sync Gradle files
3. Run on device or emulator

## WebSocket Endpoints
- Ticker: `wss://stream.binance.com:9443/ws/btcusdt@ticker`
- Trades: `wss://stream.binance.com:9443/ws/btcusdt@trade`

## License
MIT