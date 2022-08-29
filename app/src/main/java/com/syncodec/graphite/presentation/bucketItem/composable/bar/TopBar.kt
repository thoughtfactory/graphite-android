package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.bucketItem.BucketItemViewModel
import com.syncodec.graphite.presentation.custom.button.MenuButton


@Composable
fun TopBar() {
	Bar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar() {
	val activity: BucketItemActivity = LocalContext.current as BucketItemActivity
	val viewModel: BucketItemViewModel = viewModel()
	var isFavourite by viewModel.isFavourite
	var isLocked by viewModel.isLocked

	SmallTopAppBar(
		navigationIcon = {
			MenuButton(icon = R.drawable.ic_back, contentDescription = "Back") { activity.finish() }
		},
		title = {},
		actions = {
			MenuButton(
				isChecked = isFavourite,
				isEnabled = true,
				icon = R.drawable.ic_favourite
			) { isFavourite = !isFavourite }
			MenuButton(
				isChecked = isLocked,
				isEnabled = true,
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open
			) { isLocked = !isLocked }
			MenuButton(
				isEnabled = true,
				icon = R.drawable.ic_delete,
				tint = Color(0xFFF05945),
				contentDescription = "Delete"
			) {

			}
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
	)
}
