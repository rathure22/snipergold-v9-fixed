package com.snipergold.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snipergold.app.ui.LocalAdaptive
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import com.snipergold.app.ui.theme.CardStroke
import com.snipergold.app.ui.MainViewModel
import com.snipergold.app.ui.components.CandleChart
import com.snipergold.app.ui.theme.DarkBg
import com.snipergold.app.ui.theme.Gold
import com.snipergold.app.ui.theme.GreenBull
import com.snipergold.app.ui.theme.PanelBg
import com.snipergold.app.ui.theme.RedBear
import com.snipergold.app.ui.theme.Zinc400
import com.snipergold.app.ui.theme.Zinc500
import com.snipergold.app.ui.theme.Zinc600
import com.snipergold.app.ui.theme.Zinc800
import com.snipergold.app.ui.theme.Zinc900
import com.snipergold.app.ui.theme.AccentTheme
import com.snipergold.app.ui.theme.OrangeAccent
import com.snipergold.app.ui.theme.BlueDual
import com.snipergold.app.ui.theme.YellowBoth
import com.snipergold.app.ui.theme.GreenSoft
import com.snipergold.app.ui.theme.CardBg
import com.snipergold.app.ui.theme.accentColor
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val adaptive = LocalAdaptive.current
    val scroll = rememberScrollState()
    var siftingKey by remember { mutableStateOf("") }
    var goldApiKey by remember { mutableStateOf("") }
    var journalEntry by remember { mutableStateOf("") }
    var journalExit by remember { mutableStateOf("") }
    var sidebarOpen by remember { mutableStateOf(false) }
    var signalPopup by remember { mutableStateOf<String?>(null) }
    var lastPopupKey by remember { mutableStateOf("") }

    LaunchedEffect(vm.signalState.signal, vm.currentPrice) {
        val sig = vm.signalState.signal
        if (sig == "BUY" || sig == "SELL") {
            val key = "$sig|${"%.2f".format(vm.currentPrice)}|${vm.signalState.reason.take(20)}"
            if (key != lastPopupKey) {
                lastPopupKey = key
                signalPopup = sig
                delay(8000)
                if (signalPopup == sig) signalPopup = null
            }
        }
    }

    val lastCandle = vm.candles.lastOrNull()
    val ohlc = if (lastCandle != null) {
        "O:${"%.1f".format(lastCandle.open)} H:${"%.1f".format(lastCandle.high)} L:${"%.1f".format(lastCandle.low)} C:${"%.1f".format(lastCandle.close)}"
    } else "Building…"

    val changePct = if (vm.prices.size >= 2) {
        val prev = vm.prices[vm.prices.size - 2]
        val d = vm.currentPrice - prev
        val pct = if (prev != 0.0) d / prev * 100 else 0.0
        d to pct
    } else 0.0 to 0.0

    Box(Modifier.fillMaxSize().background(DarkBg).statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = if (adaptive.isWide) 88.dp else 76.dp)
                .then(if (adaptive.isWide) Modifier else Modifier)
        ) {
            // Header
            Column(Modifier.fillMaxWidth().background(Color(0xFF080B10)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("🥇", fontSize = 19.sp)
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("XAU/USD", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            Spacer(Modifier.width(6.dp))
                            LiveBadge(vm.isLive, accentColor(vm.accentTheme))
                        }
                        Text("Gold Spot · Real-time", color = Zinc500, fontSize = 12.sp, maxLines = 1)
                    }
                    // Theme toggle GREEN / ORANGE
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (vm.accentTheme == AccentTheme.GREEN) GreenBull.copy(alpha = 0.2f) else OrangeAccent.copy(alpha = 0.2f))
                            .border(1.dp, if (vm.accentTheme == AccentTheme.GREEN) GreenBull else OrangeAccent, RoundedCornerShape(8.dp))
                            .clickable { vm.toggleTheme() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (vm.accentTheme == AccentTheme.GREEN) "🟢" else "🟠",
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { sidebarOpen = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Zinc400)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    Text("$" + "%.2f".format(vm.currentPrice), color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
                    Spacer(Modifier.width(8.dp))
                    val up = changePct.first >= 0
                    Text("%+.2f (%+.2f%%)%s".format(changePct.first, changePct.second, if (up) "▲" else "▼"), color = if (up) GreenBull else RedBear, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("● ${vm.source}", color = GreenBull, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("3 SOURCE · 1s", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val themeAccent = accentColor(vm.accentTheme)
                listOf("1m", "5m", "15m", "1H", "4H", "1D").forEach { label ->
                    Box(Modifier.clickable { vm.selectTimeframe(label) }) {
                        Chip(label, vm.selectedTf == label, themeAccent)
                    }
                }
                Chip("Candles", true, themeAccent)
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding).clip(RoundedCornerShape(12.dp)).background(CardBg).border(1.dp, CardStroke, RoundedCornerShape(12.dp))) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("XAU/USD", color = Zinc400, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(ohlc, color = Zinc500, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 6.dp))
                }
                CandleChart(vm.candles.toList(), vm.guideHigh, vm.guideLow, vm.currentPrice, Modifier.fillMaxWidth().height(adaptive.chartHeight))
            }

            // Momentum + RSI
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clip(RoundedCornerShape(10.dp)).background(CardBg).border(1.dp, CardStroke, RoundedCornerShape(10.dp)).padding(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Momentum", color = Zinc500, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("RSI(14): ${"%.1f".format(vm.rsi)} ${if (vm.rsi >= 50) "▲" else "▼"}", color = if (vm.rsi >= 50) GreenBull else RedBear, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth().height(28.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
                    val bars = vm.momentum.takeLast(32)
                    if (bars.isEmpty()) {
                        Text("…", color = Zinc600, fontSize = 13.sp)
                    } else {
                        bars.forEach { v ->
                            val h = (kotlin.math.abs(v) * 26f).coerceAtLeast(2f)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (v >= 0) GreenBull else RedBear)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding).clip(RoundedCornerShape(10.dp)).background(Zinc900).border(1.dp, CardStroke, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trend", color = Zinc500, fontSize = 13.sp, maxLines = 1)
                    Spacer(Modifier.width(8.dp))
                    val bull = vm.signalState.signal != "SELL"
                    Chip(when (vm.signalState.signal) { "BUY" -> "▲ UPTREND"; "SELL" -> "▼ DOWNTREND"; else -> "SCAN" }, true, if (bull) GreenBull else RedBear)
                }
                Text("Dir: ${vm.directionLabel}", color = if (vm.directionLabel == "BULLISH") GreenBull else RedBear, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Next ${vm.nextCandleSec}s", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
            }

            Spacer(Modifier.height(8.dp))
            // GOLD TRACKER — green border #00d08455
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = adaptive.contentPadding)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(1.5.dp, GreenBull.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🥇", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("GOLD TRACKER", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                }
                Spacer(Modifier.height(4.dp))
                Text("$" + "%.2f".format(vm.currentPrice), color = GreenBull, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
                Text("Updated ${vm.lastUpdateTime}", color = Zinc600, fontSize = 12.sp, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                // OHLC 4 stats
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OhlcStat("O", vm.ohlcOpen)
                    OhlcStat("H", vm.ohlcHigh)
                    OhlcStat("L", vm.ohlcLow)
                    OhlcStat("C", vm.ohlcClose)
                }
                Spacer(Modifier.height(6.dp))
                TrackerRow("Support", "$" + "%.2f".format(vm.guideLow))
                TrackerRow("Resistance", "$" + "%.2f".format(vm.guideHigh))
                TrackerRow("Direction", if (vm.directionLabel == "BULLISH") "▲ UP" else if (vm.directionLabel == "BEARISH") "▼ DOWN" else "—")
                TrackerRow("Bars", "${vm.candles.size}")
            }

            Spacer(Modifier.height(8.dp))
            // Forex Rates auto-update
            CardBox {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("FOREX RATES", color = Zinc500, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(vm.forexTime, color = Gold, fontSize = 13.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                vm.forexRates.forEach { fx ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(fx.pair, color = Zinc400, fontSize = 15.sp, maxLines = 1)
                        Text("%.2f".format(fx.rate), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            // DUAL OPT A / B / BOTH
            Row(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DualChip("A", BlueDual, vm.dualMode == "A", Modifier.weight(1f)) { vm.updateDualMode("A") }
                DualChip("B", GreenBull, vm.dualMode == "B", Modifier.weight(1f)) { vm.updateDualMode("B") }
                DualChip("BOTH", YellowBoth, vm.dualMode == "BOTH", Modifier.weight(1f)) { vm.updateDualMode("BOTH") }
            }
            Spacer(Modifier.height(6.dp))
            // Strat Grid A/B scores (same 18)
            Row(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StratCard("STRAT A", "Scalp", vm.stratAScore, BlueDual, Modifier.weight(1f))
                StratCard("STRAT B", "London", vm.stratBScore, GreenBull, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            // Mode row
            CardBox {
                Text("MODE · ${vm.dualMode} LIVE", color = YellowBoth, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("A Scalp · B London · C tick ${vm.tickMs}ms", color = Zinc500, fontSize = 13.sp, maxLines = 1)
            }
            Spacer(Modifier.height(6.dp))
            // Sensitivity + Speed
            CardBox {
                Text("SENSITIVITY", color = Zinc500, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("strict", "normal", "loose").forEach { s ->
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (vm.sensitivity == s) GreenBull.copy(alpha = 0.25f) else Zinc900).border(1.dp, if (vm.sensitivity == s) GreenBull else Zinc800, RoundedCornerShape(8.dp)).clickable { vm.updateSensitivity(s) }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text(s, color = if (vm.sensitivity == s) GreenBull else Zinc400, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("SPEED (Strategy C)", color = Zinc500, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(500, 1000, 2000, 3000).forEach { ms ->
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (vm.tickMs == ms) Gold.copy(alpha = 0.2f) else Zinc900).border(1.dp, if (vm.tickMs == ms) Gold else Zinc800, RoundedCornerShape(8.dp)).clickable { vm.updateTickMs(ms) }.padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Text("${ms}ms", color = if (vm.tickMs == ms) Gold else Zinc400, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            CardBox {
                Text("SESSION · ${vm.activeSession}", color = Zinc400, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    vm.sessions.forEach { s ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color.Black).border(1.dp, if (s.isOpen) GreenBull.copy(alpha = 0.5f) else Zinc800, RoundedCornerShape(8.dp)).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.name.take(3), color = Zinc500, fontSize = 12.sp, maxLines = 1)
                            Text(s.time, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
                            Text(if (s.isOpen) "OPEN" else "—", color = if (s.isOpen) GreenBull else Zinc600, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(PanelBg).border(1.dp, CardStroke, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Text("PATTERNS", color = Zinc500, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    MiniRow("W", if (vm.patterns.doubleBottom.found) "✓" else "—", vm.patterns.doubleBottom.found, true)
                    MiniRow("M", if (vm.patterns.doubleTop.found) "✓" else "—", vm.patterns.doubleTop.found, false)
                    MiniRow("Bull", if (vm.patterns.bullFlag.found) "✓" else "—", vm.patterns.bullFlag.found, true)
                    MiniRow("Bear", if (vm.patterns.bearFlag.found) "✓" else "—", vm.patterns.bearFlag.found, false)
                    MiniRow("Bat+", if (vm.patterns.bullishBat.found) "✓" else "—", vm.patterns.bullishBat.found, true)
                    MiniRow("Bat-", if (vm.patterns.bearishBat.found) "✓" else "—", vm.patterns.bearishBat.found, false)
                    MiniRow("5-0+", if (vm.patterns.bullish50.found) "✓" else "—", vm.patterns.bullish50.found, true)
                    MiniRow("5-0-", if (vm.patterns.bearish50.found) "✓" else "—", vm.patterns.bearish50.found, false)
                }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(PanelBg).border(1.dp, CardStroke, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Text("GUIDE", color = Zinc500, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    val above = vm.currentPrice > vm.guideHigh
                    val below = vm.currentPrice < vm.guideLow
                    MiniRow("High", if (above) "LUSOT" else "%.1f".format(vm.guideHigh - vm.currentPrice), above, true)
                    MiniRow("Low", if (below) "LUSOT" else "%.1f".format(vm.currentPrice - vm.guideLow), below, false)
                    MiniRow("OK", if (above || below) "YES" else "WAIT", above || below, true)
                }
            }

            Spacer(Modifier.height(8.dp))
            val sig = vm.signalState.signal
            val sigColor = when (sig) { "BUY" -> GreenBull; "SELL" -> RedBear; else -> Zinc600 }
            Column(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding).clip(RoundedCornerShape(14.dp)).background(if (sig == "WAIT") Color.Black else sigColor.copy(alpha = 0.12f)).border(2.dp, if (sig == "WAIT") Zinc800 else sigColor, RoundedCornerShape(14.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(when (sig) { "BUY" -> "BUY SIGNAL"; "SELL" -> "SELL SIGNAL"; else -> "WAIT" }, color = sigColor, fontSize = 27.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("$" + "%.2f".format(vm.currentPrice) + " · pattern + guide", color = Zinc400, fontSize = 14.sp, maxLines = 1)
            }

            if (vm.tpLevels.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                CardBox {
                    Text("TP1–TP5", color = Zinc500, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(Modifier.height(6.dp))
                    vm.tpLevels.forEach { tp ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TP${tp.n} ${tp.rr}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("$" + "%.2f".format(tp.price), color = Color.White, fontSize = 15.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ENTRY", color = Zinc500, fontSize = 11.sp, maxLines = 1)
                            Text("$" + "%.2f".format(vm.signalState.entry), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SL", color = Zinc500, fontSize = 11.sp, maxLines = 1)
                            Text("$" + "%.2f".format(vm.signalState.sl), color = RedBear, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LUSOT", color = Zinc500, fontSize = 11.sp, maxLines = 1)
                            Text("$" + "%.2f".format(vm.signalState.risk), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Ji NG · SLOW-STONE™ v6", color = Zinc600, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(16.dp), maxLines = 1)
        }

        // Floating Widget — collapse/expand
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(10f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
        ) {
            Row(
                Modifier.fillMaxWidth().clickable { vm.toggleFloating() }.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val c = when (vm.signalState.signal) { "BUY" -> GreenBull; "SELL" -> RedBear; else -> Zinc600 }
                    Box(Modifier.size(8.dp).clip(CircleShape).background(c))
                    Spacer(Modifier.width(8.dp))
                    Text(vm.signalState.signal, color = c, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Spacer(Modifier.width(8.dp))
                    Text("${vm.nextCandleSec}s", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
                Text(if (vm.floatingCollapsed) "▲" else "▼", color = Zinc400, fontSize = 15.sp)
            }
            if (!vm.floatingCollapsed) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Zinc800).clickable { vm.resumeScan() }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text("Reset / Scan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // Auto popup on guide match
        if (signalPopup != null) {
            Box(Modifier.fillMaxSize().zIndex(25f).background(Color.Black.copy(alpha = 0.45f)).clickable { signalPopup = null })
        }
        AnimatedVisibility(visible = signalPopup != null, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier.align(Alignment.Center).zIndex(30f).padding(24.dp)) {
            val pop = signalPopup ?: "WAIT"
            val popColor = if (pop == "BUY") GreenBull else RedBear
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.96f)).border(2.dp, popColor, RoundedCornerShape(20.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (pop == "BUY") "▲ BUY MATCH" else "▼ SELL MATCH", color = popColor, fontSize = 29.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Text("Pattern + Guide Lusot", color = Zinc400, fontSize = 15.sp, maxLines = 1)
                Text("$" + "%.2f".format(vm.currentPrice), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(vm.signalState.reason.take(80).ifEmpty { "Guide breakout confirmed" }, color = Zinc500, fontSize = 13.sp, maxLines = 3)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { signalPopup = null }, colors = ButtonDefaults.buttonColors(containerColor = Zinc800)) { Text("Dismiss", color = Color.White, fontSize = 15.sp) }
                    Button(onClick = { signalPopup = null }, colors = ButtonDefaults.buttonColors(containerColor = popColor)) {
                        Text(if (pop == "BUY") "OK BUY" else "OK SELL", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sidebar overlay
        if (sidebarOpen) {
            Box(Modifier.fillMaxSize().zIndex(40f).background(Color.Black.copy(alpha = 0.55f)).clickable { sidebarOpen = false })
        }
        AnimatedVisibility(visible = sidebarOpen, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(300.dp).zIndex(50f)) {
            Column(Modifier.fillMaxHeight().background(Color(0xFF0A0A0F)).border(1.dp, Zinc800).verticalScroll(rememberScrollState()).padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("SIDEBAR", color = Zinc500, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("SniperGold v9", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                    IconButton(onClick = { sidebarOpen = false }) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Zinc400) }
                }
                Text("Ji NG · SLOW-STONE™ v6", color = Zinc600, fontSize = 12.sp, maxLines = 1)
                Spacer(Modifier.height(14.dp))

                SideSection("MARKET DATA ENGINE") {
                    Text("Source: ${vm.source}", color = GreenBull, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(if (vm.isLive) "● LIVE ON" else "○ OFFLINE", color = if (vm.isLive) GreenBull else RedBear, fontSize = 13.sp, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = siftingKey, onValueChange = { siftingKey = it }, placeholder = { Text("SiftingIO key", color = Zinc600, fontSize = 13.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors())
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = goldApiKey, onValueChange = { goldApiKey = it }, placeholder = { Text("GoldAPI key", color = Zinc600, fontSize = 13.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors())
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { vm.setApiKeys(siftingKey, goldApiKey) }, colors = ButtonDefaults.buttonColors(containerColor = Zinc800), modifier = Modifier.weight(1f)) { Text("Save", color = Color.White, fontSize = 13.sp, maxLines = 1) }
                        Button(onClick = { vm.loadHistory() }, colors = ButtonDefaults.buttonColors(containerColor = GreenBull), modifier = Modifier.weight(1f)) { Text("OHLCV", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SideSection("SIGNAL HISTORY (AUTO)") {
                    if (vm.signalHistory.isEmpty()) Text("No signals yet", color = Zinc600, fontSize = 13.sp, maxLines = 1)
                    else vm.signalHistory.take(10).forEach { log ->
                        val isBuy = log.signal == "BUY"
                        val ac = if (isBuy) GreenBull else RedBear
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(ac.copy(alpha = 0.08f)).border(1.dp, ac.copy(alpha = 0.45f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "${log.signal} ${if (log.isNew) "● NEW" else ""} · ${log.time}",
                                    color = ac, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1
                                )
                            }
                            Text("Lot ${"%.2f".format(log.lot)} ${log.pnlHint} · entry ${log.entryN}", color = Zinc400, fontSize = 13.sp, maxLines = 1)
                            Text("Entry $${"%.2f".format(log.entry)} · SL $${"%.2f".format(log.sl)}", color = Color.White, fontSize = 13.sp, maxLines = 1)
                            Text("TP1 $${"%.2f".format(log.tp1)} · TP2 $${"%.2f".format(log.tp2)} · TP3 $${"%.2f".format(log.tp3)}", color = Zinc400, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { vm.clearSignalHistory() }, colors = ButtonDefaults.buttonColors(containerColor = Zinc800), modifier = Modifier.fillMaxWidth()) { Text("Clear", color = Zinc400, fontSize = 13.sp, maxLines = 1) }
                }

                Spacer(Modifier.height(12.dp))
                SideSection("CONNECTION LOG") {
                    if (vm.connLogs.isEmpty()) Text("Waiting…", color = Zinc600, fontSize = 13.sp, maxLines = 1)
                    else vm.connLogs.take(20).forEach { c ->
                        Text("${c.time}  ${if (c.ok) "✓" else "✗"} ${c.msg}", color = if (c.ok) GreenBull else RedBear, fontSize = 12.sp, maxLines = 1, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(12.dp))
                SideSection("CHAT") {
                    val ctx = LocalContext.current
                    Button(
                        onClick = {
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ipchat.in")))
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Open ipchat.in", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    Text("Gilid chat · external", color = Zinc600, fontSize = 12.sp, maxLines = 1)
                }
                Spacer(Modifier.height(12.dp))
                SideSection("TRADE JOURNAL") {
                    Text("A W${vm.journalWinsA}/L${vm.journalLossA} · B W${vm.journalWinsB}/L${vm.journalLossB}", color = Zinc400, fontSize = 13.sp, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = journalEntry, onValueChange = { journalEntry = it }, placeholder = { Text("Entry", color = Zinc600, fontSize = 13.sp) }, modifier = Modifier.weight(1f), singleLine = true, colors = fieldColors())
                        OutlinedTextField(value = journalExit, onValueChange = { journalExit = it }, placeholder = { Text("Exit", color = Zinc600, fontSize = 13.sp) }, modifier = Modifier.weight(1f), singleLine = true, colors = fieldColors())
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { vm.addJournal("A", "Win", journalEntry, journalExit); journalEntry = ""; journalExit = "" }, colors = ButtonDefaults.buttonColors(containerColor = GreenBull), modifier = Modifier.fillMaxWidth()) { Text("Add Entry", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    if (vm.journal.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        vm.journal.take(5).forEach { j -> Text("${j.strategy} ${j.result} · ${j.entry}→${j.exit}", color = Zinc400, fontSize = 13.sp, maxLines = 1) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { vm.clearJournal() }, colors = ButtonDefaults.buttonColors(containerColor = Zinc800), modifier = Modifier.fillMaxWidth()) { Text("Clear Journal", color = Zinc400, fontSize = 13.sp, maxLines = 1) }
                }
                Spacer(Modifier.height(12.dp))
                SideSection("TICK INTENSITY") {
                    Text("${vm.ticksPerMin} ticks/min", color = GreenBull, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text(when { vm.ticksPerMin >= 50 -> "HIGH activity"; vm.ticksPerMin >= 20 -> "MED activity"; else -> "LOW activity" }, color = Zinc400, fontSize = 14.sp, maxLines = 1)
                }
                Spacer(Modifier.height(20.dp))
                Text("☰ Sidebar · secondary tools", color = Zinc600, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable private fun CardBox(content: @Composable () -> Unit) {
    val adaptive = LocalAdaptive.current
    Column(Modifier.fillMaxWidth().padding(horizontal = adaptive.contentPadding).clip(RoundedCornerShape(12.dp)).background(PanelBg).border(1.dp, CardStroke, RoundedCornerShape(12.dp)).padding(12.dp)) { content() }
}
@Composable private fun SideSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PanelBg).border(1.dp, Zinc800, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(title, color = Zinc500, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(6.dp)); content()
    }
}
@Composable private fun LiveBadge(live: Boolean, accent: Color = GreenBull) {
    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if (live) accent.copy(alpha = 0.15f) else RedBear.copy(alpha = 0.15f)).border(1.dp, if (live) accent else RedBear, RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(if (live) "● LIVE" else "OFF", color = if (live) accent else RedBear, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
@Composable private fun Chip(label: String, on: Boolean, color: Color = GreenBull) {
    Box(Modifier.clip(RoundedCornerShape(16.dp)).background(if (on) color.copy(alpha = 0.2f) else Zinc900).border(1.dp, if (on) color else Zinc800, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(label, color = if (on) color else Zinc400, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
@Composable private fun TrackerRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Zinc500, fontSize = 14.sp, maxLines = 1)
        Text(value, color = Color.White, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
@Composable private fun MiniRow(label: String, value: String, active: Boolean, bull: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Zinc400, fontSize = 14.sp, maxLines = 1)
        Text(
            value,
            color = when { active && bull -> GreenBull; active && !bull -> RedBear; else -> Zinc600 },
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 56.dp)
        )
    }
}
@Composable private fun OhlcStat(label: String, v: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Zinc500, fontSize = 12.sp, maxLines = 1)
        Text(if (v > 0) "%.1f".format(v) else "—", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
@Composable private fun DualChip(label: String, color: Color, on: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(if (on) color.copy(alpha = 0.18f) else Zinc900).border(1.dp, if (on) color else Zinc800, RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (on) color else Zinc400, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}
@Composable private fun StratCard(title: String, sub: String, score: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.08f)).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(sub, color = Zinc500, fontSize = 12.sp, maxLines = 1)
        Text("$score", color = color, fontSize = 21.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}
@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Zinc800, focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Gold)
