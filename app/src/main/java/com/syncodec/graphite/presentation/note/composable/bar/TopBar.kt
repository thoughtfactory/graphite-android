package com.syncodec.graphite.presentation.note.composable.bar

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsOperationPending
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalSaveNote


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onClickBack : () -> Unit,
	onClickMenu : () -> Unit,
	onClickLock : () -> Unit,
	onClickFavourite : () -> Unit,
) {
	val context = LocalContext.current

	val isFavourite = LocalCompositionIsFavourite.current
	val isLocked = LocalCompositionIsLocked.current
	val isViewing = LocalCompositionIsViewing.current
	val isOperationPending = LocalCompositionIsOperationPending.current

	val saveNote = LocalSaveNote.current

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back"
			) {
				if (isOperationPending) Toast.makeText(context, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
				else onClickBack()
			}
		},
		title = {},
		actions = {
			when (isViewing) {
				true -> {
					MenuButton(
						icon = if (isLocked == true) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
						contentDescription = "Lock",
						isChecked = isLocked == true,
						onClick = onClickLock
					)
					MenuButton(
						icon = R.drawable.ic_favourite,
						contentDescription = "Favourite",
						isChecked = isFavourite == true,
						onClick = onClickFavourite
					)
					MenuButton(
						icon = R.drawable.ic_menu,
						contentDescription = "Menu",
						onClick = onClickMenu
					)
					Spacer(modifier = Modifier.width(4.dp))
				}

				false -> {
					Button(onClick = saveNote) {
						Text(text = "Save")
					}
					Spacer(modifier = Modifier.width(4.dp))
				}

				null -> Unit
			}
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
	)
}
