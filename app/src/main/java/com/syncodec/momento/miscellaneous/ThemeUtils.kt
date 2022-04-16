package com.syncodec.momento.miscellaneous

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.syncodec.momento.R
import dev.jorgecastillo.androidcolorx.library.shades
import dev.jorgecastillo.androidcolorx.library.tints

class ThemeUtils {
	companion object {
		fun Color.tone(isDarkTheme: Boolean, hue: Int) = if (isDarkTheme) Color(this.toArgb().tints()[hue]) else Color(this.toArgb().shades()[hue])

		val bookCoverImageList: List<Int> = listOf(
			R.drawable.book_cover_1,
			R.drawable.background_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
			R.drawable.book_cover_1,
		)
	}
}
