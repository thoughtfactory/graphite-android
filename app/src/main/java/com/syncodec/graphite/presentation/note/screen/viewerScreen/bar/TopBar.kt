package com.syncodec.graphite.presentation.note.screen.viewerScreen.bar

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	isOperationPending : Boolean = false,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickMenu : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				tooltip = "Back"
			) {
				if (isOperationPending) Toast.makeText(context, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
				else onClickBack()
			}
		},
		title = {},
		actions = {
			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				tooltip = "Lock",
				checked = isLocked,
				shape = MaterialTheme.shapes.medium,
				colors = MenuButtonDefaults.menuButtonColors(),
				onClick = onClickLock
			)
			MenuButton(
				icon = R.drawable.ic_favourite,
				tooltip = "Favourite",
				checked = isFavourite,
				shape = MaterialTheme.shapes.medium,
				colors = MenuButtonDefaults.menuButtonColors(),
				onClick = onClickFavourite
			)
			MenuButton(
				icon = R.drawable.ic_menu,
				tooltip = "Menu",
				onClick = onClickMenu
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
