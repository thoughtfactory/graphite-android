package com.syncodec.graphite.presentation.attachment.composable.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.core.content.FileProvider
import com.syncodec.graphite.di.model.AttachmentObject
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
import com.syncodec.graphite.utils.Quadruple
import io.realm.kotlin.types.RealmUUID
import java.io.File


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AttachmentScreen(
	noteObject : NoteObject?,
	chapterObject : ChapterObject?,
	attachmentList : List<Quadruple<AttachmentObject, File?, Uri?, RealmUUID>>,
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

					attachmentList.forEach { attachment ->
						item(
							key = attachment.hashCode()
						) {
							Box(
								modifier = Modifier.animateItemPlacement()
							) {
								AttachmentCard(
									uri = attachment.third,
									file = attachment.second,
									attachmentObject = attachment.first,
									isSelected = attachment in selectedAttachmentList,
									onClick = {
										if (isSelected) {
											if (attachment in selectedAttachmentList) selectedAttachmentList.remove(attachment)
											else selectedAttachmentList.add(attachment)
										} else {
											try {
												if (attachment.second != null) {
													Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", attachment.second)).apply {
														addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

														context.startActivity(this)
													}
												}
											} catch (e : ActivityNotFoundException) {
												Toast.makeText(context, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
											} catch (e : Exception) {
												e.printStackTrace()
												Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
											}
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
											putExtra(Extra.Companion.Constant.NOTE_ID.name, attachment.fourth.bytes)
											putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

											context.startActivity(this)
										}
									},
									onShare = {
										try {
											val sharingIntent = Intent(Intent.ACTION_SEND)
											sharingIntent.type = attachment.first.mimeType ?: "*/*"
											sharingIntent.putExtra(Intent.EXTRA_STREAM, attachment.third)

											Intent.createChooser(sharingIntent, "Share using").apply {
												addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
												context.startActivity(this)
											}
										} catch (e : Exception) {
											e.printStackTrace()
											Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
										}
									}
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
