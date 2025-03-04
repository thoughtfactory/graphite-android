package com.syncodec.graphite.presentation.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
    surface = Color(0xFFEEEEEE),
    onSurface = Color(0xFF131313),
    surfaceVariant = Color(0xFFDEDEDE),
    onSurfaceVariant = Color(0xFF131313),
//	background = Color(0xFFECF1F4),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF02060A)
)

val darkColorScheme0 = darkColorScheme(
    primary = Color(color = 0xFFCDC9C3),
    onPrimary = Color(color = 0xFF131313),
    primaryContainer = Color(color = 0xFFCDC9C3),
    onPrimaryContainer = Color(color = 0xFF131313),
    secondary = Color(color = 0xFFA2A9AE),
    onSecondary = Color(color = 0xFF242D34),
    secondaryContainer = Color(color = 0xFFCED8DF),
    onSecondaryContainer = Color(color = 0xFF1C3648),
    surface = Color(color = 0xFF131313),
    onSurface = Color(color = 0xFFCDC9C3),
    surfaceVariant = Color(color = 0xFF313131),
    onSurfaceVariant = Color(color = 0xFFCDC9C3),
    background = Color(color = 0xFF000000),
    onBackground = Color(color = 0xFFCDC9C3),
    errorContainer = Color(color = 0xFF561C24),
    onErrorContainer = Color(color = 0xFFCDC9C3)
)


val Color.Companion.DeleteContainer: Color
    get() = Color(0xFFE94560)
val Color.Companion.DeleteContent: Color
    get() = Color.White

val Color.Companion.FavouriteContainer: Color
    get() = Color(0xFFEF5A6F)
val Color.Companion.FavouriteContent: Color
    get() = Color.Black

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

val ANIMATION_TIME = 470


object AnimationDefaults {

    val ANIMATION_TIME = 470

    fun <T> stateAnimationSpec() = tween<T>(durationMillis = ANIMATION_TIME)

    val ExpandVerticallyEnter = expandVertically(animationSpec = stateAnimationSpec())
    val ShrinkVerticallyExit = shrinkVertically(animationSpec = stateAnimationSpec())

    val ExpandHorizontallyEnter = expandHorizontally(animationSpec = stateAnimationSpec())
    val ShrinkHorizontallyExit = shrinkHorizontally(animationSpec = stateAnimationSpec())

    val FadeEnter = fadeIn(animationSpec = stateAnimationSpec())
    fun fadeEnter(alpha: Float) = fadeIn(animationSpec = stateAnimationSpec(), initialAlpha = alpha)
    val FadeExit = fadeOut(animationSpec = stateAnimationSpec())
    fun fadeExit(alpha: Float) = fadeOut(animationSpec = stateAnimationSpec(), targetAlpha = alpha)

    val ScaleEnter = scaleIn(animationSpec = stateAnimationSpec())
    fun scaleEnter(scale: Float) = scaleIn(animationSpec = stateAnimationSpec(), initialScale = scale)
    val ScaleExit = scaleOut(animationSpec = stateAnimationSpec())
    fun scaleExit(scale: Float) = scaleOut(animationSpec = stateAnimationSpec(), targetScale = scale)

    val ScaleAndFadeEnter = ScaleEnter + FadeEnter
    fun scaleAndFadeEnter(scale: Float, alpha: Float) = scaleEnter(scale = scale) + fadeEnter(alpha = alpha)
    val ScaleAndFadeExit = ScaleExit + FadeExit
    fun scaleAndFadeExit(scale: Float, alpha: Float) = scaleExit(scale = scale) + fadeExit(alpha = alpha)

    val Fade = FadeEnter togetherWith FadeExit
    fun fade(alpha: Float) = fadeEnter(alpha = alpha) togetherWith fadeExit(alpha = alpha)

    val Scale = ScaleEnter togetherWith ScaleExit
    fun scale(scale: Float) = scaleEnter(scale = scale) togetherWith scaleExit(scale = scale)

    val ScaleAndFade = ScaleAndFadeEnter togetherWith ScaleAndFadeExit
    fun scaleAndFade(scale: Float, alpha: Float) = scaleAndFadeEnter(scale = scale, alpha = alpha) togetherWith scaleAndFadeExit(scale = scale, alpha = alpha)

    val ExpandAndShrink = ExpandVerticallyEnter togetherWith ShrinkVerticallyExit

}

object ColorDefaults {
    val Color.Companion.Favourite: Color
        get() = Color(color = 0xFFFF8383)
    val Color.Companion.OnFavourite: Color
        get() = Color.White

    val Color.Companion.Lock: Color
        get() = Color(color = 0xFF6A9C89)
    val Color.Companion.OnLock: Color
        get() = Color.White

}
