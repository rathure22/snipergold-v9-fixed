package com.snipergold.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snipergold.app.data.Candle
import java.util.Locale

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    guideHigh: Double,
    guideLow: Double,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(8.dp)
    ) {
        // SLOW-STONE v6 Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SLOW-STONE™ v6",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Develop by Ji NG",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LOGIC",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "PATTERN+GUIDE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Chart Canvas Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Chart Sub-header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎯 GUIDE LINES • PATTERN BASE • 1-SEC REAL",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.US, "H:%.2f L:%.2f", guideHigh, guideLow),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chart Grid & Price Badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val strokeWidth = 1.dp.toPx()
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Grid Lines
                        val levels = 5
                        for (i in 0..levels) {
                            val y = height * (i.toFloat() / levels)
                            drawLine(
                                color = Color(0xFF2B2B2B),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = strokeWidth,
                                pathEffect = dashEffect
                            )
                        }

                        // Vertical Crosshair
                        drawLine(
                            color = Color(0xFF2B2B2B),
                            start = Offset(width * 0.3f, 0f),
                            end = Offset(width * 0.3f, height),
                            strokeWidth = strokeWidth,
                            pathEffect = dashEffect
                        )
                    }

                    // Right Axis Price Badges
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        BadgeBox(text = String.format(Locale.US, "%.2f", guideHigh), bgColor = Color(0xFFDC2626))
                        BadgeBox(text = String.format(Locale.US, "%.2f", (guideHigh + guideLow) / 2), bgColor = Color(0xFF334155))
                        BadgeBox(text = String.format(Locale.US, "%.2f", currentPrice), bgColor = Color(0xFF16A34A))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Chart Status Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("GUIDE HIGH", color = Color.Gray, fontSize = 9.sp)
                        Text(String.format(Locale.US, "%.2f", guideHigh), color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("GUIDE LOW", color = Color.Gray, fontSize = 9.sp)
                        Text(String.format(Locale.US, "%.2f", guideLow), color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PATTERN", color = Color.Gray, fontSize = 9.sp)
                        Text("M", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Detected Patterns & Breakout Check Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Card: Detected Patterns
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("DETECTED PATTERNS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Double Bottom (W)", color = Color.White, fontSize = 10.sp)
                        Text("--", color = Color.Gray, fontSize = 10.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Double Top (M)", color = Color.White, fontSize = 10.sp)
                        Text("FOUND ✓", color = Color(0xFFF87171), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Right Card: Guide Breakout Check
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("GUIDE BREAKOUT CHECK", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val diffHigh = (guideHigh - currentPrice).coerceAtLeast(0.0)
                    val diffLow = (currentPrice - guideLow).coerceAtLeast(0.0)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price vs High", color = Color.White, fontSize = 10.sp)
                        Text(String.format(Locale.US, "%.2f away", diffHigh), color = Color.Gray, fontSize = 10.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price vs Low", color = Color.White, fontSize = 10.sp)
                        Text(String.format(Locale.US, "%.2f away", diffLow), color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeBox(text: String, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
