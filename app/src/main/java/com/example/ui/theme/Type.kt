package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OrbitronFontFamily = FontFamily(
    Font(R.font.orbitron, FontWeight.Normal),
    Font(R.font.orbitron, FontWeight.Bold)
)

val RajdhaniFontFamily = FontFamily(
    Font(R.font.rajdhani, FontWeight.Normal),
    Font(R.font.rajdhani, FontWeight.Medium),
    Font(R.font.rajdhani, FontWeight.SemiBold),
    Font(R.font.rajdhani, FontWeight.Bold)
)

val SipraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 1.5.sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 1.2.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.0.sp,
        color = CyanNeon
    ),
    titleMedium = TextStyle(
        fontFamily = RajdhaniFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.8.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = RajdhaniFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = RajdhaniFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.0.sp,
        color = CyanBright
    ),
    labelSmall = TextStyle(
        fontFamily = RajdhaniFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
        color = TextMuted
    )
)
