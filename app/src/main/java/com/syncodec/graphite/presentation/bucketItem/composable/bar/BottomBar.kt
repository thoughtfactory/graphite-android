package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar(
	onClickShare : () -> Unit,
	onClickDelete : () -> Unit,
	onClickAddReminder : () -> Unit,
) {
	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(containerColor)
	) {
		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_share,
			tooltip = "Share",
			colors = MenuButtonDefaults.menuButtonColors(
				containerColor = containerColor,
				iconColor = contentColor,
			),
			onClick = onClickShare
		)

		MenuButton(
			icon = R.drawable.ic_delete,
			tooltip = "Delete",
			colors = MenuButtonDefaults.menuButtonColors(
				containerColor = containerColor,
				iconColor = MaterialTheme.colorScheme.error,
			),
			onClick = onClickDelete
		)

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_clock,
			tooltip = "Add reminder",
			onClick = onClickAddReminder
		)

		Spacer(modifier = Modifier.width(16.dp))
	}
}
