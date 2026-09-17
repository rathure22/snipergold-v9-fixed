
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
import kotlinx.coroutines.launch

@Composable
fun BalanceSyncToggle(context: Context) {
    val scope = rememberCoroutineScope()
    var isSyncEnabled by remember { mutableStateOf(false) }
    var usdtBalance by remember { mutableStateOf("0.00") }
    var paxgBalance by remember { mutableStateOf("0.00") }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BINANCE BALANCE", color = Color.Gray, fontSize = 11.sp)
                Switch(checked = isSyncEnabled, onCheckedChange = { enabled ->
                    isSyncEnabled = enabled
                    if(enabled) {
                        scope.launch {
                            val bal = BinanceManager.getBalanceParsed(context)
                            usdtBalance = bal.first
                            paxgBalance = bal.second
                        }
                    }
                })
            }
            if(isSyncEnabled) {
                Spacer(Modifier.height(8.dp))
                Text("USDT: $$usdtBalance | PAXG: $paxgBalance", color = Color(0xFF00D18C), fontSize = 12.sp)
            }
        }
    }
}
