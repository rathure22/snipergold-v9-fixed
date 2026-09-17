package com.snipergold.app.data

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
)

data class PatternResult(
    val found: Boolean,
    val score: Int,
    val desc: String
)

data class Patterns(
    val doubleBottom: PatternResult,
    val doubleTop: PatternResult,
    val bullFlag: PatternResult,
    val bearFlag: PatternResult,
    val bullishBat: PatternResult,
    val bearishBat: PatternResult,
    val bullish50: PatternResult,
    val bearish50: PatternResult
)

data class SignalState(
    val signal: String = "WAIT",
    val bullish: Boolean = false,
    val reason: String = "",
    val entry: Double = 0.0,
    val sl: Double = 0.0,
    val risk: Double = 0.0,
    val guideHigh: Double = 0.0,
    val guideLow: Double = 0.0
)

data class SessionInfo(
    val name: String,
    val time: String,
    val isOpen: Boolean
)

data class JournalEntry(
    val strategy: String,
    val result: String,
    val entry: String,
    val exit: String,
    val time: String
)

data class SignalLog(
    val signal: String,
    val time: String,
    val price: Double,
    val reason: String,
    val isNew: Boolean = true,
    val lot: Double = 0.02,
    val entryN: Int = 1,
    val entry: Double = 0.0,
    val sl: Double = 0.0,
    val tp1: Double = 0.0,
    val tp2: Double = 0.0,
    val tp3: Double = 0.0,
    val pnlHint: String = ""
)

data class TpLevel(
    val n: Int,
    val price: Double,
    val rr: String,
    val pct: String,
    val isFinal: Boolean = false
)

data class ForexRate(
    val pair: String,
    val rate: Double
)

data class ConnLog(
    val time: String,
    val msg: String,
    val ok: Boolean
)
