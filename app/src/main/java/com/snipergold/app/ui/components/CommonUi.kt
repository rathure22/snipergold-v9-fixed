package com.snipergold.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snipergold.app.ui.theme.CardBg
import com.snipergold.app.ui.theme.Gold
import com.snipergold.app.ui.theme.GreenBull
import com.snipergold.app.ui.theme.PanelBg
import com.snipergold.app.ui.theme.RedBear
import com.snipergold.app.ui.theme.Zinc400
import com.snipergold.app.ui.theme.Zinc500
import com.snipergold.app.ui.theme.Zinc600
import com.snipergold.app.ui.theme.Zinc800
import com.snipergold.app.ui.theme.Zinc900

@Composable
fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Int = 11,
    weight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = weight
    )
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PanelBg)
            .border(1.dp, Zinc800, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        MonoText(title, color = Zinc500, size = 10, weight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
fun StatusDot(active: Boolean, color: Color = GreenBull) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (active) color else Zinc600)
    )
}

@Composable
fun PatternRow(label: String, value: String, found: Boolean, bullish: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonoText(label, color = Zinc400, size = 11)
        MonoText(
            value,
            color = when {
                found && bullish -> GreenBull
                found && !bullish -> RedBear
                else -> Zinc600
            },
            size = 11,
            weight = if (found) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SessionChip(name: String, time: String, isOpen: Boolean) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black)
            .border(1.dp, Zinc800, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText(name, color = Zinc500, size = 9)
        Spacer(Modifier.height(4.dp))
        MonoText(time, color = Color.White, size = 12, weight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        MonoText(
            if (isOpen) "● OPEN" else "closed",
            color = if (isOpen) GreenBull else Zinc600,
            size = 9,
            weight = if (isOpen) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SignalBanner(
    signal: String,
    price: Double,
    reason: String,
    guideHigh: Double,
    guideLow: Double
) {
    val bg = when (signal) {
        "BUY" -> GreenBull.copy(alpha = 0.12f)
        "SELL" -> RedBear.copy(alpha = 0.12f)
        else -> Color.Black
    }
    val border = when (signal) {
        "BUY" -> GreenBull
        "SELL" -> RedBear
        else -> Zinc800
    }
    val textColor = when (signal) {
        "BUY" -> GreenBull
        "SELL" -> RedBear
        else -> Zinc600
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonoText("BUY/SELL BASE RA SA PATTERN + GUIDE LUSOT", color = Zinc500, size = 9)
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (signal) {
                "BUY" -> "BUY SIGNAL"
                "SELL" -> "SELL SIGNAL"
                else -> "WAIT"
            },
            color = textColor,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(6.dp))
        MonoText(
            if (signal == "WAIT") "Nag hulat pattern + guide lusot • $${"%.2f".format(price)}"
            else "PATTERN + LUSOT GUIDE • $${"%.2f".format(price)}",
            color = Zinc400,
            size = 11
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Zinc900.copy(alpha = 0.6f))
                .padding(12.dp)
        ) {
            MonoText(
                when (signal) {
                    "BUY", "SELL" -> "✅ $reason\nEntry: break guide\nSL: ${if (signal == "BUY") "%.2f".format(guideLow) else "%.2f".format(guideHigh)}"
                    else -> "⏳ WAIT - Guide High: ${"%.2f".format(guideHigh)}\nGuide Low: ${"%.2f".format(guideLow)}\nCurrent: ${"%.2f".format(price)}"
                },
                color = Zinc400,
                size = 10
            )
        }
    }
}
