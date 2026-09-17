
package com.snipergold.v9.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snipergold.v9.binance.BinanceManager
import com.snipergold.v9.binance.SecureApiStorage
import com.snipergold.v9.binance.TradeHistoryItem
import com.snipergold.v9.binance.TradeHistoryStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBarScreen(context: Context, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val (savedGold, savedBinKey, savedBinSec) = remember { SecureApiStorage.loadKeys(context) }
    var goldApiKey by remember { mutableStateOf(savedGold) }
    var binanceKey by remember { mutableStateOf(savedBinKey) }
    var binanceSecret by remember { mutableStateOf(savedBinSec) }
    var selectedLot by remember { mutableStateOf(SecureApiStorage.loadLot(context)) }
    val lotOptions = listOf(0.01, 0.02, 0.03, 0.05, 0.10)
    var tradeHistory by remember { mutableStateOf(TradeHistoryStorage.loadHistory(context)) }
    var isSyncEnabled by remember { mutableStateOf(false) }
    var usdtBalance by remember { mutableStateOf("0.00") }
    var paxgBalance by remember { mutableStateOf("0.00") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D)).padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SniperGold v9", color = Color(0xFFE8D5A3), fontSize = 18.sp)
            TextButton(onClick = onClose) { Text("X", color = Color.White) }
        }
        Text("Ji NG - SLOW-STONE v6", color = Color.Gray, fontSize = 10.sp)
        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("MARKET DATA ENGINE", color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                Text("Source: gold-api.com", color = Color(0xFF00D18C), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Color(0xFF00FF88), RoundedCornerShape(50)))
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE ON", color = Color(0xFF00D18C), fontSize = 11.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = goldApiKey, onValueChange = { goldApiKey = it }, label = { Text("GoldAPI Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = binanceKey, onValueChange = { binanceKey = it }, label = { Text("Binance API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = binanceSecret, onValueChange = { binanceSecret = it }, label = { Text("Binance Secret") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                Text("Lot Size (Binance Multi)", color = Color.Gray, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lotOptions.forEach { lot ->
                        FilterChip(
                            selected = selectedLot == lot,
                            onClick = { selectedLot = lot },
                            label = { Text("$lot") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00D18C), selectedLabelColor = Color.Black)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            SecureApiStorage.saveKeys(context, goldApiKey, binanceKey, binanceSecret, selectedLot)
                            Toast.makeText(context, "Saved! Lot $selectedLot", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
                    ) { Text("Save") }
                    Button(
                        onClick = { Toast.makeText(context, "OHLCV Live $selectedLot", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D18C))
                    ) { Text("OHLCV", color = Color.Black) }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sync Balance", color = Color.White, fontSize = 12.sp)
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
                    Spacer(Modifier.height(6.dp))
                    Text("USDT: $$usdtBalance | PAXG: $paxgBalance", color = Color(0xFF00D18C), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("SIGNAL HISTORY (AUTO) - BINANCE REAL", color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0F0F)), modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (tradeHistory.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Wala pay trade. Inig gamit Binance, auto log diri.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tradeHistory.reversed()) { trade ->
                        TradeHistoryCard(trade)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    val result = BinanceManager.placeRealOrder(context, "SELL", selectedLot, 4376.36)
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    tradeHistory = TradeHistoryStorage.loadHistory(context)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text("REAL SELL $selectedLot Lot sa Binance")
        }
    }
}

@Composable
fun TradeHistoryCard(trade: TradeHistoryItem) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = sdf.format(Date(trade.timestamp))
    Card(colors = CardDefaults.cardColors(containerColor = if(trade.side=="SELL") Color(0xFF3D1A1A) else Color(0xFF1A3D1A)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${trade.side} - ${trade.status}", color = if(trade.side=="SELL") Color(0xFFFF6B6B) else Color(0xFF6BFF6B), fontSize = 12.sp)
                Text(timeStr, color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Lot ${trade.lot} - Entry $${trade.entry} - SL $${trade.sl}", color = Color(0xFFCCCCCC), fontSize = 11.sp)
            Text("TP1 $${trade.tp1} - TP2 $${trade.tp2} - ${trade.symbol}", color = Color(0xFF999999), fontSize = 10.sp)
            if(trade.orderId.isNotEmpty()) {
                Text("OrderID: ${trade.orderId}", color = Color(0xFF666666), fontSize = 9.sp)
            }
        }
    }
}
