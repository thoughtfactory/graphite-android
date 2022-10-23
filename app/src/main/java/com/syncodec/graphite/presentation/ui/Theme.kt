package com.syncodec.graphite.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor


val lightColorScheme0 = lightColorScheme(
	primary = Color(0xFF1E2022),
	onPrimary = Color(0xFFC0CEDD),
	primaryContainer = Color(0xFF0F1316),
	onPrimaryContainer = Color(0xFF9BAEC3),
	secondary = Color(0xFF242D34),
	onSecondary = Color(0xFFA2A9AE),
	secondaryContainer = Color(0xFF1C3648),
	onSecondaryContainer = Color(0xFFCED8DF),
	surface = Color(0xFFC9D6DF),
	onSurface = Color(0xFF1E2A34),
	background = Color(0xFFECF1F4),
	onBackground = Color(0xFF02060A)
)

val darkColorScheme0 = darkColorScheme(
	primary = Color(0xFFC0CEDD),
	onPrimary = Color(0xFF1E2022),
	primaryContainer = Color(0xFF9BAEC3),
	onPrimaryContainer = Color(0xFF0F1316),
	secondary = Color(0xFFA2A9AE),
	onSecondary = Color(0xFF242D34),
	secondaryContainer = Color(0xFFCED8DF),
	onSecondaryContainer = Color(0xFF1C3648),
	surface = Color(0xFF1E2A34),
	onSurface = Color(0xFFC9D6DF),
	background = Color(0xFF02060A),
	onBackground = Color(0xFFCEDBE6)
)

val Color.Companion.DeleteContainer: Color
	get() = Color(0xFFE94560)
val Color.Companion.DeleteContent: Color
	get() = Color.White

val Color.Companion.FavouriteContainer: Color
	get() = Color(0x47E78EA9)
val Color.Companion.FavouriteContent: Color
	get() = Color.White

val Color.Companion.LockOpenContainer:Color
	get() = Color(0xFFE94560)
val Color.Companion.LockOpenContent:Color
	get() = Color(0xFFE94560).getInverseBWColor()

val Color.Companion.LockClosedContainer:Color
	get() = Color(0xFF76BA99)
val Color.Companion.LockClosedContent:Color
	get() = Color.White


val dialogPadding = PaddingValues(24.dp, 24.dp, 24.dp, 12.dp)
