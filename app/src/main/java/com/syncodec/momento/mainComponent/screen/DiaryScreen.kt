package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R
import com.syncodec.momento.custom.notebook.*
import com.syncodec.momento.custom.squircle.Squircle
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.filterData
import com.syncodec.momento.miscellaneous.timeStampToPrettyDay
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.todayComponent.TodayActivity
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun DiaryScreen() {
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()
	val dataStore = DataStore(context = context)

	val vaultState by viewModel.activityState.vaultState
	val showArchived by viewModel.activityState.showArchived
	val showFavourite by viewModel.activityState.showFavourite
	val showLocked by viewModel.activityState.showLocked
	var isSelected by viewModel.activityState.isSelected

	val diaryList by viewModel.defaultNoteList.observeAsState()
	val isDiaryEmpty: Boolean = diaryList?.isEmpty() ?: true

	val noteDbEntryDayMap: MutableMap<Long, MutableList<NoteDbEntry>> = mutableMapOf()

	val selectedItemList = viewModel.activityState.selectedItemList

	val calendar = Calendar.getInstance()
	diaryList
		?.forEach { diary ->
			calendar.apply {
				timeInMillis = diary.userTimestamp
				set(Calendar.MILLISECOND, 0)
				set(Calendar.SECOND, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.HOUR, 0)
			}
			if (noteDbEntryDayMap.containsKey(calendar.timeInMillis)) {
				noteDbEntryDayMap[calendar.timeInMillis]!!.add(diary)
			} else {
				noteDbEntryDayMap[calendar.timeInMillis] = mutableListOf(diary)
			}
		}

	if (isDiaryEmpty) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			if (!(showArchived || showFavourite || showLocked)) {
				QuoteCard()
				Spacer(modifier = Modifier.height(36.dp))
			} else {
				Spacer(modifier = Modifier.height(76.dp))
			}
			NoEntryCard()
		}
	} else {
		LazyColumn(
			modifier = Modifier
		) {
			item {
				AnimatedVisibility(
					visible = !(showArchived || showFavourite || showLocked),
					enter = expandVertically(tween(600)) + scaleIn(tween(600)),
					exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
				) {
					Column(modifier = Modifier.fillMaxWidth()) {
						QuoteCard()
						Spacer(modifier = Modifier.height(16.dp))
					}
				}
			}

			noteDbEntryDayMap.forEach { (day, diaryList) ->
				val filteredEntries = diaryList.filter {
					filterData(
						showArchived = showArchived,
						isArchived = it.isArchived,
						showFavourite = showFavourite,
						isFavourite = it.isFavourite,
						showLocked = showLocked,
						isLocked = it.isLocked
					)
				}

				val entrySize = filteredEntries.size
				val lastEntryKey = if (entrySize!=0) filteredEntries.last().key else null

				stickyHeader {
					AnimatedVisibility(visible = entrySize != 0) {
						NotebookHeaderCard(
							title = timeStampToPrettyDay(day),
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}"
						)
					}
				}

				diaryList.forEach { diaryDbEntry ->
					item {
						val showEntry: Boolean = filterData(
							showArchived = showArchived,
							isArchived = diaryDbEntry.isArchived,
							showFavourite = showFavourite,
							isFavourite = diaryDbEntry.isFavourite,
							showLocked = showLocked,
							isLocked = diaryDbEntry.isLocked
						)

						NoteCardData(
							timestamp = diaryDbEntry.userTimestamp,
							showFullTime = false,
							isLocked = diaryDbEntry.isLocked,
							isSelected = diaryDbEntry.key in selectedItemList,
							isArchived = diaryDbEntry.isArchived,
							isFavourite = diaryDbEntry.isFavourite,
							isDeleted = diaryDbEntry.deletedTimestamp != -1L,
							isLast = diaryDbEntry.key == lastEntryKey,
							title = diaryDbEntry.title,
							contentThumbnail = diaryDbEntry.contentThumbnail,
							attachmentCount = diaryDbEntry.attachmentCount,
							attachmentThumbnail = diaryDbEntry.attachmentThumbnail,
							address = diaryDbEntry.address,
							isVisible = showEntry,
							onClick = {
								if (isSelected) {
									isSelected = true
									if (diaryDbEntry.key in selectedItemList) {
										selectedItemList.remove(diaryDbEntry.key)
									} else {
										selectedItemList.add(diaryDbEntry.key)
									}
								} else {
									Intent(context, NoteActivity::class.java).apply {
										putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, false)
										putExtra(Konstant.Companion.Konstant.DIARY_KEY.name, diaryDbEntry.key)
										context.startActivity(this)
									}
								}
							},
							onLongClick = {
								isSelected = true
								selectedItemList.add(diaryDbEntry.key)
							},
						).apply {
							NoteCard(noteCardData = this)
						}

						NotebookTimelineSpacer(isVisible = diaryDbEntry.key != lastEntryKey && showEntry)
					}
				}
			}

			item {
				Spacer(modifier = Modifier.height(128.dp))
			}
		}
	}
}

@Preview
@ExperimentalMaterialApi
@Composable
private fun QuoteCard() {
	val context = LocalContext.current
	Box(
		modifier = Modifier
			.height(80.dp)
			.fillMaxWidth()
			.padding(12.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp))
			.clickable { context.startActivity(Intent(context, TodayActivity::class.java)) },
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
		) {
			Image(
				painter = painterResource(id = R.drawable.background),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.blur(8.dp, BlurredEdgeTreatment.Rectangle),
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Squircle(
					sizeInDp = 56.dp,
					smoothing = 6.0,
				) {
					Image(
						painter = painterResource(id = R.drawable.background),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.31f), BlendMode.SrcOver)
					)

					Column(
						modifier = Modifier
							.fillMaxSize(),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					) {
						Text(
							text = "04",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							color = Color.White,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)

						Text(
							text = "May",
							style = MaterialTheme.typography.bodySmall,
							color = Color.White,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)
					}

				}

				Spacer(modifier = Modifier.padding(8.dp))

				Column(
					modifier = Modifier
						.height(48.dp)
						.padding(0.dp, 0.dp, 16.dp, 0.dp),
					verticalArrangement = Arrangement.Center
				) {
					Text(
						text = "En Quote",
						style = MaterialTheme.typography.bodyLarge,
						color = Color.White,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)

					Text(
						text = "Looking down the misty path to uncertain destinations",
						style = MaterialTheme.typography.bodySmall,
						fontStyle = FontStyle.Italic,
						color = Color.White,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)
				}
			}
		}
	}
}
