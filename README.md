# SniperGold v9 — Android (Jetpack Compose)

Fully functional mobile port of the **SniperGold PATTERN+GUIDE** web app.

## Stack
- **Gradle** `8.11.1`
- **Kotlin** 2.0.21
- **Jetpack Compose** (BOM 2024.10.01)
- **Canvas** custom candlestick chart
- **OkHttp** live XAU/USD quotes
- **minSdk 26** / **targetSdk 35** / **compileSdk 35**

## Features (ported from HTML)
| Feature | Status |
|---------|--------|
| Live gold price (1s) | ✅ gold-api.com + optional SiftingIO / GoldAPI.io |
| 1-min OHLC candles built from ticks | ✅ |
| Canvas candlestick chart + guide lines | ✅ |
| UP/DOWN trend arrows | ✅ |
| Pattern detection (W, M, Bull/Bear Flag) | ✅ |
| Guide High / Low breakout (“lusot”) | ✅ |
| BUY / SELL signal logic | ✅ |
| TP1–TP5 + SL | ✅ |
| World trading clock (London / NY / Asia) | ✅ |
| Market Data Engine + API keys | ✅ |
| Signal history | ✅ |
| Trade journal | ✅ |
| Tick intensity | ✅ |
| Floating status bar | ✅ |

## Open in Android Studio
1. Unzip this project.
2. Open the folder in **Android Studio** (Hedgehog or newer recommended).
3. Let Gradle sync (uses Gradle 8.11.1 wrapper).
4. Run on emulator or device.

## Build from CLI
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

> First open may download the Gradle wrapper jar automatically if `gradlew` is not yet executable.  
> If `gradlew` is missing, Android Studio will generate it on sync.

## API notes
- **No key required** → public `https://api.gold-api.com/price/XAU` (fallback)
- Optional: paste **SiftingIO** / **GoldAPI.io** keys in the Market Data Engine panel for better feed + historical OHLCV.

## Developer
**Ji NG · SLOW-STONE™ v6**  
Pattern + Guide Lusot Logic · 1-sec realtime world market
