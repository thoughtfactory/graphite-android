package com.syncodec.graphite.presentation.search.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.utils.LocalVaultIsOpened


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchResultScreen(
	visibleNote : List<NoteObject>,
) {

	val isVaultOpened = LocalVaultIsOpened.current

	val _visibleNote = visibleNote.filter { if (it.isLocked) isVaultOpened else true }

	AnimatedContent(
		targetState = _visibleNote,
		transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
	) {
		if (it.isEmpty()) {
			EmptyView()
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
				contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 0.dp)
			) {
				it.forEach { _noteObject ->
					val noteObject = _noteObject.toLite()
					try {
						item {
							NoteListCard(
								id = noteObject.id,
								timestamp = noteObject.userTimestamp,
								showFullTime = true,
								isLocked = noteObject.isLocked,
								isSelected = false,
								isFavourite = noteObject.isFavourite,
								isDeleted = false,
								isLast = false,
								title = noteObject.title,
								contentThumbnail = noteObject.contentThumbnail,
								attachmentCount = noteObject.attachmentCount,
								attachmentThumbnail = null,
								address = noteObject.address,
								latLng = noteObject.latLng,
								tagList = listOf(),
								isVisible = true,
								selectedColor = MaterialTheme.colorScheme.surface,
								onClick = {  },
								onLongClick = {  },
							)
						}
					} catch (e: Exception) {

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
