package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isSelecting: Boolean = false,
	selectedSize: Int = 0,
	onClickBack: () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300)
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_close,
						onClick = onClickCancelSelect,
					)
				},
				title = {
					AnimatedText(
						text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "${selectedSize ?: "No"} items selected",
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				actions = {
					MenuButton(
						icon = R.drawable.ic_delete,
						colors = MenuButtonDefaults.deleteButtonColors(),
						onClick = onClickDelete
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				),
			)
		} else {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_back,
						onClick = onClickBack,
					)
				},
				title = {
					Text(
						text = "Attachment",
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				},
				actions = {},
				modifier = Modifier.fillMaxWidth(),
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		}
	}
}
