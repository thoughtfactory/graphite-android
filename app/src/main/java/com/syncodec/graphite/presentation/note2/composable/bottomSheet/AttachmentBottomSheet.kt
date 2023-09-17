package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.attachment.AttachmentPreview
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.presentation.common.row.SameHeightRowGrid
import com.syncodec.graphite.presentation.note2.NoteViewModel2
import com.syncodec.graphite.presentation.base.AttachmentContainer
import com.syncodec.graphite.presentation.base.AttachmentContent
import com.syncodec.graphite.utils.createTempAttachmentFileToExpose
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AttachmentBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	attachmentList: List<NoteViewModel2.Companion.AttachmentState> = listOf(),
	onAddNewAttachment: (List<Uri>) -> Unit = {},
	toggleAttachment: (NoteViewModel2.Companion.AttachmentState) -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context) }
	val showPlainTextWarning by dataStoreInstance.showPlainTextWarningAttachment.collectAsState(initial = false)

	var photoUri: Uri? by remember { mutableStateOf(null) }
	val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
		try {
			if (isCaptured) photoUri?.let { uri -> onAddNewAttachment(listOf(uri)) }
		} catch (e: Exception) {
			Toast.makeText(context, "Failed to add attachment", Toast.LENGTH_SHORT).show()
		}
	}

	val mediaPickerRequest = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
		if (uriList.isEmpty()) Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
		else onAddNewAttachment(uriList)
	}

	val filePickerRequest = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		if (uriList.isEmpty()) Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
		else onAddNewAttachment(uriList)
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Attachments",
		) {
			SameHeightRowGrid {
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_camera,
					text = stringResource(id = R.string.camera),
					contentDescription = stringResource(id = R.string.camera),
				) {
					photoUri = createTempAttachmentFileToExpose(context = context, name = "${RealmUUID.random()}.jpg").first
					takePicture.launch(photoUri)
				}
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_gallery,
					text = stringResource(id = R.string.gallery),
					contentDescription = stringResource(id = R.string.gallery),
				) {
					mediaPickerRequest.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
				}
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_new_file,
					text = stringResource(id = R.string.file),
					contentDescription = stringResource(id = R.string.file),
				) {
					filePickerRequest.launch(arrayOf("*/*"))
				}
			}

			if (attachmentList.isNotEmpty()) Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (showPlainTextWarning) {
					item(
						span = { GridItemSpan(this.maxLineSpan) }
					) {
						InfoCard(
							title = "Unencrypted data",
							description = "Attachments (files) are not encrypted and stored as it is",
							icon = R.drawable.ic_fa_warning,
							colors = InfoCardDefaults.warningCardColors(),
							buttonText = stringResource(id = R.string.dismiss),
							onClickButton = { dataStoreInstance.putShowPlainTextWarningAttachment(false) }
						)
					}
				}
				attachmentList.sortedBy { it.name }.forEach { attachmentState ->
					item {
						AttachmentItem(
							attachmentState = attachmentState,
							onClickAttachment = {},
							onClickRemove = { toggleAttachment(attachmentState) }
						)
					}
				}
				if (attachmentList.isNotEmpty()) item(
					span = { GridItemSpan(this.maxLineSpan) }
				) {
					Spacer(modifier = Modifier.height(32.dp))
				}
			}
		}
	}
}

@Composable
private fun AttachmentItem(
	attachmentState: NoteViewModel2.Companion.AttachmentState,
	onClickAttachment: () -> Unit = {},
	onClickRemove: () -> Unit = {},
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(1.dp)
	) {
		when (attachmentState) {
			is NoteViewModel2.Companion.AttachmentState.Saved -> AttachmentPreview(
				file = attachmentState.file,
				showFileName = true,
				onClick = onClickAttachment,
			)

			is NoteViewModel2.Companion.AttachmentState.New -> AttachmentPreview(
				uri = attachmentState.uri,
				showFileName = true,
				onClick = onClickAttachment,
			)

			is NoteViewModel2.Companion.AttachmentState.ToRemove -> AttachmentPreview(
				file = attachmentState.file,
				showFileName = true,
				onClick = onClickAttachment,
			)
		}

		AnimatedVisibility(
			visible = attachmentState is NoteViewModel2.Companion.AttachmentState.ToRemove,
			enter = fadeIn(tween(470)),
			exit = fadeOut(tween(470))
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.error.copy(alpha = 0.47f))
			)
		}

		Row(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.fillMaxWidth()
				.padding(8.dp)
		) {
			Spacer(modifier = Modifier.weight(1f))
			if (attachmentState is NoteViewModel2.Companion.AttachmentState.New) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.height(28.dp)
						.background(Color.AttachmentContainer, MaterialTheme.shapes.extraSmall)
						.padding(horizontal = 8.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_plus),
						contentDescription = "New attachment",
						tint = Color.AttachmentContent,
						modifier = Modifier.requiredSize(16.dp),
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = stringResource(id = R.string.new_),
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = Color.AttachmentContent,
					)
				}
			}
			Spacer(modifier = Modifier.width(4.dp))
			FilledIconButton(
				shape = MaterialTheme.shapes.extraSmall,
				colors = IconButtonDefaults.filledIconButtonColors(
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.onErrorContainer
				),
				modifier = Modifier.requiredSize(28.dp),
				onClick = onClickRemove
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_minus_solid),
					contentDescription = "Remove attachment",
					modifier = Modifier.requiredSize(16.dp)
				)
			}
		}
	}
}
