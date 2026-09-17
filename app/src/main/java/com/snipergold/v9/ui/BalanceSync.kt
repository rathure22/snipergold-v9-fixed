
package com.snipergold.v9.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snipergold.v9.binance.BinanceManager
import com.snipergold.v9.binance.SecureApiStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun BalanceSyncToggle(context: Context) {
    val scope = rememberCoroutineScope()
    var isSyncEnabled by remember { 
        mutableStateOf(context.getSharedPreferences("sniper_prefs", Context.MODE_PRIVATE).getBoolean("sync_enabled", false))
    }
    var usdtBalance by remember { mutableStateOf("0.00") }
    var paxgBalance by remember { mutableStateOf("0.00") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Auto sync kada 5 seconds kung naka ON ang toggle
    LaunchedEffect(isSyncEnabled) {
        while(isSyncEnabled) {
            isLoading = true
            val result = BinanceManager.getBalanceParsed(context)
            usdtBalance = result.first
            paxgBalance = result.second
            isLoading = false
            delay(5000) // sync every 5 sec
        }
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("BINANCE BALANCE", color = Color.Gray, fontSize = 11.sp)
                    Text("Auto Sync", color = Color.White, fontSize = 14.sp)
                }
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = { enabled ->
                        isSyncEnabled = enabled
                        context.getSharedPreferences("sniper_prefs", Context.MODE_PRIVATE)
                            .edit().putBoolean("sync_enabled", enabled).apply()
                        
                        if(enabled) {
                            // Pag ON dayon sync
                            scope.launch {
                                isLoading = true
                                val result = BinanceManager.getBalanceParsed(context)
                                usdtBalance = result.first
                                paxgBalance = result.second
                                isLoading = false
                            }
                        }
                    }
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            if(isSyncEnabled) {
                if(isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF00D18C))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("USDT", color = Color.Gray, fontSize = 10.sp)
                        Text("$$usdtBalance", color = Color(0xFF00D18C), fontSize = 16.sp)
                    }
                    Column {
                        Text("PAXG (Gold)", color = Color.Gray, fontSize = 10.sp)
                        Text("$paxgBalance PAXG", color = Color(0xFFE8D5A3), fontSize = 16.sp)
                    }
                    Column {
                        Text("~Value", color = Color.Gray, fontSize = 10.sp)
                        val total = try { paxgBalance.toDouble() * 4376.0 } catch(e: Exception) { 0.0 }
                        Text("$${String.format("%.2f", total)}", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                Text("I-ON ang toggle para ma sync imong balance", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
