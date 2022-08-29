package com.syncodec.graphite.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.jorgecastillo.androidcolorx.library.shades
import dev.jorgecastillo.androidcolorx.library.tints
import kotlin.random.Random


fun Color.tone(isDarkTheme: Boolean, hue: Int) = if(hue > 0) {
	if (isDarkTheme) Color(this.toArgb().tints()[hue]) else Color(this.toArgb().shades()[hue])
} else {
	if (!isDarkTheme) Color(this.toArgb().tints()[-hue]) else Color(this.toArgb().shades()[-hue])
}

fun Color.toHexString(): String {
	return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}

fun getRandomColor(): Color {
	return Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256), 255)
}
