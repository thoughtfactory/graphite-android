package com.syncodec.momento.bucketComponent.miscellaneous

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronLeft

@Composable
fun BucketTopBar() {
	val activity = LocalContext.current as? Activity

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.padding(8.dp)
			.zIndex(1f)
	) {
		IconButton(
			onClick = { activity?.finish() },
		) {
			Icon(
				imageVector = TablerIcons.ChevronLeft,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
			)
		}

	}
}
