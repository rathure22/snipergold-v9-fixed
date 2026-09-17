package com.snipergold.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snipergold.app.SignalEngine
import com.snipergold.app.SignalType
import com.snipergold.app.TradeSignal
import com.snipergold.app.Sensitivity as EngineSensitivity
import com.snipergold.app.data.Candle
import com.snipergold.app.data.JournalEntry
import com.snipergold.app.data.MarketDataEngine
import com.snipergold.app.data.Patterns
import com.snipergold.app.data.SessionInfo
import com.snipergold.app.data.SignalLog
import com.snipergold.app.data.SignalState
import com.snipergold.app.data.TpLevel
import com.snipergold.app.data.ForexRate
import com.snipergold.app.data.ConnLog
import com.snipergold.app.ui.theme.AccentTheme
import com.snipergold.app.utils.PatternDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

sealed class SmartTpResult {
    data class Valid(val price: Double, val buffer: Double, val distance: Double, val reason: String) : SmartTpResult()
    data class Skip(val reason: String) : SmartTpResult()
}

data class GoldTickSync(val price: Double, val timestampMs: Long, val source: String)

class MainViewModel : ViewModel() {

    private val engine = MarketDataEngine()
    private val signalEngine = SignalEngine()
    private var tickJob: Job? = null

    var tradeSignal by mutableStateOf<TradeSignal?>(null)
        private set
    var currentPrice by mutableDoubleStateOf(4294.70)
        private set
    var source by mutableStateOf("gold-api.com")
        private set
    var isLive by mutableStateOf(false)
        private set
    var errorMsg by mutableStateOf<String?>(null)
        private set

    val candles = mutableStateListOf<Candle>()
    val prices = mutableStateListOf<Double>()

    var guideHigh by mutableDoubleStateOf(4302.0)
        private set
    var guideLow by mutableDoubleStateOf(4286.0)
        private set

    var patterns by mutableStateOf(
        Patterns(
            doubleBottom = com.snipergold.app.data.PatternResult(false, 0, "--"),
            doubleTop = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bullFlag = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bearFlag = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bullishBat = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bearishBat = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bullish50 = com.snipergold.app.data.PatternResult(false, 0, "--"),
            bearish50 = com.snipergold.app.data.PatternResult(false, 0, "--")
        )
    )
        private set

    var signalState by mutableStateOf(SignalState())
        private set
    var sessions by mutableStateOf(listOf(SessionInfo("LONDON", "--:--", false), SessionInfo("NEW YORK", "--:--", false), SessionInfo("ASIA", "--:--", false)))
        private set
    var activeSession by mutableStateOf("ALL CLOSED")
        private set
    var tpLevels by mutableStateOf<List<TpLevel>>(emptyList())
        private set
    var signalHistory = mutableStateListOf<SignalLog>()
    var journal = mutableStateListOf<JournalEntry>()
    var ticksPerMin by mutableIntStateOf(0)
        private set
    private val tickTimestamps = mutableListOf<Long>()
    var nextCandleSec by mutableIntStateOf(60)
        private set
    var rsi by mutableDoubleStateOf(50.0)
        private set
    var momentum = mutableStateListOf<Float>()
    var dayHigh by mutableDoubleStateOf(0.0)
        private set
    var dayLow by mutableDoubleStateOf(0.0)
        private set
    var selectedTf by mutableStateOf("1m")
        private set
    var directionLabel by mutableStateOf("NEUTRAL")
        private set
    var lastUpdateTime by mutableStateOf("--:--:--")
        private set
    var forexRates by mutableStateOf(listOf(ForexRate("USD→PHP", 62.91), ForexRate("EUR→PHP", 72.60), ForexRate("KWD→PHP", 204.24)))
        private set
    var forexTime by mutableStateOf("--:--:--")
        private set
    var connLogs = mutableStateListOf<ConnLog>()
    var accentTheme by mutableStateOf(AccentTheme.GREEN)
    private var entryCounter = 0
    var dualMode by mutableStateOf("BOTH")
        private set
    var sensitivity by mutableStateOf("normal")
        private set
    var tickMs by mutableIntStateOf(1000)
        private set
    var floatingCollapsed by mutableStateOf(false)
    var stratAScore by mutableIntStateOf(0)
        private set
    var stratBScore by mutableIntStateOf(0)
        private set
    var journalWinsA by mutableIntStateOf(0)
        private set
    var journalLossA by mutableIntStateOf(0)
        private set
    var journalWinsB by mutableIntStateOf(0)
        private set
    var journalLossB by mutableIntStateOf(0)
        private set
    var ohlcOpen by mutableDoubleStateOf(0.0)
        private set
    var ohlcHigh by mutableDoubleStateOf(0.0)
        private set
    var ohlcLow by mutableDoubleStateOf(0.0)
        private set
    var ohlcClose by mutableDoubleStateOf(0.0)
        private set

