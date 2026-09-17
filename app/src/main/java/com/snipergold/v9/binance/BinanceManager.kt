
package com.snipergold.v9.binance

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object BinanceManager {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://api.binance.com"

    private fun hmacSHA256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    data class OrderResult(
        val success: Boolean,
        val orderId: String,
        val message: String,
        val realPrice: Double = 0.0
    )

    // PARA SA BALANCE SYNC - kuha USDT ug PAXG balance
    suspend fun getBalanceParsed(context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        val (apiKey, secret) = SecureApiStorage.loadKeys(context).let { Pair(it.second, it.third) }
        if (apiKey.isEmpty()) return@withContext Pair("0.00", "0.00")
        
        val timestamp = System.currentTimeMillis()
        val query = "timestamp=$timestamp"
        val signature = hmacSHA256(query, secret)
        val url = "$BASE_URL/api/v3/account?$query&signature=$signature"
        
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("X-MBX-APIKEY", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Pair("0.00", "0.00")
            
            val json = JSONObject(body)
            val balances = json.getJSONArray("balances")
            var usdt = "0.00"
            var paxg = "0.00"
            for (i in 0 until balances.length()) {
                val bal = balances.getJSONObject(i)
                val asset = bal.getString("asset")
                val free = bal.getString("free")
                if (asset == "USDT") usdt = free
                if (asset == "PAXG") paxg = free
            }
            return@withContext Pair(usdt, paxg)
        } catch (e: Exception) {
            return@withContext Pair("0.00", "0.00")
        }
    }

    suspend fun getBalance(context: Context): String = withContext(Dispatchers.IO) {
        val (apiKey, secret) = SecureApiStorage.loadKeys(context).let { Pair(it.second, it.third) }
        if (apiKey.isEmpty()) return@withContext "No API Key"
        val timestamp = System.currentTimeMillis()
        val query = "timestamp=$timestamp"
        val signature = hmacSHA256(query, secret)
        val url = "$BASE_URL/api/v3/account?$query&signature=$signature"
        try {
            val request = Request.Builder().url(url).get().addHeader("X-MBX-APIKEY", apiKey).build()
            val response = client.newCall(request).execute()
            return@withContext response.body?.string() ?: "Error"
        } catch (e: Exception) {
            return@withContext "Error: ${e.message}"
        }
    }

    suspend fun placeRealOrder(
        context: Context,
        side: String,
        lot: Double,
        entryPrice: Double = 4376.36
    ): OrderResult = withContext(Dispatchers.IO) {
        val (apiKey, secret) = SecureApiStorage.loadKeys(context).let { Pair(it.second, it.third) }
        if (apiKey.isEmpty() || secret.isEmpty()) {
            return@withContext OrderResult(false, "", "Wala pay Binance API Key - i-save sa sa SideBar")
        }
        val symbol = "PAXGUSDT"
        val quantity = lot
        val timestamp = System.currentTimeMillis()
        val query = "symbol=$symbol&side=$side&type=MARKET&quantity=$quantity&timestamp=$timestamp"
        val signature = hmacSHA256(query, secret)
        val finalUrl = "$BASE_URL/api/v3/order?$query&signature=$signature"
        try {
            val request = Request.Builder()
                .url(finalUrl)
                .post("".toRequestBody("application/json".toMediaType()))
                .addHeader("X-MBX-APIKEY", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val orderId = json.optString("orderId", "BIN_${timestamp}")
                val filledPrice = json.optJSONArray("fills")?.let { fills ->
                    if (fills.length() > 0) fills.getJSONObject(0).optDouble("price", entryPrice) else entryPrice
                } ?: entryPrice
                val trade = TradeHistoryItem(
                    timestamp = timestamp,
                    symbol = "PAXG/USDT (XAU $${entryPrice})",
                    side = side,
                    entry = filledPrice,
                    sl = if (side == "SELL") filledPrice + 0.93 else filledPrice - 0.93,
                    tp1 = if (side == "SELL") filledPrice - 1.42 else filledPrice + 1.42,
                    tp2 = if (side == "SELL") filledPrice - 2.04 else filledPrice + 2.04,
                    lot = lot,
                    status = "REAL - BINANCE ✓",
                    orderId = orderId
                )
                TradeHistoryStorage.saveTrade(context, trade)
                OrderResult(true, orderId, "REAL $side $lot Lot - OrderID: $orderId - Price: $filledPrice", filledPrice)
            } else {
                val json = JSONObject(body)
                val msg = json.optString("msg", body)
                OrderResult(false, "", "Binance Error: $msg")
            }
        } catch (e: Exception) {
            OrderResult(false, "", "Error: ${e.message}")
        }
    }
}
