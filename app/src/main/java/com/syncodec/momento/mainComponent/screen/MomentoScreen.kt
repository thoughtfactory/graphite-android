package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import java.text.SimpleDateFormat


sealed class MomentoComponentType {
	object Diary : MomentoComponentType()
	object Notebook : MomentoComponentType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun MomentoScreen() {
	val viewModel: MainViewModel = viewModel()

	val vaultState by viewModel.activityState.vaultState
	var showArchived by viewModel.activityState.showArchived
	var showFavourite by viewModel.activityState.showFavourite
	var showLocked by viewModel.activityState.showLocked

	val scaffoldScale by animateFloatAsState(
		targetValue = if (viewModel.activityState.bottomSheetState.progress.to == ModalBottomSheetValue.Hidden) 1f else 0.95f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioHighBouncy,
			stiffness = Spring.StiffnessMediumLow
		),
	)

	val chipDataList: MutableList<ChipData> = mutableListOf(
		ChipData(title = "Archived", imageVector = TablerIcons.Archive, isSelected = showArchived) { showArchived = !showArchived },
		ChipData(title = "Favourite", imageVector = TablerIcons.Heart, isSelected = showFavourite) { showFavourite = !showFavourite },
	)
	if (vaultState == Momento.Companion.VaultState.OPENED) {
		chipDataList.add(ChipData(title = "Locked", imageVector = TablerIcons.Container, isSelected = showLocked) { showLocked = !showLocked })
	}

	var currentState by remember { mutableStateOf(0) }
	LaunchedEffect(key1 = currentState) {
		viewModel.activityState.momentoComponentType.value = if (currentState == 0) MomentoComponentType.Diary else MomentoComponentType.Notebook
	}

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
					StateData(title = "Diary", icon = TablerIcons.Signature, color = MaterialTheme.colorScheme.primary),
					StateData(title = "Notebook", icon = TablerIcons.Notebook, color = MaterialTheme.colorScheme.primary),
				),
				currentState = currentState,
				modifier = Modifier
					.height(32.dp)
			) { currentState = it }
		}

		AnimatedVisibility(
			visible = showArchived || showFavourite || showLocked || vaultState == Momento.Companion.VaultState.OPENED,
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
