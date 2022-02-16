package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.ComponentChooser
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Trash


sealed class MomentoScreenType {
	object Diary : MomentoScreenType()
	object Notebook : MomentoScreenType()
	object Scratchpad : MomentoScreenType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun MomentoScreen() {
	val viewModel: MainViewModel = viewModel()

	var showArchived by viewModel.mainActivityState.showArchived
	var showFavourite by viewModel.mainActivityState.showFavourite
	var showTrash by viewModel.mainActivityState.showTrash
	val isSelected by viewModel.mainActivityState.isSelected
	var showDeleteDialog by viewModel.mainActivityState.showDeleteDialog

	val scaffoldScale by animateFloatAsState(
		targetValue = if (viewModel.mainActivityState.bottomSheetState.progress.to == ModalBottomSheetValue.Hidden) 1f else 0.95f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioHighBouncy,
			stiffness = Spring.StiffnessMediumLow
		),
	)

	val chipDataList: List<ChipData> = listOf(
		ChipData(title = "Archived", imageVector = TablerIcons.Archive, isSelected = showArchived) { showArchived = !showArchived },
		ChipData(title = "Favourite", imageVector = TablerIcons.Heart, isSelected = showFavourite) { showFavourite = !showFavourite },
//		ChipData(title = "Trash", imageVector = TablerIcons.Trash, isSelected = showTrash) { showTrash = !showTrash },
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		ComponentChooser(
			isSelected = isSelected,
			selectedSize = viewModel.mainActivityState.selectedEntryList.size,
			onClickDelete = { showDeleteDialog = true }
		)

		AnimatedVisibility(
			visible = showArchived || showFavourite || showTrash,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp, 0.dp, 4.dp, 8.dp)
		) {
			ChipView(chipDataList = chipDataList)
		}

		Crossfade(
			targetState = viewModel.mainActivityState.momentoScreenType.value,
			modifier = Modifier
				.graphicsLayer {
					this.scaleX = scaffoldScale
					this.scaleY = scaffoldScale
				}
		) { momentoScreenType ->
			when (momentoScreenType) {
				MomentoScreenType.Diary -> DiaryScreen()
				MomentoScreenType.Notebook -> NotebookScreen()
				MomentoScreenType.Scratchpad -> ScratchpadScreen()
			}
		}
	}
}
