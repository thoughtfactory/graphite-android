package com.syncodec.graphite.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.jorgecastillo.androidcolorx.library.shades
import dev.jorgecastillo.androidcolorx.library.tints
import kotlin.random.Random


fun Color.tone(isDarkTheme: Boolean, hue: Int) = if (hue > 0) {
	if (isDarkTheme) Color(this.toArgb().tints()[hue]) else Color(this.toArgb().shades()[hue])
} else {
	if (!isDarkTheme) Color(this.toArgb().tints()[-hue]) else Color(this.toArgb().shades()[-hue])
}

fun Color.toHexString(): String {
	return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}

fun String.toColor(fallbackColor: Color? = null): Color? {
	return try {
		Color(android.graphics.Color.parseColor("#${this}"))
	} catch (e: Exception) {
//		e.printStackTrace()
		fallbackColor
	}
}

fun getRandomColor(): Color {
	return Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 255)
}

fun Color.Companion.random() : Color {
	return Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 255)
}
