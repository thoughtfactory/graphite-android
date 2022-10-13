package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar(
	modifier: Modifier,
	openSheet: (BucketBottomSheetType) -> Unit
) {
	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(containerColor)
	) {
		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_share,
			contentDescription = "Share Bucket",
			tint = contentColor
		) {}

		MenuButton(
			icon = R.drawable.ic_export,
			contentDescription = "Export Bucket",
			tint = contentColor
		) {}

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_menu,
			contentDescription = "Menu",
			tint = contentColor
		) {
			openSheet(BucketBottomSheetType.MENU)
		}

		Spacer(modifier = Modifier.width(16.dp))
	}
}
