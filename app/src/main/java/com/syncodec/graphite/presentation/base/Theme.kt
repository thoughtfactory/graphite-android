package com.syncodec.graphite.presentation.base

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor


val lightColorScheme0 = lightColorScheme(
    primary = Color(0xFF1E2022),
    onPrimary = Color(0xFFDEE2E7),
    primaryContainer = Color(0xFF0F1316),
    onPrimaryContainer = Color(0xFFEBEBEB),
    secondary = Color(0xFF242D34),
    onSecondary = Color(0xFFA2A9AE),
    secondaryContainer = Color(0xFF1C3648),
    onSecondaryContainer = Color(0xFFCED8DF),
    surface = Color(0xFFE4E4E4),
    onSurface = Color(0xFF1E2A34),
//	background = Color(0xFFECF1F4),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF02060A),
    error = Color(0xFFF94C10),
    onError = Color.White,
    errorContainer = Color(0xFFFAD4D4),
    onErrorContainer = Color.Black,
)

val darkColorScheme0 = darkColorScheme(
    primary = Color(0xFFEFEFEF),
    onPrimary = Color(0xFF1E2022),
    primaryContainer = Color(0xFFE3E3E3),
    onPrimaryContainer = Color(0xFF0F1316),
    secondary = Color(0xFFA2A9AE),
    onSecondary = Color(0xFF242D34),
    secondaryContainer = Color(0xFFCED8DF),
    onSecondaryContainer = Color(0xFF1C3648),
    surface = Color(0xFF1E2A34),
    onSurface = Color(0xFFC9D6DF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFCEDBE6)
)


val Color.Companion.DeleteContainer: Color
    get() = Color(0xFFE94560)
val Color.Companion.DeleteContent: Color
    get() = Color.White

val Color.Companion.FavouriteContainer: Color
    get() = Color(0xFFE53F8B)
val Color.Companion.FavouriteContent: Color
    get() = Color.White

val Color.Companion.LockOpenContainer: Color
    get() = Color(0xFFE94560)
val Color.Companion.LockOpenContent: Color
    get() = Color(0xFFE94560).getInverseBWColor()

val Color.Companion.LockClosedContainer: Color
    get() = Color(0xFF76BA99)
val Color.Companion.LockClosedContent: Color
    get() = Color.White

val Color.Companion.AttachmentContainer: Color
    get() = Color(0xFFF5B971)

val Color.Companion.AttachmentContent: Color
    get() = Color.Black

val Color.Companion.LocationContainer: Color
    get() = Color(0xFF318DFD)

val Color.Companion.LocationContent: Color
    get() = Color.White

val Color.Companion.PositiveContainer: Color
    get() = Color(0xFF76BA99)

val Color.Companion.PositiveContent: Color
    get() = Color.White

val ICON_SIZE = 20.dp


const val ANIMATION_DURATION_MILLIS = 470
const val ANIMATION_DURATION_MILLIS_FAST = 170

val SCALE_AND_FADE_TRANSFORM: ContentTransform =
    fadeIn(animationSpec = tween(ANIMATION_DURATION_MILLIS)) + scaleIn(animationSpec = tween(ANIMATION_DURATION_MILLIS), initialScale = 0.71f) togetherWith
            fadeOut(animationSpec = tween(ANIMATION_DURATION_MILLIS)) + scaleOut(animationSpec = tween(ANIMATION_DURATION_MILLIS), targetScale = 0.71f)

val SCALE_AND_FADE_ENTER: EnterTransition = scaleIn(animationSpec = tween(ANIMATION_DURATION_MILLIS), initialScale = 0.71f) + fadeIn(tween(ANIMATION_DURATION_MILLIS))
val SCALE_AND_FADE_EXIT: ExitTransition = scaleOut(animationSpec = tween(ANIMATION_DURATION_MILLIS), targetScale = 0.71f) + fadeOut(tween(ANIMATION_DURATION_MILLIS))

val SCALE_AND_FADE_ENTER_FAST: EnterTransition = scaleIn(animationSpec = tween(ANIMATION_DURATION_MILLIS_FAST), initialScale = 0.71f) + fadeIn(tween(ANIMATION_DURATION_MILLIS_FAST))
val SCALE_AND_FADE_EXIT_FAST: ExitTransition = scaleOut(animationSpec = tween(ANIMATION_DURATION_MILLIS_FAST), targetScale = 0.71f) + fadeOut(tween(ANIMATION_DURATION_MILLIS_FAST))

