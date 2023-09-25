package com.syncodec.graphite.presentation.search.composable

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.SearchSelectionActionView
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.presentation.notebook.composable.noteList
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.search.composable.bar.BottomBar
import com.syncodec.graphite.presentation.search.composable.bar.TopBar
import com.syncodec.graphite.presentation.search.composable.buildingBlock.SearchWhatView
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SearchScreen(
	tagList: List<TagObject> = listOf(),
	filteredNoteList: List<NoteObjectLite> = listOf(),
	currentFilterList: Set<SearchViewModel.Companion.NoteFilter> = setOf(),
	onAddFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
	onRemoveFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
) {
	val context = LocalContext.current

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID) {
		if (!isSelecting) isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = setOf()
	}

	GenericScaffold2(
		topBar = {
			TopBar(
				currentFilterList = currentFilterList,
				onAddFilter = onAddFilter,
				onRemoveFilter = onRemoveFilter,
			)
		},
		bottomBar = { BottomBar() },
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting
	) {
		Crossfade(
			targetState = currentFilterList.isEmpty(),
			label = "searchWhat_animation"
		) { isSearchWhatViewVisible ->
			if (isSearchWhatViewVisible) {
				SearchWhatView(
					tagList = tagList,
					onAddFilter = onAddFilter
				)
			}
			else {
				LazyColumn(
					modifier = Modifier.fillMaxSize()
				) {
//					noteList(
//						noteList = filteredNoteList,
//						selectedIdList = selectedIdList,
////						headerTitle = ,
////						headerSubTitle =,
////						headerMinHeight =,
////						headerBackgroundColor =,
////						isVisible =,
////						toggleVisibility =,
//						onClick = {
//							if (isSelecting) {
//								onSelect(it.id)
//							}
//							else {
//								Intent(context, NoteActivity2::class.java).apply {
//									putExtra(Extra.Companion.Extra.IsNew.name, false)
//									putExtra(Extra.Companion.Extra.NoteId.name, it.id.bytes)
//									putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
//									context.startActivity(this)
//								}
//							}
//						},
//						onLongClick = { onSelect(it.id) },
//					)
				}
			}
		}

		SearchSelectionActionView(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
			isSelecting = isSelecting,
			isAllItemFavourite = selectedIdList.isNotEmpty() && filteredNoteList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = selectedIdList.isNotEmpty() && filteredNoteList.filter { it.id in selectedIdList }.all { it.isLocked },
			selectedItemCount = selectedIdList.size,
			onClickDelete = {},
			onClickFavourite = {},
			onClickLock = {},
		)
	}
}
