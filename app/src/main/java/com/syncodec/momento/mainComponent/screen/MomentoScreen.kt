package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
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
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.ComponentChooser
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Notebook
import compose.icons.tablericons.Signature


sealed class MomentoComponentType {
	object Diary : MomentoComponentType()
	object Notebook : MomentoComponentType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun MomentoScreen() {
	val viewModel: MainViewModel = viewModel()

	var showArchived by viewModel.activityState.showArchived
	var showFavourite by viewModel.activityState.showFavourite
	var showTrash by viewModel.activityState.showTrash

	val scaffoldScale by animateFloatAsState(
		targetValue = if (viewModel.activityState.bottomSheetState.progress.to == ModalBottomSheetValue.Hidden) 1f else 0.95f,
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
		TopBar()

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			StateButton(
				stateList = listOf(
					StateData(title = "Diary", icon = TablerIcons.Signature, color = androidx.compose.material3.MaterialTheme.colorScheme.primary),
					StateData(title = "Notebook", icon = TablerIcons.Notebook, color = androidx.compose.material3.MaterialTheme.colorScheme.primary),
				),
				initialState = if (viewModel.activityState.momentoComponentType.value == MomentoComponentType.Diary) 0 else 1,
				modifier = Modifier
					.height(32.dp)
			) {
				if (it == 0) {
					viewModel.activityState.momentoComponentType.value = MomentoComponentType.Diary
				} else {
					viewModel.activityState.momentoComponentType.value = MomentoComponentType.Notebook
				}
			}
		}

		AnimatedVisibility(
			visible = showArchived || showFavourite || showTrash,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp, 0.dp, 4.dp, 8.dp)
		) {
			ChipView(chipDataList = chipDataList)
		}

		Crossfade(
			targetState = viewModel.activityState.momentoComponentType.value,
			modifier = Modifier
				.graphicsLayer {
					this.scaleX = scaffoldScale
					this.scaleY = scaffoldScale
				}
		) { momentoScreenType ->
			when (momentoScreenType) {
				MomentoComponentType.Diary -> DiaryScreen()
				MomentoComponentType.Notebook -> NotebookScreen()
			}
		}
	}
}
