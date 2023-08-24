package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.selectionAction.SelectionActionView
import com.syncodec.graphite.utils.AttachmentUtil
import com.syncodec.graphite.utils.share
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AttachmentViewerBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	savedFileList: List<File> = listOf(),
) {
	val context = LocalContext.current

	var isSelecting by remember { mutableStateOf(false) }
	var selectedFileSet by remember { mutableStateOf<Set<File>>(setOf()) }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
		modifier = Modifier.fillMaxSize()
	) {
		GenericBottomSheetSkeleton2(
			title = "Attachments",
			modifier = Modifier.weight(1f)
		) {
			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				savedFileList.sortedBy { it.name }.forEach { file ->
					item(key = file) {
						AttachmentItem(
							file = file,
							isSelected = file in selectedFileSet,
							onClick = {
								if (isSelecting) {
									selectedFileSet.toMutableSet().apply {
										if (file in this) remove(file) else add(file)
										selectedFileSet = this.toSet()
									}
								}
							},
							onLongClick = {
								isSelecting = true
								selectedFileSet.toMutableSet().apply {
									if (file in this) remove(file) else add(file)
									selectedFileSet = this.toSet()
								}
							}
						)
					}
				}
			}
		}

		SelectionActionView(
			isSelecting = isSelecting,
			selectedItemCount = selectedFileSet.size,
			modifier = Modifier.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp),
			onClickShare = { selectedFileSet.share(context = context) },
			onClickSelectAll = { selectedFileSet = savedFileList.toSet() },
			onClickDelete = {},
			onClickCancel = {
				isSelecting = false
				selectedFileSet = setOf()
			},
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun AttachmentItem(
	file: File = File(""),
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val context = LocalContext.current

	var bitmap by remember { mutableStateOf<Bitmap?>(null) }
	var bitmapOverlay by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = file) {
		bitmap = AttachmentUtil.getPreview(context = context, file = file)
	}

	val borderWidth by animateDpAsState(targetValue = if (isSelected) 2.dp else 0.dp, label = "borderWidth_animation")
	val borderColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent, label = "borderColor_animation")

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(1.dp)
			.background(MaterialTheme.colorScheme.surface)
			.border(borderWidth, borderColor)
			.combinedClickable(enabled = true, onClick = onClick, onLongClick = onLongClick)
	) {
		bitmap?.let {
			if (Build.VERSION.SDK_INT >= 31) {
				Image(
					bitmap = it.asImageBitmap(),
					contentDescription = file.name ?: "Attachment Preview",
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
						.blur(24.dp)
				)
			}

			Image(
				bitmap = it.asImageBitmap(),
				contentDescription = file.name ?: "Attachment Preview",
				contentScale = ContentScale.Fit,
				modifier = Modifier.fillMaxSize()
			)
		} ?: Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 12.dp)
		) {
			Spacer(modifier = Modifier.weight(1f))
			Spacer(modifier = Modifier.height(8.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_file),
				contentDescription = "File",
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
				modifier = Modifier.requiredSize(40.dp)
			)
			Spacer(modifier = Modifier.height(8.dp))
			Box(modifier = Modifier.weight(1f)) {
				Text(
					text = file.name,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
				)
			}
		}
	}
}


//@Preview
//@Composable
//private fun AttachmentItem(
//	file: File = File(""),
//	onClick: () -> Unit = {},
//) {
//	val context = LocalContext.current
//
//	val imageRequest = remember(file) {
//		ImageRequest.Builder(context)
//			.data(file)
//			.crossfade(true)
//			.build()
//	}
//
//	Box(
//		contentAlignment = Alignment.Center,
//		modifier = Modifier
//			.fillMaxWidth()
//			.aspectRatio(1f)
//			.padding(1.dp)
//			.clip(MaterialTheme.shapes.medium)
//			.clickable { onClick() }
//	) {
//		AsyncImage(
//			model = imageRequest,
//			contentDescription = "stringResource(R.string.description)",
//			contentScale = ContentScale.Crop,
//			modifier = Modifier
//				.fillMaxSize()
//				.blur(24.dp)
//		)
//
//		AsyncImage(
//			model = imageRequest,
//			contentDescription = "stringResource(R.string.description)",
//			contentScale = ContentScale.Fit,
//			modifier = Modifier.fillMaxSize()
//		)
//	}
//}

