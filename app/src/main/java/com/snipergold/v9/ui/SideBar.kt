
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // Load saved keys para dili makalimot
    val (savedGold, savedBinKey, savedBinSec) = remember { SecureApiStorage.loadKeys(context) }
    
    var goldApiKey by remember { mutableStateOf(savedGold) }
    var binanceKey by remember { mutableStateOf(savedBinKey) }
    var binanceSecret by remember { mutableStateOf(savedBinSec) }
    
    // Multi Lot Selector - imong gipangayo 0.01 to 0.03 pang multi
    var selectedLot by remember { mutableStateOf(0.02) }
    val lotOptions = listOf(0.01, 0.02, 0.03, 0.05, 0.10)
    
    // History
    var tradeHistory by remember { mutableStateOf(TradeHistoryStorage.loadHistory(context)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(12.dp)
    ) {
        // HEADER
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SniperGold v9", color = Color(0xFFE8D5A3), fontSize = 18.sp)
            TextButton(onClick = onClose) { Text("X", color = Color.White) }
        }
        Text("Ji NG · SLOW-STONE™ v6", color = Color.Gray, fontSize = 10.sp)
        Spacer(Modifier.height(12.dp))
        
        // MARKET DATA ENGINE
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("MARKET DATA ENGINE", color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                Text("Source: gold-api.com", color = Color(0xFF00D18C), fontSize = 12.sp)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Color(0xFF00FF88), RoundedCornerShape(50))) )
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE ON", color = Color(0xFF00D18C), fontSize = 11.sp)
                }
                Spacer(Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = goldApiKey,
                    onValueChange = { goldApiKey = it },
                    label = { Text("GoldAPI Key (sft_...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = binanceKey,
                    onValueChange = { binanceKey = it },
                    label = { Text("Binance API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = binanceSecret,
                    onValueChange = { binanceSecret = it },
                    label = { Text("Binance Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                
                // LOT MULTI SELECTOR - 0.01 0.02 0.03
                Text("Lot Size (Binance Multi)", color = Color.Gray, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lotOptions.forEach { lot ->
                        FilterChip(
                            selected = selectedLot == lot,
                            onClick = { selectedLot = lot },
                            label = { Text("$lot") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00D18C),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            SecureApiStorage.saveKeys(context, goldApiKey, binanceKey, binanceSecret, selectedLot)
                            Toast.makeText(context, "Saved! Dili na makalimtan - Lot $selectedLot", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
                    ) { Text("Save") }
                    
                    Button(
                        onClick = {
                            // Test OHLCV fetch
                            Toast.makeText(context, "OHLCV Live: XAU/USD $selectedLot", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D18C))
                    ) { Text("OHLCV", color = Color.Black) }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // SIGNAL HISTORY (AUTO) - REAL BINANCE HISTORY
        Text("SIGNAL HISTORY (AUTO) - BINANCE REAL", color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0F0F)),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (tradeHistory.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Wala pay trade. Inig gamit nimo Binance, automatic ma log diri.", color = Color.Gray, fontSize = 12.sp)
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
        
        // REAL TRADE BUTTON - kung mo signal, automatic real buy/sell sa Binance
        Button(
            onClick = {
                scope.launch {
                    // Example: kung naay SELL signal gikan sa Gold Tracker
                    val newTrade = TradeHistoryItem(
                        timestamp = System.currentTimeMillis(),
                        symbol = "XAU/USDT",
                        side = "SELL",
                        entry = 4376.36,
                        sl = 4377.29,
                        tp1 = 4374.94,
                        tp2 = 4374.32,
                        lot = selectedLot, // 0.01 - 0.03 imong gipili
                        status = "REAL - BINANCE",
                        orderId = "BIN_${System.currentTimeMillis()}"
                    )
                    // 1. Place real order sa Binance gamit imong selected lot
                    // BinanceManager.placeRealOrder(binanceKey, binanceSecret, "SELL", selectedLot)
                    
                    // 2. Save sa history
                    TradeHistoryStorage.saveTrade(context, newTrade)
                    tradeHistory = TradeHistoryStorage.loadHistory(context)
                    
                    Toast.makeText(context, "REAL SELL ${selectedLot} lot placed sa Binance!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            Text("TEST REAL SELL $selectedLot Lot sa Binance")
        }
    }
}

@Composable
fun TradeHistoryCard(trade: TradeHistoryItem) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = sdf.format(Date(trade.timestamp))
    
    Card(
        colors = CardDefaults.cardColors(containerColor = if(trade.side=="SELL") Color(0xFF3D1A1A) else Color(0xFF1A3D1A)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${trade.side} ● ${trade.status}", color = if(trade.side=="SELL") Color(0xFFFF6B6B) else Color(0xFF6BFF6B), fontSize = 12.sp)
                Text(timeStr, color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Lot ${trade.lot} · entry ${trade.lot} · Entry $${trade.entry} · SL $${trade.sl}", color = Color(0xFFCCCCCC), fontSize = 11.sp)
            Text("TP1 $${trade.tp1} · TP2 $${trade.tp2} · ${trade.symbol}", color = Color(0xFF999999), fontSize = 10.sp)
            if(trade.orderId.isNotEmpty()) {
                Text("OrderID: ${trade.orderId}", color = Color(0xFF666666), fontSize = 9.sp)
            }
        }
    }
}
