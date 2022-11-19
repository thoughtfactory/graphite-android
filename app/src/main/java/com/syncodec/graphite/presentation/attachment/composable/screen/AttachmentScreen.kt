package com.syncodec.graphite.presentation.attachment.composable.screen

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.attachment.composable.bar.TopBar
import com.syncodec.graphite.presentation.attachment.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentCard
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AttachmentScreen(
	noteObject : NoteObject?,
	chapterObject: ChapterObject?,
	attachmentList: Map<RealmUUID, Triple<AttachmentObject, File?, Uri?>>
) {
	val activity = LocalContext.current as AttachmentActivity
	val scope = rememberCoroutineScope()

	val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	val openSheet = { scope.launch { modalBottomSheetState.show() } }

	ModalBottomSheetLayout(
		sheetState = modalBottomSheetState,
		sheetContent = {
			MenuBottomSheet()
		},
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				TopBar(
					onClickBack = { activity.onBackPressed() },
					onClickMenu = { openSheet() }
				)
			},
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(144.dp),
					modifier = Modifier.fillMaxSize()
				) {

					header {
						if (noteObject != null || chapterObject != null) {
							AttachmentHeader(
								noteObject = noteObject,
								chapterObject = chapterObject
							)
						}
					}

					attachmentList.forEach { (id, data) ->
						item {
							AttachmentCard(
								uri = data.third,
								file = data.second,
								attachmentObject = data.first
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun AttachmentHeader(
	noteObject : NoteObject?,
	chapterObject : ChapterObject?,
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Text(
				text = (noteObject?.title ?: chapterObject?.title) ?: "Untitled",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onBackground,
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = (noteObject?.id ?: chapterObject?.id).toString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
			)
		}
	}
}

fun LazyGridScope.header(
	content : @Composable LazyGridItemScope.() -> Unit
) {
	item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}