    // --- Smart TP + Sync + Advance ---
    var smartTpResult by mutableStateOf<SmartTpResult?>(null)
        private set
    var syncedPrice by mutableDoubleStateOf(0.0)
        private set
    var velocityPerSec by mutableDoubleStateOf(0.0)
        private set
    var atr by mutableDoubleStateOf(5.2)
        private set
    var lastSiftingTick by mutableStateOf<GoldTickSync?>(null)
        private set
    var lastGoldApiTick by mutableStateOf<GoldTickSync?>(null)
        private set
    var bufferMult by mutableDoubleStateOf(0.6)
        private set
    var delaySec by mutableDoubleStateOf(3.0)
        private set
    var advanceSec by mutableDoubleStateOf(5.0)
        private set
    var predictedPrice by mutableDoubleStateOf(0.0)
        private set
    var predictedPrice5s by mutableDoubleStateOf(0.0)
        private set
    var predictedPrice10s by mutableDoubleStateOf(0.0)
        private set
    var futureResistance by mutableDoubleStateOf(0.0)
        private set
    var futureResistance10s by mutableDoubleStateOf(0.0)
        private set
    var arrowDirection by mutableStateOf("NEUTRAL")
        private set
    var standbyTpPreview by mutableStateOf<List<TpLevel>>(emptyList())
        private set
    var pendingWaitSignal by mutableStateOf<SignalLog?>(null)
        private set
    var waitCount by mutableIntStateOf(0)
        private set
    var nextSessionEntry by mutableStateOf<Double?>(null)
        private set

    // --- Cooldown & Forex Live ---
    private var lastSignalTime = 0L
    private var lastSignalType = ""
    var signalCooldownSec by mutableIntStateOf(30)
        private set
    private var forexJob: Job? = null
    var isForexLive by mutableStateOf(false)
        private set

    // API Key Wiring
    var siftingKeyInput by mutableStateOf("")
        private set
    var goldApiKeyInput by mutableStateOf("")
        private set
    var isSiftingKeyValid by mutableStateOf(false)
        private set
    var isApiKeySaved by mutableStateOf(false)
        private set
    var showApiKeyError by mutableStateOf(false)
        private set

    init {
        siftingKeyInput = engine.siftingKey ?: ""
        goldApiKeyInput = engine.goldApiKey ?: ""
        isSiftingKeyValid = siftingKeyInput.startsWith("sft_")
        isApiKeySaved = isSiftingKeyValid
        startLiveFeed()
        startForexLiveFeed()
    }

    fun selectTimeframe(tf: String) { selectedTf = tf }
    fun toggleTheme() { accentTheme = if (accentTheme == AccentTheme.GREEN) AccentTheme.ORANGE else AccentTheme.GREEN }
    fun updateDualMode(m: String) { dualMode = m }
    fun updateSensitivity(s: String) { sensitivity = s }
    fun updateTickMs(ms: Int) { tickMs = ms }
    fun toggleFloating() { floatingCollapsed = !floatingCollapsed }
    fun updateBuffer(mult: Double) { bufferMult = mult }
    fun updateDelaySec(sec: Double) { delaySec = sec }
    fun updateAdvanceSec(sec: Double) { advanceSec = sec.coerceIn(0.0, 20.0) }

    fun onSiftingKeyChange(newKey: String) {
        siftingKeyInput = newKey.trim()
        isSiftingKeyValid = siftingKeyInput.startsWith("sft_") && siftingKeyInput.length > 10
        showApiKeyError = false
        isApiKeySaved = false
    }
    fun onGoldApiKeyChange(newKey: String) { goldApiKeyInput = newKey.trim(); isApiKeySaved = false }
    fun saveApiKeys() {
        if (!isSiftingKeyValid && siftingKeyInput.isNotEmpty()) { showApiKeyError = true; return }
        engine.siftingKey = siftingKeyInput
        engine.goldApiKey = goldApiKeyInput
        isApiKeySaved = true
        showApiKeyError = false
        logConn("API Keys saved · sifting=${if(siftingKeyInput.isNotEmpty()) "OK" else "empty"}", true)
        startLiveFeed()
    }
    fun clearApiKeys() { siftingKeyInput=""; goldApiKeyInput=""; engine.siftingKey=""; engine.goldApiKey=""; isSiftingKeyValid=false; isApiKeySaved=false }
    fun dismissApiError() { showApiKeyError = false }
    fun resumeScan() {
        signalState = SignalState("WAIT", false, "", currentPrice, guideLow, 0.0, guideHigh, guideLow)
        directionLabel = "NEUTRAL"
        smartTpResult = null
    }

