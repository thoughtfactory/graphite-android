package com.syncodec.graphite.presentation.note.screen.viewerScreen.bar

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenu
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItem


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	isOperationPending : Boolean = false,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickPin : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current

	var isMenuDropdownVisible by remember { mutableStateOf(false) }

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
				colors = MenuButtonDefaults.menuButtonColors(),
				onClick = onClickLock
			)
			MenuButton(
				icon = R.drawable.ic_favourite,
				tooltip = "Favourite",
				checked = isFavourite,
				colors = MenuButtonDefaults.menuButtonColors(),
				onClick = onClickFavourite
			)
			Box {
				MenuButton(
					icon = R.drawable.ic_menu,
					tooltip = "Menu",
					onClick = { isMenuDropdownVisible = true },
				)
				DropdownMenu(
					itemList = listOf(
						DropdownMenuItem(
							title = "Pin to notification",
							icon = R.drawable.ic_pin,
							isPro = true,
						) {
							if (BaseApplication.isPro.value) {
								isMenuDropdownVisible = false
								onClickPin()
							} else Toast.makeText(context, "Join Graphite Pro to access this feature.", Toast.LENGTH_SHORT).show()
						},
						DropdownMenuItem(title = "Delete", icon = R.drawable.ic_delete, iconColor = MaterialTheme.colorScheme.error) {
							isMenuDropdownVisible = false; onClickDelete()
						},
					),
					isVisible = isMenuDropdownVisible,
				) { isMenuDropdownVisible = false }
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
