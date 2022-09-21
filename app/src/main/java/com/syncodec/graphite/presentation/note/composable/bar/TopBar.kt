package com.syncodec.graphite.presentation.note.composable.bar

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note.NoteViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onClickMetadata: () -> Unit
) {
	val activity: NoteActivity = LocalContext.current as NoteActivity
	val viewModel: NoteViewModel = viewModel()

	val isViewer by viewModel.isViewer
	val isSaving by viewModel.isSaving

	val isFavourite by viewModel.isFavourite
	val isLocked by viewModel.isLocked

	TopAppBar(
		navigationIcon = {
			Crossfade(targetState = isViewer) {
				if (it) {
					MenuButton(
						icon = R.drawable.ic_back,
						contentDescription = "Back"
					) {
						if (isSaving)
							Toast.makeText(activity, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
						else
							activity.onBackPressed()
					}
				} else {
					MenuButton(
						icon = R.drawable.ic_close,
						contentDescription = "Discard",
					) {
						if (isSaving)
							Toast.makeText(activity, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
						else
							activity.onBackPressed()
					}
				}
			}
		},
		title = {},
		actions = {
			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				contentDescription = "Lock",
				isChecked = isLocked
			) { viewModel.onUpdateLock() }
			MenuButton(
				icon = R.drawable.ic_favourite,
				contentDescription = "Favourite",
				isChecked = isFavourite
			) { viewModel.onUpdateFavorite() }
			MenuButton(
				icon = R.drawable.ic_info,
				contentDescription = "Metadata",
				onClick = onClickMetadata
			)
			Spacer(modifier = Modifier.width(4.dp))
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
	)
}
