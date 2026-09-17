package com.snipergold.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.snipergold.app.data.Candle
import com.snipergold.app.ui.theme.GreenBull
import com.snipergold.app.ui.theme.RedBear

@Composable
fun CandleChart(
    candles: List<Candle>,
    guideHigh: Double,
    guideLow: Double,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF12110F))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val W = size.width
            val H = size.height
            if (candles.size < 2) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 32f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("Building real candles…", W / 2f, H / 2f, paint)
                }
                return@Canvas
            }

            val vis = candles.takeLast(48)
            var min = vis.minOf { it.low }.coerceAtMost(guideLow)
            var max = vis.maxOf { it.high }.coerceAtLeast(guideHigh)
            val pad = ((max - min) * 0.12).coerceAtLeast(0.8)
            min -= pad
            max += pad
            val range = (max - min).coerceAtLeast(0.01)
            fun py(v: Double) = (H - 20f - ((v - min) / range * (H - 36f)).toFloat())

            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)

            // Resistance zone (guide high band)
            val yRes = py(guideHigh)
            drawRect(
                color = RedBear.copy(alpha = 0.12f),
                topLeft = Offset(0f, (yRes - 14f).coerceAtLeast(0f)),
                size = Size(W, 28f)
            )
            drawLine(RedBear.copy(alpha = 0.7f), Offset(0f, yRes), Offset(W, yRes), 1.5f, pathEffect = dash)

            // Support zone (guide low band)
            val ySup = py(guideLow)
            drawRect(
                color = GreenBull.copy(alpha = 0.12f),
                topLeft = Offset(0f, ySup - 14f),
                size = Size(W, 28f)
            )
            drawLine(GreenBull.copy(alpha = 0.7f), Offset(0f, ySup), Offset(W, ySup), 1.5f, pathEffect = dash)

            // Zone labels
            drawContext.canvas.nativeCanvas.apply {
                val pRes = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#F87171")
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                val pSup = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#4ADE80")
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                drawText("RESISTANCE  ${"%.2f".format(guideHigh)}", 10f, (yRes - 6f).coerceAtLeast(18f), pRes)
                drawText("SUPPORT  ${"%.2f".format(guideLow)}", 10f, (ySup + 20f).coerceAtMost(H - 6f), pSup)
            }

            val slot = W / vis.size
            val bw = (slot * 0.72f).coerceIn(3f, 28f)

            // Candles
            vis.forEachIndexed { i, cd ->
                val x = (i + 0.5f) * W / vis.size
                val bull = cd.close >= cd.open
                val color = if (bull) GreenBull else RedBear
                drawLine(color, Offset(x, py(cd.high)), Offset(x, py(cd.low)), 2.5f, cap = StrokeCap.Round)
                val yO = py(cd.open)
                val yC = py(cd.close)
                val top = minOf(yO, yC)
                val bodyH = kotlin.math.abs(yC - yO).coerceAtLeast(2.5f)
                drawRect(color, Offset(x - bw / 2, top), Size(bw, bodyH))
            }

            // Trend arrows (swing)
            if (vis.size >= 8) {
                var peakIdx = 0
                for (i in 1 until vis.size) if (vis[i].high > vis[peakIdx].high) peakIdx = i
                var troughIdx = peakIdx
                for (i in peakIdx until vis.size) if (vis[i].low < vis[troughIdx].low) troughIdx = i

                fun arrow(x1: Float, y1: Float, x2: Float, y2: Float, color: Color) {
                    drawLine(color, Offset(x1, y1), Offset(x2, y2), 2.5f, cap = StrokeCap.Round)
                    val ang = kotlin.math.atan2(y2 - y1, x2 - x1)
                    val hl = 12f
                    val path = Path().apply {
                        moveTo(x2, y2)
                        lineTo(x2 - hl * kotlin.math.cos(ang - 0.4f), y2 - hl * kotlin.math.sin(ang - 0.4f))
                        lineTo(x2 - hl * kotlin.math.cos(ang + 0.4f), y2 - hl * kotlin.math.sin(ang + 0.4f))
                        close()
                    }
                    drawPath(path, color)
                }

                if (peakIdx >= 3) {
                    arrow(0.5f * W / vis.size, py(vis[0].low), (peakIdx + 0.5f) * W / vis.size, py(vis[peakIdx].high) - 6f, GreenBull)
                }
                if (troughIdx > peakIdx + 2) {
                    arrow((peakIdx + 0.5f) * W / vis.size, py(vis[peakIdx].high) + 8f, (troughIdx + 0.5f) * W / vis.size, py(vis[troughIdx].low) - 8f, RedBear)
                }
                val last = vis.last()
                if (vis.size - 1 > troughIdx + 2 && last.close >= vis[troughIdx].low) {
                    arrow((troughIdx + 0.5f) * W / vis.size, py(vis[troughIdx].low) + 6f, (vis.size - 1 + 0.5f) * W / vis.size, py(last.close) - 6f, GreenBull)
                }
            }

            // Current price line + badge
            val last = vis.last()
            val lastY = py(last.close)
            val bull = last.close >= last.open
            val badgeColor = if (bull) GreenBull else RedBear
            drawLine(
                badgeColor.copy(alpha = 0.55f),
                Offset(0f, lastY),
                Offset(W, lastY),
                1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
            val badgeW = 110f
            val badgeX = W - badgeW - 8f
            val badgeY = (lastY - 14f).coerceIn(4f, H - 32f)
            drawRect(badgeColor, Offset(badgeX, badgeY), Size(badgeW, 28f))
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                drawText("$${ "%.2f".format(last.close) }", badgeX + 8f, badgeY + 20f, paint)
            }
        }
    }
}
