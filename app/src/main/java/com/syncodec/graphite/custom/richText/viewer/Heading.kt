@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.syncodec.graphite.custom.richText.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.resolveDefaults
import androidx.compose.ui.unit.sp

/**
 * Function that computes the [TextStyle] for the given header level, given the current [TextStyle]
 * for this point in the composition. Note that the [TextStyle] passed into this function will be
 * fully resolved. The returned style will then be _merged_ with the passed-in text style, so any
 * unspecified properties will be inherited.
 */
// TODO factor a generic "block style" thing out, use for code block, quote block, and this, to
// also allow controlling top/bottom space.
public typealias HeadingStyle = (level: Int, textStyle: TextStyle) -> TextStyle

internal val DefaultHeadingStyle: HeadingStyle = { level, textStyle ->
	when (level) {
		1 -> TextStyle(
			fontSize = 45.sp,
			lineHeight = 45.sp,
			fontWeight = FontWeight.Bold
		)
		2 -> TextStyle(
			fontSize = 37.sp,
			lineHeight = 37.sp,
			fontWeight = FontWeight.Bold
		)
		3 -> TextStyle(
			fontSize = 29.sp,
			lineHeight = 29.sp,
			fontWeight = FontWeight.Bold
		)
		4 -> TextStyle(
			fontSize = 25.sp,
			lineHeight = 25.sp,
			fontWeight = FontWeight.Bold
		)
		5 -> TextStyle(
			fontSize = 21.sp,
			lineHeight = 21.sp,
			fontWeight = FontWeight.Bold
		)
		6 -> TextStyle(
			fontSize = 17.sp,
			lineHeight = 17.sp,
			fontWeight = FontWeight.Bold
		)
		else -> textStyle
	}
}

/**
 * A section heading.
 *
 * @param level The non-negative rank of the header, with 0 being the most important.
 */
@Composable
fun RichTextScope.Heading(
	level: Int,
	text: String
) {
	Heading(level) {
		Text(text)
	}
}

/**
 * A section heading.
 *
 * @param level The non-negative rank of the header, with 0 being the most important.
 */
@Composable
fun RichTextScope.Heading(
	level: Int,
	children: @Composable RichTextScope.() -> Unit
) {
	require(level >= 0) { "Level must be at least 0" }

	val incomingStyle = currentTextStyle.let {
		it.copy(color = it.color.takeOrElse { currentContentColor })
	}
	val currentTextStyle = resolveDefaults(incomingStyle, LocalLayoutDirection.current)

	val headingStyleFunction = currentRichTextStyle.resolveDefaults().headingStyle!!
	val headingTextStyle = headingStyleFunction(level, currentTextStyle)
	val mergedTextStyle = currentTextStyle.merge(headingTextStyle)

	ProvideTextStyle(mergedTextStyle) {
		children()
	}
}
