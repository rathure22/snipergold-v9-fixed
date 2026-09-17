package com.snipergold.app.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Auto-adjust breakpoints for phone / tablet / wide.
 * Phone  < 600dp
 * Tablet >= 600dp
 * Wide   >= 840dp
 */
enum class DeviceClass { PHONE, TABLET, WIDE }

data class AdaptiveMetrics(
    val device: DeviceClass,
    val isWide: Boolean,
    val contentPadding: Dp,
    val cardGap: Dp,
    val chartHeight: Dp,
    val sidebarWidth: Dp,
    val priceSize: Int,
    val titleSize: Int,
    val bodySize: Int,
    val maxContentWidth: Dp
)

val LocalAdaptive = compositionLocalOf {
    AdaptiveMetrics(
        device = DeviceClass.PHONE,
        isWide = false,
        contentPadding = 12.dp,
        cardGap = 8.dp,
        chartHeight = 220.dp,
        sidebarWidth = 300.dp,
        priceSize = 28,
        titleSize = 11,
        bodySize = 12,
        maxContentWidth = 480.dp
    )
}

@Composable
fun AdaptiveShell(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val metrics = when {
            w >= 840.dp -> AdaptiveMetrics(
                device = DeviceClass.WIDE,
                isWide = true,
                contentPadding = 20.dp,
                cardGap = 12.dp,
                chartHeight = 340.dp,
                sidebarWidth = 340.dp,
                priceSize = 36,
                titleSize = 12,
                bodySize = 13,
                maxContentWidth = 1100.dp
            )
            w >= 600.dp -> AdaptiveMetrics(
                device = DeviceClass.TABLET,
                isWide = true,
                contentPadding = 16.dp,
                cardGap = 10.dp,
                chartHeight = 300.dp,
                sidebarWidth = 320.dp,
                priceSize = 32,
                titleSize = 12,
                bodySize = 13,
                maxContentWidth = 900.dp
            )
            else -> AdaptiveMetrics(
                device = DeviceClass.PHONE,
                isWide = false,
                contentPadding = 12.dp,
                cardGap = 8.dp,
                chartHeight = 220.dp,
                sidebarWidth = 300.dp,
                priceSize = 28,
                titleSize = 11,
                bodySize = 12,
                maxContentWidth = 520.dp
            )
        }
        CompositionLocalProvider(LocalAdaptive provides metrics) {
            content()
        }
    }
}
