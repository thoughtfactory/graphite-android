package com.syncodec.momento.mainComponent.modalBottomSheet

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.screen.MomentoScreenType
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch


data class MenuBottomSheetButtonData(val title: String, val imageVector: ImageVector, val highlight: Boolean = false, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet() {
	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	val hideSheet: () -> Unit = {
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.hide()
		}
	}

	val menuBottomSheetButtonDataLists: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Media", imageVector = TablerIcons.Photo) {},
		MenuBottomSheetButtonData(title = "Vault", imageVector = TablerIcons.Container) {},
		MenuBottomSheetButtonData(title = "Life in Weeks", imageVector = TablerIcons.CalendarMinus) {},
		MenuBottomSheetButtonData(title = "Settings", imageVector = TablerIcons.Settings) {},

		MenuBottomSheetButtonData(title = "Archived", imageVector = TablerIcons.Archive) {},
		MenuBottomSheetButtonData(title = "Favourite", imageVector = TablerIcons.Heart) {},
		MenuBottomSheetButtonData(title = "Pinned", imageVector = TablerIcons.Pinned) {},
		MenuBottomSheetButtonData(title = "Trash", imageVector = TablerIcons.Trash) {},

		MenuBottomSheetButtonData(
			title = "Diary",
			imageVector = TablerIcons.Signature,
			highlight = viewModel.mainActivityState.momentoScreenType.value == MomentoScreenType.Diary
		) {
			viewModel.mainActivityState.momentoScreenType.value = MomentoScreenType.Diary
			hideSheet()
		},
		MenuBottomSheetButtonData(
			title = "Notebook",
			imageVector = TablerIcons.Notebook,
			highlight = viewModel.mainActivityState.momentoScreenType.value == MomentoScreenType.Notebook
		) {
			viewModel.mainActivityState.momentoScreenType.value = MomentoScreenType.Notebook
			hideSheet()
		},
		MenuBottomSheetButtonData(
			title = "Scratchpad",
			imageVector = TablerIcons.Notes,
			highlight = viewModel.mainActivityState.momentoScreenType.value == MomentoScreenType.Scratchpad
		) {
			viewModel.mainActivityState.momentoScreenType.value = MomentoScreenType.Scratchpad
			hideSheet()
		},
		null,
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

		LazyVerticalGrid(
			cells = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(menuBottomSheetButtonDataLists) { _, menuBottomSheetButtonData ->
				MenuBottomSheetButton(menuBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		Card(
			backgroundColor = MaterialTheme.colorScheme.primaryContainer,
			elevation = 0.dp,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.padding(24.dp, 0.dp),
			onClick = { /*TODO*/ }
		) {

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
