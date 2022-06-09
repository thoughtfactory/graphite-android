package com.syncodec.graphite.mainComponent.screen

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.database.quote.QuoteDbEntry
import java.io.File


@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun GraphiteScreen(
	noteMap: Map<String, NoteDbEntry>,
	notebookListFlow: List<NotebookDbEntry>,
	componentType: MainActivity.ComponentType,
	isSelected: Boolean,
	selectedItemList: List<String>,
	isFilterActive: Boolean,
	quote: QuoteDbEntry?,
	quoteBg: Drawable?,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		Crossfade(
			targetState = componentType,
			modifier = Modifier
		) {
			when (it) {
				MainActivity.ComponentType.NOTE -> NoteScreen(
					noteMap = noteMap,
					selectedItemList = selectedItemList,
					isFilterActive = isFilterActive,
					quote = quote,
					quoteBg = quoteBg,
				) { click, data -> onAction(click, data) }
				MainActivity.ComponentType.NOTEBOOK -> NotebookScreen(
					notebookListFlow = notebookListFlow,
					isSelected = isSelected,
					selectedItemList = selectedItemList,
					filterTag = listOf()
				) { click, data -> onAction(click, data) }
			}
		}
	}
}
