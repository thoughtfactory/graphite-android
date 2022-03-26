package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import compose.icons.tablericons.Signature


sealed class MomentoComponentType {
	object Note : MomentoComponentType()
	object Notebook : MomentoComponentType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun MomentoScreen(
	noteList: List<NoteDbEntry>,
	notebookList: List<NotebookDbEntry>,
	momentoComponentType: MomentoComponentType,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
//	val chipDataList: MutableList<ChipData> = mutableListOf(
//		ChipData(title = "Archived", imageVector = TablerIcons.Archive, isSelected = showArchived) { showArchived = !showArchived },
//		ChipData(title = "Favourite", imageVector = TablerIcons.Heart, isSelected = showFavourite) { showFavourite = !showFavourite },
//	)
//	if (vaultState == Momento.Companion.VaultState.OPENED) {
//		chipDataList.add(ChipData(title = "Locked", imageVector = TablerIcons.Container, isSelected = showLocked) { showLocked = !showLocked })
//	}

	var currentState by remember { mutableStateOf(0) }
//	LaunchedEffect(key1 = currentState) {
//		viewModel.activityState.momentoComponentType.value = if (currentState == 0) MomentoComponentType.Note else MomentoComponentType.Notebook
//	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		TopBar(
			isSelected = isSelected,
			selectedItemSize = selectedItemList.size
		) { click, data -> onClick(click, data) }

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

//		AnimatedVisibility(
//			visible = showArchived || showFavourite || showLocked || vaultState == Momento.Companion.VaultState.OPENED,
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(4.dp, 0.dp, 4.dp, 8.dp)
//		) {
//			ChipView(chipDataList = chipDataList)
//		}

		Crossfade(
			targetState = momentoComponentType,
			modifier = Modifier
				.graphicsLayer {
//					this.scaleX = scaffoldScale
//					this.scaleY = scaffoldScale
				}
		) { momentoScreenType ->
			when (momentoScreenType) {
				MomentoComponentType.Note -> NoteScreen(
					noteList = noteList,
					isSelected = false,
					selectedItemList = selectedItemList,
					filterTag = listOf()
				) { click, data ->  onClick(click, data) }
				MomentoComponentType.Notebook -> NotebookScreen(
					notebookList = notebookList,
					isSelected = isSelected,
					selectedItemList = selectedItemList,
					filterTag = listOf()
				) {click, data ->  onClick(click, data)}
			}
		}
	}
}
