package com.snipergold.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MarketDataEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    var source: String = "gold-api.com"
        private set

    var lastHistoryCount: Int = 0
        private set

    // Optional keys (can be set from UI / SharedPreferences)
    var siftingKey: String = ""
    var goldApiKey: String = ""

    suspend fun fetchGoldPrice(): Double = withContext(Dispatchers.IO) {
        // 1. Try SiftingIO
        if (siftingKey.isNotBlank()) {
            try {
                val p = siftingQuote()
                source = "SiftingIO"
                return@withContext p
            } catch (_: Exception) {}
        }
        // 2. Try GoldAPI.io
        if (goldApiKey.isNotBlank()) {
            try {
                val p = goldApiQuote()
                source = "GoldAPI.io"
                return@withContext p
            } catch (_: Exception) {}
        }
        // 3. Public fallback: gold-api.com
        try {
            val p = publicGoldQuote()
            source = "gold-api.com"
            return@withContext p
        } catch (_: Exception) {}

        // 4. Public fallback: goldprice.org (no key needed)
        try {
            val p = goldpriceOrgQuote()
            source = "goldprice.org"
            return@withContext p
        } catch (_: Exception) {}

        throw Exception("All market data sources unavailable")
    }

    private fun goldpriceOrgQuote(): Double {
        val req = Request.Builder()
            .url("https://data-asg.goldprice.org/dbXRates/USD")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("goldprice.org ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("empty")
            val json = JSONObject(body)
            val items = json.getJSONArray("items")
            val p = items.getJSONObject(0).optDouble("xauPrice", Double.NaN)
            if (!p.isFinite()) throw Exception("bad price")
            return p
        }
    }

    private fun siftingQuote(): Double {
        val req = Request.Builder()
            .url("https://api.sifting.io/v1/last/trade/commodities/XAUUSD")
            .addHeader("X-API-Key", siftingKey)
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Sifting ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("empty")
            val json = JSONObject(body)
            val p = json.optDouble("p", Double.NaN)
            if (!p.isFinite()) throw Exception("bad price")
            return p
        }
    }

    private fun goldApiQuote(): Double {
        val req = Request.Builder()
            .url("https://www.goldapi.io/api/price/XAU/USD")
            .addHeader("x-access-token", goldApiKey)
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("GoldAPI ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("empty")
            val json = JSONObject(body)
            val p = json.optDouble("price", Double.NaN)
            if (!p.isFinite()) throw Exception("bad price")
            return p
        }
    }

    private fun publicGoldQuote(): Double {
        val req = Request.Builder()
            .url("https://api.gold-api.com/price/XAU")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("gold-api ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("empty")
            val json = JSONObject(body)
            val p = json.optDouble("price", Double.NaN)
            if (!p.isFinite()) throw Exception("bad price")
            return p
        }
    }

    suspend fun loadHistory(): List<Candle> = withContext(Dispatchers.IO) {
        if (siftingKey.isBlank()) throw Exception("SiftingIO key required for history")
        val url = "https://api.sifting.io/v1/hist/commodities/XAUUSD/bars?interval=1m&limit=180"
        val req = Request.Builder()
            .url(url)
            .addHeader("X-API-Key", siftingKey)
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("History ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("empty")
            val json = JSONObject(body)
            val arr = when {
                json.has("data") -> json.getJSONArray("data")
                json.has("bars") -> json.getJSONArray("bars")
                else -> throw Exception("No bars")
            }
            val list = mutableListOf<Candle>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val t = o.optLong("t", o.optLong("time", o.optLong("timestamp", 0)))
                val open = o.optDouble("open", Double.NaN)
                val high = o.optDouble("high", Double.NaN)
                val low = o.optDouble("low", Double.NaN)
                val close = o.optDouble("close", Double.NaN)
                if (listOf(open, high, low, close).all { it.isFinite() }) {
                    list.add(Candle(t, open, high, low, close, o.optDouble("v", 0.0)))
                }
            }
            lastHistoryCount = list.size
            source = "SiftingIO"
            list.takeLast(180)
        }
    }


    /** Public FX rates → PHP (auto update). Fallback static if offline. */
    suspend fun fetchForexRates(): List<ForexRate> = withContext(Dispatchers.IO) {
        // 1. Unang priority: open.er-api.com (walay key, naay TINUOD nga KWD data — 161 currencies)
        try {
            val req = Request.Builder()
                .url("https://open.er-api.com/v6/latest/USD")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("er-api ${resp.code}")
                val body = resp.body?.string() ?: throw Exception("empty")
                val json = JSONObject(body)
                if (json.optString("result") != "success") throw Exception("er-api bad result")
                val rates = json.getJSONObject("rates")
                val usdPhp = rates.getDouble("PHP")
                val eurPerUsd = rates.getDouble("EUR")
                val kwdPerUsd = rates.getDouble("KWD")
                val eurPhp = usdPhp / eurPerUsd
                val kwdPhp = usdPhp / kwdPerUsd
                return@withContext listOf(
                    ForexRate("USD→PHP", usdPhp),
                    ForexRate("EUR→PHP", eurPhp),
                    ForexRate("KWD→PHP", kwdPhp)
                )
            }
        } catch (_: Exception) {}

        // 2. Fallback: frankfurter.app (ECB — walay KWD, gi-estimate na lang)
        try {
            val req = Request.Builder()
                .url("https://api.frankfurter.app/latest?from=USD&to=PHP,EUR")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("fx ${resp.code}")
                val body = resp.body?.string() ?: throw Exception("empty")
                val json = JSONObject(body)
                val rates = json.getJSONObject("rates")
                val usdPhp = rates.optDouble("PHP", 62.91)
                val eurPerUsd = rates.optDouble("EUR", 0.92)
                val eurPhp = if (eurPerUsd > 0) usdPhp / eurPerUsd else 72.60
                val kwdPhp = usdPhp * 3.25 // estimate — walay KWD sa ECB/frankfurter
                return@withContext listOf(
                    ForexRate("USD→PHP", usdPhp),
                    ForexRate("EUR→PHP", eurPhp),
                    ForexRate("KWD→PHP", kwdPhp)
                )
            }
        } catch (_: Exception) {
            listOf(
                ForexRate("USD→PHP", 62.91),
                ForexRate("EUR→PHP", 72.60),
                ForexRate("KWD→PHP", 204.24)
            )
        }
    }
}
