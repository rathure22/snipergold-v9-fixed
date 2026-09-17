package com.snipergold.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// v5.8 palette — slightly softened for long-session eye comfort
val Gold = Color(0xFFD4B87A)
val GoldBright = Color(0xFFE8C97A)
val DarkBg = Color(0xFF0C0B0A)       // not pure black — less eye strain
val CardBg = Color(0xFF12110F)
val PanelBg = Color(0xFF161412)
val GreenBull = Color(0xFF00D084)
val GreenSoft = Color(0x5500D084)
val RedBear = Color(0xFFE85A5A)      // slightly softer red
val OrangeAccent = Color(0xFFFF7A1A)
val BlueDual = Color(0xFF5BA8FF)
val YellowBoth = Color(0xFFFFB020)
val Zinc400 = Color(0xFFB0B0B8)
val Zinc500 = Color(0xFF8A8A94)
val Zinc600 = Color(0xFF5C5C66)
val Zinc800 = Color(0xFF2A2A30)
val Zinc900 = Color(0xFF1A1A1E)
val CardStroke = Color(0xFF2A2830)

enum class AccentTheme { GREEN, ORANGE }

val LocalAccent = compositionLocalOf { AccentTheme.GREEN }

fun accentColor(theme: AccentTheme): Color =
    if (theme == AccentTheme.GREEN) GreenBull else OrangeAccent

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = GreenBull,
    tertiary = RedBear,
    background = DarkBg,
    surface = CardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF2F0EB),
    onSurface = Color(0xFFF2F0EB)
)

/** Readable type scale — less harsh than pure white 100% */
private val SgTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = Gold
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = Zinc500
    )
)

@Composable
fun SniperGoldTheme(
    accentTheme: AccentTheme = AccentTheme.GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = DarkColorScheme.copy(primary = accentColor(accentTheme))
    CompositionLocalProvider(LocalAccent provides accentTheme) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SgTypography,
            content = content
        )
    }
}
