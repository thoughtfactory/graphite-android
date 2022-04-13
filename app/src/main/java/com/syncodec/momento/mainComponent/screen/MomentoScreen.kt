package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry


@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun MomentoScreen(
	noteMap: Map<String, NoteDbEntry>,
	notebookMap: Map<String, Pair<NotebookDbEntry, Int>>,
	componentType: MainActivity.ComponentType,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	Box(
		modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		Crossfade(
			targetState = componentType,
			modifier = Modifier
		) {
			when (it) {
				MainActivity.ComponentType.NOTE -> NoteScreen(
					noteMap = noteMap,
					isSelected = false,
					selectedItemList = selectedItemList,
					filterTag = listOf()
				) { click, data ->  onAction(click, data) }
				MainActivity.ComponentType.NOTEBOOK -> NotebookScreen(
					notebookMap = notebookMap,
					isSelected = isSelected,
					selectedItemList = selectedItemList,
					filterTag = listOf()
				) {click, data ->  onAction(click, data)}
			}
		}
	}
}
