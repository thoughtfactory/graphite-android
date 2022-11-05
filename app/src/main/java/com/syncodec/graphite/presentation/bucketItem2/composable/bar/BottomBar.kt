package com.syncodec.graphite.presentation.bucketItem2.composable.bar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar(
	onClickShare : () -> Unit,
	onClickDelete : () -> Unit,
	onClickMove : () -> Unit,
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
			contentDescription = "Share",
			tint = contentColor,
			onClick = onClickShare
		)

		MenuButton(
			icon = R.drawable.ic_delete,
			contentDescription = "Delete",
			tint = Color.DeleteContainer,
			onClick = onClickDelete
		)

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_folder,
			contentDescription = "Move to",
			tint = contentColor,
			onClick = onClickMove
		)

		Spacer(modifier = Modifier.width(16.dp))
	}
}
