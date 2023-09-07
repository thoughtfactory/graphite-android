package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.presentation.common.row.SameHeightRowGrid
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AttachmentBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	savedFileList: List<File> = listOf(),
	newFileList: List<Uri> = listOf(),
	toRemoveFileList: List<File> = listOf(),
	onAddNewFile: (List<Uri>) -> Unit = {},
	onRemoveNewFile: (List<Uri>) -> Unit = {},
	onRemoveSavedFile: (List<File>) -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context) }
	val showPlainTextWarning by dataStoreInstance.showPlainTextWarningAttachment.collectAsState(initial = false)

	val mediaPickerRequest = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
		if (uriList.isEmpty()) Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
		else onAddNewFile(uriList)
	}

	val filePickerRequest = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		if (uriList.isEmpty()) Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
		else onAddNewFile(uriList)
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

			if (savedFileList.isNotEmpty() or newFileList.isNotEmpty()) Spacer(modifier = Modifier.height(12.dp))

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
							buttonText = "Dismiss",
							onClickButton = { dataStoreInstance.putShowPlainTextWarningAttachment(false) }
						)
					}
				}
				newFileList.forEach { uri ->
					item { AttachmentItem(uri = uri) }
				}
				savedFileList.forEach { file ->
					item { AttachmentItem(file = file) }
				}
				if (newFileList.isNotEmpty() || savedFileList.isNotEmpty()) item(
					span = { GridItemSpan(this.maxLineSpan) }
				) {
					Spacer(modifier = Modifier.height(64.dp))
				}
			}
		}
	}
}

@Preview
@Composable
private fun AttachmentItem(
	file: File = File(""),
	onClick: () -> Unit = {}
) {
	val context = LocalContext.current
	val imageRequest = remember(file) {
		ImageRequest.Builder(context)
			.data(file)
			.crossfade(false)
			.build()
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(1.dp)
			.clickable { onClick() }
	) {
		AsyncImage(
			model = imageRequest,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
				.blur(24.dp)
		)

		AsyncImage(
			model = imageRequest,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Preview
@Composable
private fun AttachmentItem(
	uri: Uri = Uri.EMPTY,
	onClick: () -> Unit = {},
) {
	val context = LocalContext.current

	val imageRequest = remember(uri) {
		ImageRequest.Builder(context)
			.data(uri)
			.crossfade(true)
			.build()
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(1.dp)
			.clickable { onClick() }
	) {
		AsyncImage(
			model = imageRequest,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
				.blur(24.dp)
		)

		AsyncImage(
			model = imageRequest,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize()
		)
	}
}
