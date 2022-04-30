@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.syncodec.graphite.custom.richText.viewer

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State

/**
 * Scope object for composables that can draw rich text.
 *
 * RichTextScope facilitates a context for RichText elements. It does not
 * behave like a [State] or a [CompositionLocal]. Starting from [RichText],
 * this scope carries information that should not be passed down as a state.
 */
@Immutable
object RichTextScope
