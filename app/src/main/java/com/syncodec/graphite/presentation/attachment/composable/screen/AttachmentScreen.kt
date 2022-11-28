package com.syncodec.graphite.presentation.attachment.composable.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.attachment.composable.bar.TopBar
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentCard
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.attachment.composable.dialog.AttachmentDialog
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewFile
import io.realm.kotlin.types.RealmUUID
import java.io.File


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AttachmentScreen(
	noteObject : NoteObject?,
	chapterObject : ChapterObject?,
	attachmentList : List<Triple<RealmUUID, File?, Uri?>>,
	onClickBack : () -> Unit,
) {
	val context = LocalContext.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedAttachmentList = AttachmentActivity.LocalSelectedAttachmentList.current
	val onSelect = LocalCompositionOnSelect.current

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(onClickBack = onClickBack)
		},
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {

			if (attachmentList.isEmpty()) {
				EmptyView()
			} else {
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

					attachmentList.sortedBy { it.second?.hashCode()?.plus((it.third?.hashCode() ?: 0)) }.forEach { attachment ->
						item(
							key = attachment.hashCode()
						) {
							Box(
								modifier = Modifier.animateItemPlacement()
							) {
								AttachmentCard(
									uri = attachment.third,
									file = attachment.second,
									isSelected = attachment in selectedAttachmentList,
									onClick = {
										when {
											isSelected && attachment in selectedAttachmentList -> selectedAttachmentList.remove(attachment)
											isSelected && attachment !in selectedAttachmentList -> selectedAttachmentList.add(attachment)
											else -> attachment.second?.viewFile(context)
										}
									},
									onLongClick = {
										onSelect(true)
										if (attachment in selectedAttachmentList) selectedAttachmentList.remove(attachment)
										else selectedAttachmentList.add(attachment)
									},
									openNote = {
										Intent(context, NoteActivity::class.java).apply {
											putExtra(Extra.Companion.Constant.IS_NEW.name, false)
											putExtra(Extra.Companion.Constant.NOTE_ID.name, attachment.first.bytes)
											putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

											context.startActivity(this)
										}
									},
									onShare = { attachment.second?.share(context) }
								)
							}
						}
					}
				}
			}

			AttachmentDialog()
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
