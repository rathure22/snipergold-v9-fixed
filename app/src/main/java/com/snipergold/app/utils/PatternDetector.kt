package com.snipergold.app.utils

import com.snipergold.app.data.PatternResult
import com.snipergold.app.data.Patterns
import kotlin.math.abs

object PatternDetector {

    fun detect(prices: List<Double>): Patterns {
        if (prices.size < 20) {
            return Patterns(
                doubleBottom = PatternResult(false, 0, "--"),
                doubleTop = PatternResult(false, 0, "--"),
                bullFlag = PatternResult(false, 0, "--"),
                bearFlag = PatternResult(false, 0, "--"),
                bullishBat = PatternResult(false, 0, "--"),
                bearishBat = PatternResult(false, 0, "--"),
                bullish50 = PatternResult(false, 0, "--"),
                bearish50 = PatternResult(false, 0, "--")
            )
        }
        val last = prices.takeLast(20)
        val lows = mutableListOf<Double>()
        val highs = mutableListOf<Double>()
        for (i in 2 until last.size - 2) {
            if (last[i] < last[i - 1] && last[i] < last[i - 2] &&
                last[i] < last[i + 1] && last[i] < last[i + 2]
            ) lows.add(last[i])
            if (last[i] > last[i - 1] && last[i] > last[i - 2] &&
                last[i] > last[i + 1] && last[i] > last[i + 2]
            ) highs.add(last[i])
        }

        val db = lows.size >= 2 && abs(lows.last() - lows[lows.size - 2]) < 1.5
        val dt = highs.size >= 2 && abs(highs.last() - highs[highs.size - 2]) < 1.5

        val firstHalf = last.take(10)
        val secondHalf = last.drop(10)
        val upMove = firstHalf.last() - firstHalf.first()
        val range = (secondHalf.maxOrNull() ?: 0.0) - (secondHalf.minOrNull() ?: 0.0)
        val bullFlag = upMove > 3 && range < 2.2
        val bearFlag = upMove < -3 && range < 2.2

        val (bullishBat, bearishBat) = detectBatPattern(prices)
        val (bullish50, bearish50) = detect50Pattern(prices)

        return Patterns(
            doubleBottom = PatternResult(
                db, if (db) 90 else lows.size * 20,
                if (db) "W @ ${"%.2f".format(lows.last())}" else "${lows.size} lows"
            ),
            doubleTop = PatternResult(
                dt, if (dt) 90 else highs.size * 20,
                if (dt) "M @ ${"%.2f".format(highs.last())}" else "${highs.size} highs"
            ),
            bullFlag = PatternResult(bullFlag, if (bullFlag) 75 else 20, if (bullFlag) "Bull flag" else "No flag"),
            bearFlag = PatternResult(bearFlag, if (bearFlag) 75 else 20, if (bearFlag) "Bear flag" else "No flag"),
            bullishBat = bullishBat,
            bearishBat = bearishBat,
            bullish50 = bullish50,
            bearish50 = bearish50
        )
    }

    /**
     * Zigzag swing-point extraction: collapses the raw price series into a sequence of
     * alternating swing highs and lows, filtering out moves smaller than [minMovePct].
     */
    private fun findSwingPoints(prices: List<Double>, minMovePct: Double = 0.15): List<Double> {
        if (prices.size < 5) return emptyList()
        val pivots = mutableListOf<Double>()
        var anchor = prices[0]
        var direction = 0 // 0 = undetermined, 1 = rising, -1 = falling
        for (i in 1 until prices.size) {
            val price = prices[i]
            val pctFromAnchor = if (anchor != 0.0) abs(price - anchor) / anchor * 100 else 0.0
            when (direction) {
                0 -> if (pctFromAnchor >= minMovePct) {
                    pivots.add(anchor)
                    direction = if (price > anchor) 1 else -1
                    anchor = price
                }
                1 -> if (price > anchor) {
                    anchor = price
                } else if (abs(anchor - price) / anchor * 100 >= minMovePct) {
                    pivots.add(anchor)
                    direction = -1
                    anchor = price
                }
                else -> if (price < anchor) {
                    anchor = price
                } else if (abs(price - anchor) / anchor * 100 >= minMovePct) {
                    pivots.add(anchor)
                    direction = 1
                    anchor = price
                }
            }
        }
        pivots.add(anchor)
        return pivots
    }

