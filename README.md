# BitcoinTracker Pro v2.0

Production-grade real-time Bitcoin tracker for Android.

## What's New & Best Features
- Clean Architecture + Repository pattern
- OkHttp WebSockets (ticker + trades + orderbook depth20)
- Live RSI(14), SMA(20), EMA(9) calculated on-device
- Price alerts with local notifications (trigger when price crosses target)
- Orderbook visualization (bids/asks)
- Simple portfolio PnL tracker
- Modern Jetpack Compose + Material 3 + Navigation
- Auto-reconnect + connection status
- Dark theme with Bitcoin orange accents
- Persisted alerts & portfolio (DataStore ready)
- Ready for Kalshi / on-chain / auto-trading bot integration

## Build & Run
1. Open in Android Studio Hedgehog+ or newer
2. Sync Gradle
3. Run on device/emulator (API 26+)

Or build from terminal:
```bash
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`

## GitHub Actions CI (Actual Builds)
The included `.github/workflows/android-build.yml` will:
- Build debug APK on every push/PR
- Build release APK on tag or manual dispatch
- Upload APKs as downloadable artifacts

Just push this folder to a new GitHub repo and the workflow runs automatically. No local Android SDK needed for CI builds.

## Future Roadmap (your trading bot)
- Kalshi BTC prediction market integration (positions + limit orders)
- Glassnode / CryptoQuant whale alerts (API key in settings)
- TA strategy signals (RSI divergence, MA crossover) + push to your Python bot
- Backtest results viewer
- One-tap "Execute on Kalshi" from signal

## Tech
Kotlin 2.0 • Compose BOM 2024.10 • OkHttp WS • DataStore • Material 3 • Navigation Compose

Built with the android-app-builder skill for maximum quality and real CI.

MIT License