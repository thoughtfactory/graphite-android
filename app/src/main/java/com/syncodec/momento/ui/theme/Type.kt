package com.syncodec.momento.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.syncodec.momento.R


private val OverlockFontFamily = FontFamily(
	Font(R.font.overlock_regular, FontWeight.Normal),
	Font(R.font.overlock_bold, FontWeight.Bold),
)

private val SourceSansProFontFamily = FontFamily(
	Font(R.font.source_sans_pro_regular, FontWeight.Normal),
	Font(R.font.source_sans_pro_bold, FontWeight.Bold),
)

private val UbuntuFontFamily = FontFamily(
	Font(R.font.ubuntu_regular, FontWeight.Normal),
	Font(R.font.ubuntu_bold, FontWeight.Bold),
)

private val ATWriter = FontFamily(
	Font(R.font.atwriter)
)

val OverlockTypography = Typography(
	titleMedium = TextStyle(
		fontFamily = OverlockFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp,
		lineHeight = 27.sp,
		letterSpacing = 1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = OverlockFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 18.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.8.sp
	),
	bodyMedium = TextStyle(
		fontFamily = OverlockFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 20.sp,
		letterSpacing = 0.4.sp
	),
	bodySmall = TextStyle(
		fontFamily = OverlockFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.2.sp
	)
)

val SourceSansProTypography = Typography(
	titleMedium = TextStyle(
		fontFamily = SourceSansProFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp,
		lineHeight = 27.sp,
		letterSpacing = 1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = SourceSansProFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 18.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.8.sp
	),
	bodyMedium = TextStyle(
		fontFamily = SourceSansProFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 20.sp,
		letterSpacing = 0.4.sp
	),
	bodySmall = TextStyle(
		fontFamily = SourceSansProFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.2.sp
	)
)

val UbuntuTypography = Typography(
	titleMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp,
		lineHeight = 27.sp,
		letterSpacing = 1.sp
	),
	titleSmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 20.sp,
		lineHeight = 24.sp,
		letterSpacing = 1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 18.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.8.sp
	),
	bodyMedium = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 14.sp,
		lineHeight = 18.sp,
		letterSpacing = 0.4.sp
	),
	bodySmall = TextStyle(
		fontFamily = UbuntuFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 16.sp,
		letterSpacing = 0.2.sp
	)
)

val ATWriterTypography = Typography(
	titleMedium = TextStyle(
		fontFamily = ATWriter,
		fontWeight = FontWeight.Bold,
		fontSize = 24.sp,
		lineHeight = 27.sp,
		letterSpacing = 1.sp
	),
	titleSmall = TextStyle(
		fontFamily = ATWriter,
		fontWeight = FontWeight.Bold,
		fontSize = 20.sp,
		lineHeight = 24.sp,
		letterSpacing = 1.sp
	),
	bodyLarge = TextStyle(
		fontFamily = ATWriter,
		fontWeight = FontWeight.Bold,
		fontSize = 18.sp,
		lineHeight = 24.sp,
		letterSpacing = 1.4.sp
	),
	bodyMedium = TextStyle(
		fontFamily = ATWriter,
		fontWeight = FontWeight.Normal,
		fontSize = 14.sp,
		lineHeight = 18.sp,
		letterSpacing = 1.4.sp
	),
	bodySmall = TextStyle(
		fontFamily = ATWriter,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 16.sp,
		letterSpacing = 1.4.sp
	)
)
