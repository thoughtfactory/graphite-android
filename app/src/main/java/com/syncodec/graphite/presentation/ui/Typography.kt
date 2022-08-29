package com.syncodec.graphite.presentation.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


private val UbuntuFontFamily = FontFamily(
	Font(R.font.ubuntu_regular, FontWeight.Normal),
)

val UbuntuTypography = Typography(
	displayLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Light,
		fontSize = 57.sp,
		lineHeight = 60.sp,
		letterSpacing = 1.sp
	),
	displayMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Light,
		fontSize = 45.sp,
		lineHeight = 48.sp,
		letterSpacing = 1.sp
	),
	displaySmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 36.sp,
		lineHeight = 40.sp,
		letterSpacing = 1.sp
	),
	headlineLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 32.sp,
		lineHeight = 36.sp,
		letterSpacing = 1.sp
	),
	headlineMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 28.sp,
		lineHeight = 30.sp,
		letterSpacing = 1.sp
	),
	headlineSmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 24.sp,
		lineHeight = 26.sp,
		letterSpacing = 1.sp
	),
	titleLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 22.sp,
		lineHeight = 28.sp,
		letterSpacing = 1.sp
	),
	titleMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 16.sp,
		lineHeight = 24.sp,
		letterSpacing = 1.sp
	),
	titleSmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 14.sp,
		lineHeight = 16.sp,
		letterSpacing = 1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 18.sp,
		letterSpacing = 0.8.sp
	),
	bodyMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 14.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.4.sp
	),
	bodySmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 14.sp,
		letterSpacing = 0.2.sp
	),
	labelLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 11.sp,
		lineHeight = 12.sp,
		letterSpacing = 0.2.sp
	),
	labelMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 10.sp,
		lineHeight = 12.sp,
		letterSpacing = 0.2.sp
	),
	labelSmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 8.sp,
		lineHeight = 10.sp,
		letterSpacing = 0.2.sp
	)
)
