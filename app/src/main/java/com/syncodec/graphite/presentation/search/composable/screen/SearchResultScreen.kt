package com.syncodec.graphite.presentation.search.composable.screen

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.utils.AttachmentType
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Composable
fun SearchResultScreen(
	visibleNote : List<NoteObject>,
	tagList: List<TagObject>,
) {
	val context = LocalContext.current

	val isVaultOpened = LocalVaultIsOpened.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current
	val onSelect = LocalCompositionOnSelect.current

	val _visibleNote = visibleNote.filter { if (it.isLocked) isVaultOpened else true }

	if (_visibleNote.isEmpty()) {
		EmptyView(
			image = R.drawable.il_searching,
			title = "Oops!! We couldn't find anything.",
		)
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 0.dp)
		) {
			_visibleNote.forEach { noteObject ->

				item {
					var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

					LaunchedEffect(key1 = noteObject.id.hashCode() + noteObject.thumbnail.hashCode()) {
						try {
							if (noteObject.thumbnailType == AttachmentType.IMAGE.name.lowercase()) thumbnail = noteObject.thumbnail?.decodeBase64ToBitmap()
						} catch (e : Exception) {
//							e.printStackTrace()
						}
					}

					NoteListCard(
						id = noteObject.id,
						timestamp = noteObject.userTimestamp,
						isLocked = noteObject.isLocked,
						isSelected = noteObject.id in selectedRealmUUIDList,
						isFavourite = noteObject.isFavourite,
						isLast = false,
						title = noteObject.title,
						contentThumbnail = noteObject.contentThumbnail,
						attachmentThumbnail = thumbnail,
						address = noteObject.address,
						latLng = noteObject.getLatLng(),
						tagList = tagList.filter { noteObject.id in it.objectIdList },
						isVisible = true,
						selectedColor = MaterialTheme.colorScheme.surface,
						onClick = {
							if (isSelected) {
								if (selectedRealmUUIDList.contains(noteObject.id)) selectedRealmUUIDList.remove(noteObject.id)
								else selectedRealmUUIDList.add(noteObject.id)
							} else {
								Intent(context, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Constant.IS_NEW.name, false)
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, noteObject.parentId?.bytes)
									putExtra(Extra.Companion.Constant.NOTE_ID.name, noteObject.id?.bytes)
									putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

									context.startActivity(this)
								}
							}
						},
					) {
						onSelect(true)
						if (selectedRealmUUIDList.contains(noteObject.id)) selectedRealmUUIDList.remove(noteObject.id)
						else selectedRealmUUIDList.add(noteObject.id)
					}
				}
			}
		}
	}
}
