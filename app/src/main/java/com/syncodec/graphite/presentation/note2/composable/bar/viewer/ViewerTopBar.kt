package com.syncodec.graphite.presentation.note2.composable.bar.viewer

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.ui.IconButtonSize


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ViewerTopBar(
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	onClickLocalOnly: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickPin: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickBack: () -> Unit = {},
) {
	val context = LocalContext.current
	val isPro by BaseApplication.isPro.collectAsState()

	var isMenuDropdownVisible by remember { mutableStateOf(false) }

	TopAppBar(
		navigationIcon = {
			GenericButton(
				icon = R.drawable.ic_fa_back,
				tooltip = "Back",
				onClick = onClickBack
			)
		},
		title = {},
		actions = {
			LockButton(
				isLocked = isLocked,
				onClick = onClickLock,
			)
			FavouriteButton(
				isFavourite = isFavourite,
				onClick = onClickFavourite,
			)
			Box {
				GenericButton(
					icon = R.drawable.ic_fa_menu,
					tooltip = "Menu",
					onClick = { isMenuDropdownVisible = true },
				)
				DropdownMenu(
					expanded = isMenuDropdownVisible,
					onDismissRequest = { isMenuDropdownVisible = false }
				) {
					DropdownMenuItem(
						leadingIcon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_pin),
								contentDescription = "Pin to notification",
								modifier = Modifier.requiredSize(IconButtonSize),
							)
						},
						text = { Text(text = "Pin to notification") },
						trailingIcon = if (!isPro) {
							{ Icon(painter = painterResource(id = R.drawable.ic_fa_pro), contentDescription = "Pro feature", modifier = Modifier.requiredSize(IconButtonSize)) }
						} else null,
						onClick = {
							if (isPro) {
								isMenuDropdownVisible = false; onClickPin()
							} else Toast.makeText(context, "Join Graphite Pro to access this feature.", Toast.LENGTH_SHORT).show()
						},
					)
					DropdownMenuItem(
						leadingIcon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_trash),
								contentDescription = "Delete",
								tint = MaterialTheme.colorScheme.error,
								modifier = Modifier.requiredSize(IconButtonSize)
							)
						},
						text = { Text(text = "Delete", color = MaterialTheme.colorScheme.error) },
						onClick = { /*TODO*/ },
					)
				}
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
