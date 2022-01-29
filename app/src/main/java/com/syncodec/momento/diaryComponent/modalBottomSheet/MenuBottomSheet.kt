package com.syncodec.momento.diaryComponent.modalBottomSheet

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.diaryComponent.DiaryViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch


data class MenuBottomSheetButtonData(val title: String, val imageVector: ImageVector, val highlight: Boolean = false, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet() {
	val viewModel: DiaryViewModel = viewModel()
	val scope = rememberCoroutineScope()


	val hideSheet: () -> Unit = {
		scope.launch {
			viewModel.diaryActivityState.bottomSheetState.hide()
		}
	}

	val menuBottomSheetButtonDataLists: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Archive", imageVector = TablerIcons.Archive) {},
		MenuBottomSheetButtonData(title = "Favourite", imageVector = TablerIcons.Heart) {},
		MenuBottomSheetButtonData(title = "Pin to top", imageVector = TablerIcons.Pinned) {},
		MenuBottomSheetButtonData(title = "Pin to notification", imageVector = TablerIcons.Notification) {},

		MenuBottomSheetButtonData(title = "Add to notebook", imageVector = TablerIcons.Notebook) {},
		MenuBottomSheetButtonData(title = "Move in vault", imageVector = TablerIcons.Container) {},
		MenuBottomSheetButtonData(title = "Duplicate", imageVector = TablerIcons.Copy) {},
		MenuBottomSheetButtonData(title = "Move to trash", imageVector = TablerIcons.Trash) {},

		MenuBottomSheetButtonData(title = "Discard changes", imageVector = TablerIcons.X) {},
		MenuBottomSheetButtonData(title = "Share", imageVector = TablerIcons.Share) {},
		MenuBottomSheetButtonData(title = "Export", imageVector = TablerIcons.FileExport) {},
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			imageVector = TablerIcons.Dots
		)

		TimestampCard(
			createdTimestamp = viewModel.diary.createdTimestamp,
			modifiedTimestamp = viewModel.diary.modifiedTimestamp
		)

		Spacer(modifier = Modifier.height(12.dp))

		LazyVerticalGrid(
			cells = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(menuBottomSheetButtonDataLists) { _, menuBottomSheetButtonData ->
				MenuBottomSheetButton(menuBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun MenuBottomSheetButton(
	menuBottomSheetButtonData: MenuBottomSheetButtonData?
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (menuBottomSheetButtonData != null) {
			Crossfade(targetState = menuBottomSheetButtonData.highlight) { highlight ->
				if (highlight) {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.onSecondaryContainer,
						shape = RoundedCornerShape(16.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clip(RoundedCornerShape(16.dp))
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.secondaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				} else {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
						shape = RoundedCornerShape(8.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clip(RoundedCornerShape(8.dp))
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				}
			}
			Text(
				text = menuBottomSheetButtonData.title,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				maxLines = 2,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
	}
}
