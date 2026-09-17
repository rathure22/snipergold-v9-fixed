package com.snipergold.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// 100% LUWAS - gamit built-in system fonts, walay Downloadable Fonts, walay certificate
// Serif = Luxury Serif (Playfair Display style), SansSerif = Modern Pro Sans (Inter style)
val LuxurySerif = FontFamily.Serif
val ModernSans = FontFamily.SansSerif

val SniperTypography = Typography(
    // Branding - Luxury Serif
    displayLarge = TextStyle(
        fontFamily = LuxurySerif,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp
    ),
    // Price - Monospace para stable ang live ticker (dili mag-shake ang numbers)
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    // Body - Modern Sans
    bodyLarge = TextStyle(
        fontFamily = ModernSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ModernSans,
        fontSize = 10.sp
    )
)