    private fun logConn(msg: String, ok: Boolean) {
        val t = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        connLogs.add(0, ConnLog(t, msg, ok))
        if (connLogs.size > 60) connLogs.removeAt(connLogs.lastIndex)
    }
    fun refreshQuote() {
        viewModelScope.launch {
            try {
                val p = engine.fetchGoldPrice()
                onNewPrice(p, engine.source)
                source = engine.source; isLive = true; errorMsg = null
                logConn("Quote OK · $source", true)
            } catch (e: Exception) { errorMsg = e.message; logConn("Quote fail · ${e.message}", false) }
            refreshForex()
        }
    }
    fun refreshForex() {
        viewModelScope.launch {
            try {
                forexRates = engine.fetchForexRates()
                forexTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                isForexLive = true
                logConn("Forex OK", true)
            } catch (e: Exception) { 
                isForexLive = false
                logConn("Forex fallback", false) 
            }
        }
    }

    fun startForexLiveFeed() {
        forexJob?.cancel()
        forexJob = viewModelScope.launch {
            while (isActive) {
                try {
                    forexRates = engine.fetchForexRates()
                    forexTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                    isForexLive = true
                } catch (e: Exception) {
                    isForexLive = false
                    logConn("Forex live fail · ${e.message}", false)
                }
                delay(5_000) // every 5 seconds
            }
        }
    }

    fun stopForexLiveFeed() {
        forexJob?.cancel()
        forexJob = null
        isForexLive = false
    }

