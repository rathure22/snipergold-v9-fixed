
package com.snipergold.v9.binance

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject

object SecureApiStorage {
    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "sniper_gold_keys_v9",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveKeys(context: Context, goldApiKey: String, binanceKey: String, binanceSecret: String, lot: Double = 0.02) {
        getPrefs(context).edit()
            .putString("gold_api_key", goldApiKey)
            .putString("binance_api_key", binanceKey)
            .putString("binance_secret", binanceSecret)
            .putFloat("lot_size", lot.toFloat())
            .apply()
    }

    fun loadKeys(context: Context): Triple<String, String, String> {
        val p = getPrefs(context)
        return Triple(
            p.getString("gold_api_key","") ?: "",
            p.getString("binance_api_key","") ?: "",
            p.getString("binance_secret","") ?: ""
        )
    }
    
    fun loadLot(context: Context): Double {
        return getPrefs(context).getFloat("lot_size", 0.02f).toDouble()
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}

data class TradeHistoryItem(
    val timestamp: Long,
    val symbol: String,
    val side: String,
    val entry: Double,
    val sl: Double,
    val tp1: Double,
    val tp2: Double,
    val lot: Double,
    val status: String,
    val orderId: String = ""
)

object TradeHistoryStorage {
    private const val PREF_NAME = "trade_history_prefs"
    private const val KEY_HISTORY = "history_json"

    fun saveTrade(context: Context, trade: TradeHistoryItem) {
        val history = loadHistory(context).toMutableList()
        history.add(trade)
        if (history.size > 100) history.removeAt(0)
        val jsonArray = JSONArray()
        history.forEach { t ->
            val obj = JSONObject()
            obj.put("timestamp", t.timestamp)
            obj.put("symbol", t.symbol)
            obj.put("side", t.side)
            obj.put("entry", t.entry)
            obj.put("sl", t.sl)
            obj.put("tp1", t.tp1)
            obj.put("tp2", t.tp2)
            obj.put("lot", t.lot)
            obj.put("status", t.status)
            obj.put("orderId", t.orderId)
            jsonArray.put(obj)
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun loadHistory(context: Context): List<TradeHistoryItem> {
        val jsonStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                TradeHistoryItem(
                    timestamp = obj.getLong("timestamp"),
                    symbol = obj.getString("symbol"),
                    side = obj.getString("side"),
                    entry = obj.getDouble("entry"),
                    sl = obj.getDouble("sl"),
                    tp1 = obj.getDouble("tp1"),
                    tp2 = obj.getDouble("tp2"),
                    lot = obj.getDouble("lot"),
                    status = obj.getString("status"),
                    orderId = obj.optString("orderId","")
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}
