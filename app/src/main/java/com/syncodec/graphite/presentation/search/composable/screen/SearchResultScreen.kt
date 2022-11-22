package com.syncodec.graphite.presentation.search.composable.screen

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
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
		EmptyView()
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
							if (noteObject.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) thumbnail = noteObject.thumbnail?.decodeBase64ToBitmap()
						} catch (e : Exception) {
							e.printStackTrace()
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
						attachmentCount = noteObject.attachmentList.size,
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
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, noteObject.parentChapterId?.bytes)
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

@Composable
private fun EmptyView() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Image(
			painter = painterResource(id = R.drawable.il_searching),
			contentDescription = "No entries found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "No entries found",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
	}
}
