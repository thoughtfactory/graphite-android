package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.attachment.composable.dialog.AttachmentDialogType
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.DeleteContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onClickBack: () -> Unit,
) {
	val isSelected = LocalCompositionIsSelected.current
	val selectedAttachmentList = AttachmentActivity.LocalSelectedAttachmentList.current
	val onSelect = LocalCompositionOnSelect.current

	val openDialog = AttachmentActivity.LocalOpenDialog.current

	Crossfade(
		targetState = isSelected
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_close,
						tint = MaterialTheme.colorScheme.onBackground,
					) {
						onSelect(false)
						selectedAttachmentList.clear()
					}
				},
				title = {
					Text(
						text = if (selectedAttachmentList.isEmpty()) "No items selected" else if (selectedAttachmentList.size == 1) "1 item selected" else "${selectedAttachmentList.size} items selected",
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(
						onClick = { openDialog(AttachmentDialogType.DELETE) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Delete items",
							tint = Color.DeleteContainer
						)
					}
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
