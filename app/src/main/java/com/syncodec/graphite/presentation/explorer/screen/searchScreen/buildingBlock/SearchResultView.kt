package com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.noteList
import com.syncodec.graphite.utils.LocalIsAuthenticated
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun SearchResultView(
	noteList : List<NoteObjectLite> = listOf(),
	tagList : List<TagObject> = listOf(),
	selectedIdList : List<RealmUUID> = listOf(),
	onClickNote : (RealmUUID) -> Unit = {},
	onLongClickNote : (RealmUUID) -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current

	AnimatedContent(
		targetState = noteList,
		transitionSpec = { scaleIn(tween(300)) with scaleOut(tween(300)) }
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			noteList(
				noteList = it.filter { if (it.isLocked) isAuthenticated else true },
				tagList = tagList,
				selectedIdList = selectedIdList,
				onClick = { onClickNote(it.id) },
				onLongClick = { onLongClickNote(it.id) },
			)

			item { Spacer(modifier = Modifier.height(32.dp)) }
		}
	}
}
