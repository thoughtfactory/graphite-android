package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.SearchBar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun TopBar(
	title : String = "Calendar",
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	onClickCancelSelect : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {

	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300)
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_close,
						onClick = onClickCancelSelect
					)
				},
				title = {
					AnimatedText(
						text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "$selectedSize items selected",
						color = MaterialTheme.colorScheme.onBackground
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
					titleContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		} else {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_back,
						onClick = { backPressedDispatcher?.onBackPressed() }
					)
				},
				title = {
					Text(
						text = title,
						fontWeight = FontWeight.Bold,
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					titleContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		}
	}
}