    private var lastLoggedSource = ""
    private var lastLoggedLive: Boolean? = null
    fun startLiveFeed() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val price = engine.fetchGoldPrice()
                    onNewPrice(price, engine.source)
                    source = engine.source; isLive = true; errorMsg = null
                    if (source != lastLoggedSource || lastLoggedLive == false) {
                        logConn("Live tick OK · $source", true); lastLoggedSource = source
                    }
                    lastLoggedLive = true
                } catch (e: Exception) {
                    errorMsg = e.message
                    if (lastLoggedLive != false) logConn("Live tick fail · ${e.message}", false)
                    lastLoggedLive = false; isLive = false
                    onNewPrice(currentPrice, source)
                }
                updateWorldClock(); updateNextCandle()
                delay(tickMs.toLong().coerceIn(200L, 5000L))
            }
        }
    }

    private fun onNewPrice(price: Double, src: String) {
        currentPrice = price
        prices.add(price)
        if (prices.size > 100) prices.removeAt(0)
        val now = System.currentTimeMillis()
        val tick = GoldTickSync(price, now, src)
        if (src.contains("sifting", ignoreCase = true)) lastSiftingTick = tick else lastGoldApiTick = tick
        if (lastSiftingTick != null && lastGoldApiTick != null) {
            syncedPrice = syncTwoFeeds(lastSiftingTick!!, lastGoldApiTick!!)
            velocityPerSec = calculateVelocity(lastSiftingTick!!, lastGoldApiTick!!)
        } else {
            syncedPrice = price
            if (prices.size >= 2) velocityPerSec = prices.takeLast(2).let { it[1] - it[0] }
        }
        // Advance predict - base sa arrow
        predictedPrice = syncedPrice + (velocityPerSec * advanceSec)
        predictedPrice5s = syncedPrice + (velocityPerSec * 5.0)
        predictedPrice10s = syncedPrice + (velocityPerSec * 10.0)
        futureResistance = guideHigh + (velocityPerSec * advanceSec * 0.3)
        futureResistance10s = guideHigh + (velocityPerSec * 10.0 * 0.3)
        arrowDirection = when {
            velocityPerSec > 0.01 -> "BULLISH"
            velocityPerSec < -0.01 -> "BEARISH"
            else -> directionLabel
        }
        pushCandle(price, now)
        tickTimestamps.add(now)
        tickTimestamps.removeAll { now - it > 60_000 }
        ticksPerMin = tickTimestamps.size
        updateGuide(); updateAtrFromCandles()
        patterns = PatternDetector.detect(prices.toList())
        evaluateSignal(price)
        updateRsiAndMomentum(); updateDayRange()
        lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
    }

    private fun updateAtrFromCandles() {
        if (candles.size < 15) return
        val recent = candles.takeLast(14)
        var trSum = 0.0
        for (i in 1 until recent.size) {
            val curr = recent[i]; val prev = recent[i-1]
            val tr = maxOf(curr.high - curr.low, abs(curr.high - prev.close), abs(curr.low - prev.close))
            trSum += tr
        }
        atr = trSum / 14.0
        if (atr < 0.5) atr = 5.2
    }
    private fun syncTwoFeeds(fast: GoldTickSync, slow: GoldTickSync): Double {
        val timeDiffSec = (fast.timestampMs - slow.timestampMs) / 1000.0
        if (abs(timeDiffSec) < 0.1) return fast.price
        val vel = (fast.price - slow.price) / timeDiffSec.coerceAtLeast(0.1)
        val adjustedSlow = slow.price + (vel * timeDiffSec)
        return (fast.price * 0.7) + (adjustedSlow * 0.3)
    }
    private fun calculateVelocity(fast: GoldTickSync, slow: GoldTickSync): Double {
        val timeDiffSec = (fast.timestampMs - slow.timestampMs) / 1000.0
        return if (timeDiffSec != 0.0) (fast.price - slow.price) / timeDiffSec else 0.0
    }

    private fun calculateSmartTpAdjusted(entryPrice: Double, atrVal: Double, resistance: Double, isLong: Boolean, delay: Double): SmartTpResult {
        val baseBuffer = atrVal * bufferMult
        val delayBuffer = atrVal * 0.1 * delay
        val advanceBuffer = abs(velocityPerSec) * advanceSec * 0.5
        val totalBuffer = baseBuffer + delayBuffer + advanceBuffer
        val effectiveEntry = if (predictedPrice != 0.0) (entryPrice * 0.7) + (predictedPrice * 0.3) else entryPrice
        return if (isLong) {
            val adjustedResistance = resistance + (velocityPerSec * advanceSec * 0.2)
            val safeResistance = adjustedResistance - totalBuffer
            val distance = safeResistance - effectiveEntry
            if (distance <= 0) return SmartTpResult.Skip("SKIP: Naa naka sa resistance! Entry ${"%.2f".format(effectiveEntry)} >= SafeRes ${"%.2f".format(safeResistance)}")
            if (distance < atrVal * 1.5) return SmartTpResult.Skip("SKIP: Duol ra! Dist ${"%.2f".format(distance)} < ${"%.2f".format(atrVal * 1.5)} | Adv ${advanceSec}s")
            val atrTarget = effectiveEntry + (atrVal * 1.5)
            val percentTarget = effectiveEntry + (distance * 0.75)
            val finalTp = minOf(safeResistance, atrTarget, percentTarget)
            SmartTpResult.Valid(finalTp, totalBuffer, distance, "Adv${advanceSec}s SafeRes ${"%.2f".format(safeResistance)}")
        } else {
            val adjustedSupport = resistance + (velocityPerSec * advanceSec * 0.2)
            val safeSupport = adjustedSupport + totalBuffer
            val distance = effectiveEntry - safeSupport
            if (distance <= 0) return SmartTpResult.Skip("SKIP SHORT: Naa naka sa support!")
            val atrTarget = effectiveEntry - (atrVal * 1.5)
            val percentTarget = effectiveEntry - (distance * 0.75)
            val finalTp = maxOf(safeSupport, atrTarget, percentTarget)
            SmartTpResult.Valid(finalTp, totalBuffer, distance, "SHORT Adv${advanceSec}s")
        }
    }

    private fun updateRsiAndMomentum() {
        if (prices.size < 15) return
        val closes = prices.takeLast(15)
        var gains = 0.0; var losses = 0.0
        for (i in 1 until closes.size) { val d = closes[i] - closes[i - 1]; if (d >= 0) gains += d else losses -= d }
        val ag = gains / 14.0; val al = losses / 14.0
        rsi = if (al == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + ag / al))
        val recent = prices.takeLast(25)
        if (recent.size >= 2) {
            val deltas = mutableListOf<Float>()
            for (i in 1 until recent.size) deltas.add((recent[i] - recent[i - 1]).toFloat())
            val maxAbs = deltas.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(0.01f) ?: 1f
            momentum.clear(); momentum.addAll(deltas.map { (it / maxAbs).coerceIn(-1f, 1f) })
        }
    }
    private fun updateDayRange() {
        if (candles.isEmpty()) return
        dayHigh = candles.maxOf { it.high }; dayLow = candles.minOf { it.low }
        val last = candles.last(); ohlcOpen = last.open; ohlcHigh = last.high; ohlcLow = last.low; ohlcClose = last.close
        stratAScore = listOf(patterns.doubleBottom, patterns.bullFlag).count { it.found } * 9
        stratBScore = listOf(patterns.doubleTop, patterns.bearFlag).count { it.found } * 9
        if (stratAScore == 0) stratAScore = 18; if (stratBScore == 0) stratBScore = 18
    }
    private fun pushCandle(price: Double, ts: Long) {
        val bucket = (ts / 60_000) * 60_000; val last = candles.lastOrNull()
        if (last == null || last.time != bucket) {
            candles.add(Candle(bucket, price, price, price, price))
            if (candles.size > 180) candles.removeAt(0)
        } else {
            val idx = candles.lastIndex
            candles[idx] = last.copy(high = maxOf(last.high, price), low = minOf(last.low, price), close = price)
        }
    }
    private fun updateGuide() {
        if (prices.size < 18) return
        val last18 = prices.takeLast(18)
        guideHigh = last18.maxOrNull() ?: guideHigh
        guideLow = last18.minOrNull() ?: guideLow
    }

    // --- CORRECTED BUY / SELL / WAIT LOGIC WITH ADVANCE ---
    private fun evaluateSignal(price: Double) {
        val effectivePrice = syncedPrice.ifZero(price)
        val predictedEffPrice = if (predictedPrice != 0.0) (effectivePrice * 0.6) + (predictedPrice * 0.4) else effectivePrice

        // 1. SignalEngine gamit ang predicted price para sakto bisan naay delay
        val engineSensitivity = when (sensitivity) {
            "strict" -> EngineSensitivity.STRICT
            "loose" -> EngineSensitivity.LOOSE
            else -> EngineSensitivity.NORMAL
        }
        val generatedSignal = signalEngine.generateSignal(
            currentPrice = predictedEffPrice,
            supportPrice = guideLow,
            resistancePrice = guideHigh,
            rsi = rsi,
            sensitivity = engineSensitivity
        )
        tradeSignal = generatedSignal

        var signal = when (generatedSignal.type) {
            SignalType.BUY -> "BUY"
            SignalType.SELL -> "SELL"
            SignalType.WAIT -> "WAIT"
        }
        var bullish = generatedSignal.type == SignalType.BUY
        var entry = generatedSignal.entry
        var sl = generatedSignal.stopLoss
        var risk = generatedSignal.risk
        var reason = when (generatedSignal.type) {
            SignalType.BUY -> "ENGINE BUY: Near Support ${"%.2f".format(guideLow)} + RSI ${"%.1f".format(rsi)} + Arrow $arrowDirection"
            SignalType.SELL -> "ENGINE SELL: Near Resistance ${"%.2f".format(guideHigh)} + RSI ${"%.1f".format(rsi)} + Arrow $arrowDirection"
            SignalType.WAIT -> ""
        }

        // 2. Pattern fallback kung WAIT pa gihapon - pero i-check ang advance logic
        if (signal == "WAIT") {
            val aboveHigh = predictedEffPrice > guideHigh
            val belowLow = predictedEffPrice < guideLow
            val bullishArrow = arrowDirection == "BULLISH" || directionLabel == "BULLISH"
            val bearishArrow = arrowDirection == "BEARISH" || directionLabel == "BEARISH"

            if ((patterns.doubleBottom.found || patterns.bullFlag.found) && aboveHigh && bullishArrow) {
                signal = "BUY"; bullish = true
                reason = "PATTERN BUY: ${if (patterns.doubleBottom.found) "W Bottom" else "Bull Flag"} + LUSOT HIGH ${"%.2f".format(guideHigh)} + Arrow BULLISH"
                entry = predictedEffPrice; sl = guideLow; risk = entry - sl
            } else if ((patterns.doubleTop.found || patterns.bearFlag.found) && belowLow && bearishArrow) {
                signal = "SELL"; bullish = false
                reason = "PATTERN SELL: ${if (patterns.doubleTop.found) "M Top" else "Bear Flag"} + LUSOT LOW ${"%.2f".format(guideLow)} + Arrow BEARISH"
                entry = predictedEffPrice; sl = guideHigh; risk = sl - entry
            } else if (patterns.bullishBat.found && bullishArrow) {
                signal = "BUY"; bullish = true
                reason = "BAT Bullish @ ${"%.2f".format(predictedEffPrice)} + Arrow BULLISH"
                entry = predictedEffPrice; sl = guideLow; risk = maxOf(0.30, entry - sl)
            } else if (patterns.bearishBat.found && bearishArrow) {
                signal = "SELL"; bullish = false
                reason = "BAT Bearish @ ${"%.2f".format(predictedEffPrice)} + Arrow BEARISH"
                entry = predictedEffPrice; sl = guideHigh; risk = maxOf(0.30, sl - entry)
            } else if (patterns.bullish50.found && bullishArrow) {
                signal = "BUY"; bullish = true
                reason = "5-0 Bullish @ ${"%.2f".format(predictedEffPrice)}"
                entry = predictedEffPrice; sl = guideLow; risk = maxOf(0.30, entry - sl)
            } else if (patterns.bearish50.found && bearishArrow) {
                signal = "SELL"; bullish = false
                reason = "5-0 Bearish @ ${"%.2f".format(predictedEffPrice)}"
                entry = predictedEffPrice; sl = guideHigh; risk = maxOf(0.30, sl - entry)
            }
        }

        // 3. WAIT validation - kung duol na kaayo sa resistance/support bisan naay advance, WAIT gihapon
        if (signal == "BUY" || signal == "SELL") {
            val smartCheck = calculateSmartTpAdjusted(
                entryPrice = effectivePrice,
                atrVal = atr,
                resistance = if (bullish) guideHigh else guideLow,
                isLong = bullish,
                delay = delaySec
            )
            // Kung SKIP, himoon natong WAIT para dili ma-fake BUY/SELL
            if (smartCheck is SmartTpResult.Skip) {
                // Pero kung kusog kaayo ang arrow ug layo pa ang future resistance, tugotan ang BUY/SELL
                val futureOk = if (bullish) {
                    futureResistance - effectivePrice > atr * 0.8
                } else {
                    effectivePrice - (guideLow + atr * 0.8) > atr * 0.8
                }
                if (!futureOk) {
                    signal = "WAIT"
                    bullish = false
                    reason = "WAIT: ${smartCheck.reason} + FutureRes Check Fail"
                }
            }
        }

        // ========== 30 SECOND COOLDOWN (before setting state) ==========
        val nowMs = System.currentTimeMillis()
        val timeSinceLast = (nowMs - lastSignalTime) / 1000.0
        if (signal != "WAIT" && signal == lastSignalType && timeSinceLast < signalCooldownSec) {
            signal = "WAIT"
            bullish = false
            reason = "COOLDOWN: Wait ${"%.0f".format(signalCooldownSec - timeSinceLast)}s more"
        }
        // ==============================================================

        signalState = SignalState(signal, bullish, reason, entry, sl, risk, guideHigh, guideLow)
        directionLabel = when (signal) {
            "BUY" -> "BULLISH"
            "SELL" -> "BEARISH"
            else -> arrowDirection
        }

        // 4. TP LEVELS - Sakto sa BUY/SELL/WAIT
        val currentResistance = if (bullish) guideHigh else guideLow
        val smartFiltered = calculateSmartTpAdjusted(
            entryPrice = effectivePrice,
            atrVal = atr,
            resistance = currentResistance,
            isLong = bullish,
            delay = delaySec
        )
        smartTpResult = smartFiltered

        if (signal == "BUY" || signal == "SELL") {
            val baseTps = if (generatedSignal.type != SignalType.WAIT) {
                listOf(generatedSignal.tp1, generatedSignal.tp2, generatedSignal.tp3, generatedSignal.tp4, generatedSignal.tp5)
            } else {
                listOf(entry + risk, entry + risk*2, entry + risk*3, entry + risk*4, entry + risk*5).let {
                    if (!bullish) it.map { v -> entry - (v - entry) } else it
                }
            }
            tpLevels = applySmartTpAsPrimary(baseTps, smartFiltered, bullish)
        } else {
            tpLevels = emptyList()
            // smartTpResult naa gihapon para makita ngano WAIT
        }

        if (signal == "BUY" || signal == "SELL") {
            val last = signalHistory.firstOrNull()
            val samePrice = last != null && last.signal == signal && abs(last.price - price) < 0.15

            if (!samePrice) {
                lastSignalTime = System.currentTimeMillis()
                lastSignalType = signal

                val updated = signalHistory.map { it.copy(isNew = false) }
                signalHistory.clear()
                signalHistory.addAll(updated)
                entryCounter++
                val tps = tpLevels
                val t = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()) + " PHT"
                signalHistory.add(0, SignalLog(
                    signal = signal, time = t, price = price, reason = reason, isNew = true, lot = 0.02,
                    entryN = entryCounter, entry = entry, sl = sl,
                    tp1 = tps.getOrNull(0)?.price ?: entry + risk,
                    tp2 = tps.getOrNull(1)?.price ?: entry + risk * 2,
                    tp3 = tps.getOrNull(2)?.price ?: entry + risk * 3,
                    pnlHint = if (bullish) "+0.05" else "-0.05"
                ))
                if (signalHistory.size > 20) signalHistory.removeAt(signalHistory.lastIndex)
            }
        }
    }

    private fun Double.ifZero(fallback: Double): Double = if (this == 0.0) fallback else this

    private fun applySmartTpAsPrimary(baseTps: List<Double>, smartResult: SmartTpResult, bullish: Boolean): List<TpLevel> {
        val baseEntry = syncedPrice.ifZero(currentPrice)
        val entry = if (predictedPrice != 0.0) (baseEntry * 0.5) + (predictedPrice * 0.3) + (predictedPrice5s * 0.2) else baseEntry
        val resistance = guideHigh; val support = guideLow
        return when (smartResult) {
            is SmartTpResult.Skip -> {
                val safeDist = if (bullish) (resistance - atr * 0.8 - abs(velocityPerSec)*advanceSec) - entry else entry - (support + atr * 0.8 + abs(velocityPerSec)*advanceSec)
                if (safeDist <= 0) emptyList() else {
                    val adv = abs(velocityPerSec) * advanceSec * 0.3
                    listOf(entry + if (bullish) safeDist * 0.3 + adv else -safeDist * 0.3 - adv,
                        entry + if (bullish) safeDist * 0.5 + adv else -safeDist * 0.5 - adv,
                        entry + if (bullish) safeDist * 0.7 + adv else -safeDist * 0.7 - adv,
                        entry + if (bullish) safeDist * 0.85 + adv else -safeDist * 0.85 - adv
                    ).mapIndexed { idx, price -> TpLevel(idx + 1, price, "${idx+1}:1", listOf("50%","30%","15%","5%")[idx], false) }
                }
            }
            is SmartTpResult.Valid -> {
                val smartPrice = smartResult.price; val safeDistance = smartResult.distance
                val wickProtection = atr * 0.25
                val advanceComp5s = velocityPerSec * 5.0 * 0.4
                val advanceComp10s = velocityPerSec * 10.0 * 0.4
                val advanceComp20s = velocityPerSec * 20.0 * 0.4
                val tp1Price = entry + if (bullish) safeDistance * 0.45 + advanceComp5s else -safeDistance * 0.45 + advanceComp5s
                val tp2Price = entry + if (bullish) safeDistance * 0.65 + advanceComp5s else -safeDistance * 0.65 + advanceComp5s
                val tp3Price = smartPrice - if (bullish) wickProtection else -wickProtection + advanceComp5s * 0.5
                val tp4Base = if (bullish) minOf(predictedPrice10s, futureResistance10s - wickProtection) else maxOf(predictedPrice10s, futureResistance10s + wickProtection)
                val tp4Price = if (tp4Base == 0.0) smartPrice + advanceComp10s else tp4Base
                val predicted20s = syncedPrice + (velocityPerSec * 20.0)
                val futureRes20s = guideHigh + (velocityPerSec * 20.0 * 0.3)
                val tp5Base = if (bullish) minOf(predicted20s, futureRes20s - wickProtection * 0.5) else maxOf(predicted20s, futureRes20s + wickProtection * 0.5)
                val tp5Price = if (tp5Base == 0.0) smartPrice + advanceComp20s else tp5Base
                val finalPrices = listOf(tp1Price, tp2Price, tp3Price, tp4Price, tp5Price).map { calculatedTp ->
                    if (bullish) minOf(calculatedTp, smartPrice + abs(advanceComp20s) + atr * 0.5) else maxOf(calculatedTp, smartPrice - abs(advanceComp20s) - atr * 0.5)
                }
                listOf(
                    TpLevel(1, finalPrices[0], "5s", "50%", false),
                    TpLevel(2, finalPrices[1], "5s", "30%", false),
                    TpLevel(3, finalPrices[2], "10s Adv", "15%", false),
                    TpLevel(4, finalPrices[3], "10s Auto $arrowDirection", "4% ${if(arrowDirection=="BULLISH") "↑" else "↓"}", true),
                    TpLevel(5, finalPrices[4], "20s Auto", "1% runner", true)
                )
            }
        }
    }

    private fun buildTpLevels(bullish: Boolean, entry: Double, risk: Double, smartResult: SmartTpResult? = null) {
        val baseLevels = listOf(
            if (bullish) entry + risk else entry - risk,
            if (bullish) entry + risk * 2 else entry - risk * 2,
            if (bullish) entry + risk * 3 else entry - risk * 3,
            if (bullish) entry + risk * 4 else entry - risk * 4,
            if (bullish) entry + risk * 5 else entry - risk * 5
        )
        tpLevels = if (smartResult != null) applySmartTpAsPrimary(baseLevels, smartResult, bullish) else {
            baseLevels.mapIndexed { idx, p -> TpLevel(idx+1, p, "${idx+1}:1", listOf("50%","30%","10%","5%","runner")[idx], idx==4) }
        }
    }

    private fun updateWorldClock() {
        val now = Calendar.getInstance()
        fun session(name: String, tzId: String, openH: Int, closeH: Int): SessionInfo {
            val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)); cal.timeInMillis = now.timeInMillis
            val h = cal.get(Calendar.HOUR_OF_DAY); val open = h in openH until closeH
            val time = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getTimeZone(tzId) }.format(now.time)
            return SessionInfo(name, time, open)
        }
        val london = session("LONDON", "Europe/London", 8, 17)
        val ny = session("NEW YORK", "America/New_York", 8, 17)
        val asia = session("ASIA", "Asia/Tokyo", 9, 18)
        sessions = listOf(london, ny, asia)
        val active = listOf(london, ny, asia).filter { it.isOpen }.map { it.name }
        activeSession = if (active.isEmpty()) "ALL CLOSED" else active.joinToString(" + ")
    }
    private fun updateNextCandle() {
        val now = System.currentTimeMillis(); val next = ((now / 60_000) + 1) * 60_000
        nextCandleSec = ((next - now) / 1000).toInt().coerceAtLeast(0)
    }
    fun setApiKeys(sifting: String, goldApi: String) {
        siftingKeyInput = sifting.trim(); goldApiKeyInput = goldApi.trim()
        engine.siftingKey = siftingKeyInput; engine.goldApiKey = goldApiKeyInput
        isSiftingKeyValid = siftingKeyInput.startsWith("sft_")
        isApiKeySaved = isSiftingKeyValid || goldApiKeyInput.isNotEmpty()
    }
    fun loadHistory() {
        viewModelScope.launch {
            try {
                val hist = engine.loadHistory()
                candles.clear(); candles.addAll(hist)
                prices.clear(); prices.addAll(hist.map { it.close })
                source = engine.source; updateGuide()
                patterns = PatternDetector.detect(prices.toList()); updateAtrFromCandles()
            } catch (e: Exception) { errorMsg = e.message }
        }
    }
    fun addJournal(strategy: String, result: String, entry: String, exit: String) {
        val t = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        journal.add(0, JournalEntry(strategy, result, entry, exit, t))
        val win = result.equals("Win", true) || result.equals("W", true)
        val loss = result.equals("Loss", true) || result.equals("L", true)
        when {
            strategy.uppercase().contains("A") && win -> journalWinsA++
            strategy.uppercase().contains("A") && loss -> journalLossA++
            strategy.uppercase().contains("B") && win -> journalWinsB++
            strategy.uppercase().contains("B") && loss -> journalLossB++
            win -> journalWinsA++; loss -> journalLossA++
        }
    }
    fun clearJournal() { journal.clear() }
    fun clearSignalHistory() { signalHistory.clear(); pendingWaitSignal = null; nextSessionEntry = null; waitCount = 0 }
    fun clearPendingWait() { pendingWaitSignal = null; nextSessionEntry = null; waitCount = 0 }
    override fun onCleared() { tickJob?.cancel(); super.onCleared() }
}
