package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.raw.RawActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.Extra


@Composable
fun MenuBottomSheet(
	closeSheet : () -> Unit
) {
	val activity: NoteActivity = LocalContext.current as NoteActivity
	val viewModel : NoteViewModel = viewModel()

	val noteId by viewModel.noteId
	val parentChapterId by viewModel.parentChapterId
	val createdTimestamp by viewModel.createdTimestamp
	val modifiedTimestamp by viewModel.modifiedTimestamp
	val title by viewModel.title
	val contentThumbnail by viewModel.contentThumbnail
	val locationState by viewModel.locationState
	val latLng by viewModel.latLng
	val address by viewModel.address

	val isViewer by viewModel.isViewer

	var showNotificationPermissionDialog by remember { mutableStateOf(false) }
	var showExportOptions by remember { mutableStateOf(false) }

	val buttonList : List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(
			title = "Pin to Notification",
			icon = R.drawable.ic_pin
		) { showNotificationPermissionDialog = true },
		BottomSheetButtonData(title = "Share", icon = R.drawable.ic_share) {},
		BottomSheetButtonData(
			title = "Delete",
			icon = R.drawable.ic_delete,
			containerColor = Color.DeleteContainer,
			contentColor = Color.DeleteContent,
		) {},
		BottomSheetButtonData(title = "Export", icon = R.drawable.ic_export) { showExportOptions = ! showExportOptions },
		BottomSheetButtonData(title = "Copy", icon = R.drawable.ic_copy) {},
		BottomSheetButtonData(title = "Duplicate", icon = R.drawable.ic_note) {},
		BottomSheetButtonData(
			title = "Raw",
			icon = R.drawable.ic_raw_data
		) {
			Intent(activity, RawActivity::class.java).apply {
				putExtra(Extra.Companion.Constant.OBJECT_ID.name, noteId.toString())
				putExtra(Extra.Companion.Constant.OBJECT_TYPE.name, Extra.Companion.ObjectType.NOTE.name)

				activity.startActivity(this)
			}
		},
	)

	NotificationPermissionDialog(
		showDialog = showNotificationPermissionDialog,
		onDismiss = { showNotificationPermissionDialog = false },
	) {
		showNotificationPermissionDialog = false
		if (isViewer) {
			noteId?.let {
				parentChapterId?.let { it1 ->
					NotePinNotification()
						.showSimpleNotification(
							context = activity,
							noteId = it,
							chapterId = it1,
							title = title ?: "Untitled",
							content = contentThumbnail ?: "No content",
							notificationId = noteId?.hashCode() ?: 0,
						) { Toast.makeText(activity, "Notification permission not available. Please enable permission from settings", Toast.LENGTH_SHORT).show() }
				}
			}
		} else {
			Toast.makeText(activity, "Please save note before pinning", Toast.LENGTH_SHORT).show()
		}
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu
		)

		BottomSheetButtonGrid(buttonList = buttonList)

		ExpandableBox(
			isVisible = showExportOptions
		) {
			val containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f)
			val contentColor = MaterialTheme.colorScheme.onBackground
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp, 0.dp)
			) {
				SettingsButton(
					title = "Export as PDF",
					icon = R.drawable.ic_file_pdf,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
				SettingsButton(
					title = "Export as HTML",
					icon = R.drawable.ic_file_html,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
				SettingsButton(
					title = "Export as Markdown",
					icon = R.drawable.ic_file_pdf,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
				SettingsButton(
					title = "Export as Text",
					icon = R.drawable.ic_file_text,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
				SettingsButton(
					title = "Export as Image",
					icon = R.drawable.ic_file_image,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
				SettingsButton(
					title = "Export all Attachments",
					icon = R.drawable.ic_gallery,
					containerColor = containerColor,
					contentColor = contentColor
				) {}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
