package com.syncodec.momento.bucketItemComponent.miscellaneous

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import compose.icons.TablerIcons
import compose.icons.tablericons.Pencil


@Composable
fun ThumbnailCard(
	thumbnail: Any?
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.width(screenWidth * 0.5f)
			.aspectRatio(0.75f)
			.padding(0.dp),
	) {
		Image(
			painter = rememberImagePainter(
				data = thumbnail,
				builder = { crossfade(true) }
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	}
}