    /**
     * Harmonic Bat pattern (bullish + bearish), using the standard published ratio bands:
     *   AB retraces XA by 0.382-0.500
     *   BC retraces AB by 0.382-0.886
     *   D completes near 0.786-0.886 retracement of XA (the Bat's signature "deep" D point)
     * X-A-B-C are the last 4 confirmed swing points; D is the current (live) price, since
     * that is the point the pattern is still forming toward - this flags a potential
     * reversal zone rather than a confirmed historical pattern.
     */
    private fun detectBatPattern(prices: List<Double>): Pair<PatternResult, PatternResult> {
        val pivots = findSwingPoints(prices)
        if (pivots.size < 4) {
            return PatternResult(false, 0, "--") to PatternResult(false, 0, "--")
        }
        val n = pivots.size
        val x = pivots[n - 4]
        val a = pivots[n - 3]
        val b = pivots[n - 2]
        val c = pivots[n - 1]
        val d = prices.last()

        val xa = a - x
        val ab = b - a
        if (xa == 0.0 || ab == 0.0) {
            return PatternResult(false, 0, "--") to PatternResult(false, 0, "--")
        }

        val abRetrace = abs(ab / xa)
        val bcRetrace = abs((c - b) / ab)
        val adRetrace = abs((d - a) / xa)

        val abOk = abRetrace in 0.382..0.500
        val bcOk = bcRetrace in 0.382..0.886
        val adOk = adRetrace in 0.786..0.886

        val bullishShape = x < a && b < a && b > x && c > b && c < a
        val bearishShape = x > a && b > a && b < x && c < b && c > a

        val bullishFound = bullishShape && abOk && bcOk && adOk && d <= c
        val bearishFound = bearishShape && abOk && bcOk && adOk && d >= c

        val bullishScore = if (bullishFound) 85 else if (bullishShape) 40 else 10
        val bearishScore = if (bearishFound) 85 else if (bearishShape) 40 else 10

        return PatternResult(
            bullishFound, bullishScore,
            if (bullishFound) "Bat D @ ${"%.2f".format(d)}" else "No bull bat"
        ) to PatternResult(
            bearishFound, bearishScore,
            if (bearishFound) "Bat D @ ${"%.2f".format(d)}" else "No bear bat"
        )
    }

    /**
     * Harmonic 5-0 pattern (bullish + bearish), using the widely-taught retail ratio bands:
     *   AB extends XA by 1.13-1.618 (continuation move beyond X)
     *   BC extends AB by 1.618-2.24 (retracement move beyond A)
     *   D completes at ~50% retracement of BC (the pattern's signature "50" level)
     * X-A-B-C are the last 4 confirmed swing points; D is the current (live) price, since
     * that is the point the pattern is still forming toward - this flags a potential
     * reversal zone rather than a confirmed historical pattern. Ratio bands are a common
     * approximation of Scott Carney's harmonic trading rules; tolerances can be tightened
     * later if false positives show up in practice.
     */
    private fun detect50Pattern(prices: List<Double>): Pair<PatternResult, PatternResult> {
        val pivots = findSwingPoints(prices)
        if (pivots.size < 4) {
            return PatternResult(false, 0, "--") to PatternResult(false, 0, "--")
        }
        val n = pivots.size
        val x = pivots[n - 4]
        val a = pivots[n - 3]
        val b = pivots[n - 2]
        val c = pivots[n - 1]
        val d = prices.last()

        val xa = a - x
        val ab = b - a
        val bc = c - b
        if (xa == 0.0 || ab == 0.0 || bc == 0.0) {
            return PatternResult(false, 0, "--") to PatternResult(false, 0, "--")
        }

        val abExt = abs(ab / xa)
        val bcExt = abs(bc / ab)
        val cdRetrace = abs((d - c) / bc)

        val abOk = abExt in 1.13..1.618
        val bcOk = bcExt in 1.618..2.24
        val cdOk = cdRetrace in 0.42..0.58

        // Bullish: X-A down, A-B extends further down past X, B-C retraces up, D forming a low
        val bullishShape = x > a && b < x && c > b && d < c
        // Bearish: X-A up, A-B extends further up past X, B-C retraces down, D forming a high
        val bearishShape = x < a && b > x && c < b && d > c

        val bullishFound = bullishShape && abOk && bcOk && cdOk
        val bearishFound = bearishShape && abOk && bcOk && cdOk

        val bullishScore = if (bullishFound) 85 else if (bullishShape) 40 else 10
        val bearishScore = if (bearishFound) 85 else if (bearishShape) 40 else 10

        return PatternResult(
            bullishFound, bullishScore,
            if (bullishFound) "5-0 D @ ${"%.2f".format(d)}" else "No bull 5-0"
        ) to PatternResult(
            bearishFound, bearishScore,
            if (bearishFound) "5-0 D @ ${"%.2f".format(d)}" else "No bear 5-0"
        )
    }
}
