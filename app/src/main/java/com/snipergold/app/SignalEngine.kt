package com.snipergold.app

enum class SignalType { BUY, SELL, WAIT }

enum class Sensitivity { STRICT, NORMAL, LOOSE }

data class TradeSignal(
    val type: SignalType,
    val entry: Double,
    val stopLoss: Double,
    val risk: Double,
    val tp1: Double,
    val tp2: Double,
    val tp3: Double,
    val tp4: Double,
    val tp5: Double
)

class SignalEngine {

    fun generateSignal(
        currentPrice: Double,
        supportPrice: Double,
        resistancePrice: Double,
        rsi: Double,
        sensitivity: Sensitivity = Sensitivity.NORMAL,
        slBuffer: Double = 0.50
    ): TradeSignal {

        val distanceToSupport = kotlin.math.abs(currentPrice - supportPrice)
        val distanceToResistance = kotlin.math.abs(currentPrice - resistancePrice)
        
        // Paspas o higpit nga distansya depende sa Sensitivity
        val maxDistance = when (sensitivity) {
            Sensitivity.STRICT -> 1.00
            Sensitivity.NORMAL -> 1.50
            Sensitivity.LOOSE -> 3.00
        }

        val isNearSupport = distanceToSupport <= maxDistance
        val isNearResistance = distanceToResistance <= maxDistance

        // RSI limits depende sa Sensitivity
        val (buyRsiLimit, sellRsiLimit) = when (sensitivity) {
            Sensitivity.STRICT -> Pair(30.0, 70.0)
            Sensitivity.NORMAL -> Pair(45.0, 55.0)
            Sensitivity.LOOSE -> Pair(60.0, 40.0) // Mas luag ang pasok sa loose
        }

        // 1. LOOSE / INSTANT MODE (Kung Loose ang pinili)
        if (sensitivity == Sensitivity.LOOSE) {
            val isUptrend = rsi < 50.0
            val type = if (isUptrend) SignalType.BUY else SignalType.SELL
            val sl = if (type == SignalType.BUY) supportPrice - slBuffer else resistancePrice + slBuffer
            val risk = kotlin.math.abs(currentPrice - sl)
            
            if (risk > 0) {
                return TradeSignal(
                    type = type,
                    entry = currentPrice,
                    stopLoss = sl,
                    risk = risk,
                    tp1 = if (type == SignalType.BUY) currentPrice + (risk * 1) else currentPrice - (risk * 1),
                    tp2 = if (type == SignalType.BUY) currentPrice + (risk * 2) else currentPrice - (risk * 2),
                    tp3 = if (type == SignalType.BUY) currentPrice + (risk * 3) else currentPrice - (risk * 3),
                    tp4 = if (type == SignalType.BUY) currentPrice + (risk * 4) else currentPrice - (risk * 4),
                    tp5 = if (type == SignalType.BUY) currentPrice + (risk * 5) else currentPrice - (risk * 5)
                )
            }
        }

        // 2. BUY Logic: Near Support + RSI condition (Strict & Normal)
        if (isNearSupport && rsi <= buyRsiLimit) {
            val sl = supportPrice - slBuffer
            val risk = currentPrice - sl

            if (risk > 0) {
                return TradeSignal(
                    type = SignalType.BUY,
                    entry = currentPrice,
                    stopLoss = sl,
                    risk = risk,
                    tp1 = currentPrice + (risk * 1),
                    tp2 = currentPrice + (risk * 2),
                    tp3 = currentPrice + (risk * 3),
                    tp4 = currentPrice + (risk * 4),
                    tp5 = currentPrice + (risk * 5)
                )
            }
        }

        // 3. SELL Logic: Near Resistance + RSI condition (Strict & Normal)
        if (isNearResistance && rsi >= sellRsiLimit) {
            val sl = resistancePrice + slBuffer
            val risk = sl - currentPrice

            if (risk > 0) {
                return TradeSignal(
                    type = SignalType.SELL,
                    entry = currentPrice,
                    stopLoss = sl,
                    risk = risk,
                    tp1 = currentPrice - (risk * 1),
                    tp2 = currentPrice - (risk * 2),
                    tp3 = currentPrice - (risk * 3),
                    tp4 = currentPrice - (risk * 4),
                    tp5 = currentPrice - (risk * 5)
                )
            }
        }

        // 4. WAIT Logic (Kung walay nasabtan nga kondisyon)
        return TradeSignal(
            type = SignalType.WAIT,
            entry = currentPrice,
            stopLoss = 0.0,
            risk = 0.0,
            tp1 = 0.0, tp2 = 0.0, tp3 = 0.0, tp4 = 0.0, tp5 = 0.0
        )
    }

    fun calculateRsi(closes: List<Double>, period: Int = 14): Double {
        if (closes.size < period + 1) return 50.0
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) {
                gains.add(change)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(kotlin.math.abs(change))
            }
        }

        val avgGain = gains.takeLast(period).sum() / period
        val avgLoss = losses.takeLast(period).sum() / period

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }
}
