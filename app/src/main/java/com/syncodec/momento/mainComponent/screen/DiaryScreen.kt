package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
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
import com.syncodec.momento.custom.entry.EntryCard
import com.syncodec.momento.custom.entry.EntryHeaderCard
import com.syncodec.momento.custom.entry.EntryTimelineSpacer
import com.syncodec.momento.custom.entry.NoEntryCard
import com.syncodec.momento.custom.squircle.Squircle
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.miscellaneous.filterData
import com.syncodec.momento.miscellaneous.timeStampToPrettyDay
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.todayComponent.TodayActivity
import org.joda.time.LocalDateTime
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun DiaryScreen() {
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()

	val vaultState by viewModel.activityState.vaultState
	val showArchived by viewModel.activityState.showArchived
	val showFavourite by viewModel.activityState.showFavourite
	val showLocked by viewModel.activityState.showLocked
	var isSelected by viewModel.activityState.isSelected

	val diaryList by viewModel.diaryRepository.diaryDbEntryListLiveData.observeAsState()
	val isDiaryEmpty: Boolean = diaryList?.isEmpty() ?: true

	val diaryDbEntryDayMap: MutableMap<Long, MutableList<DiaryDbEntry>> = mutableMapOf()

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
			if (diaryDbEntryDayMap.containsKey(calendar.timeInMillis)) {
				diaryDbEntryDayMap[calendar.timeInMillis]!!.add(diary)
			} else {
				diaryDbEntryDayMap[calendar.timeInMillis] = mutableListOf(diary)
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
					enter = expandIn(),
					exit = shrinkOut()
				) {
					Column(modifier = Modifier.fillMaxWidth()) {
						QuoteCard()
						Spacer(modifier = Modifier.height(16.dp))
					}
				}
			}

			diaryDbEntryDayMap.forEach { (day, diaryList) ->
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
				val lastEntryKey = if (entrySize!=0) filteredEntries.last().primaryKey else null

				stickyHeader {
					AnimatedVisibility(visible = entrySize != 0) {
						EntryHeaderCard(
							title = timeStampToPrettyDay(day),
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}"
						)
					}
				}

				diaryList.forEach { diaryDbEntry ->
					item {
						val tint = MaterialTheme.colorScheme.secondaryContainer

						val showEntry: Boolean = filterData(
							showArchived = showArchived,
							isArchived = diaryDbEntry.isArchived,
							showFavourite = showFavourite,
							isFavourite = diaryDbEntry.isFavourite,
							showLocked = showLocked,
							isLocked = diaryDbEntry.isLocked
						)

						EntryCard(
							timestamp = diaryDbEntry.userTimestamp,
							isLocked = diaryDbEntry.isLocked,
							isSelected = diaryDbEntry.primaryKey in selectedItemList,
							isArchived = diaryDbEntry.isArchived,
							isFavourite = diaryDbEntry.isFavourite,
							isDeleted = diaryDbEntry.deletedTimestamp != -1L,
							isLast = diaryDbEntry.primaryKey == lastEntryKey,
							title = diaryDbEntry.title,
							contentThumbnail = diaryDbEntry.contentThumbnail,
							attachmentCount = diaryDbEntry.attachmentCount,
							attachmentThumbnail = diaryDbEntry.attachmentThumbnail,
							address = diaryDbEntry.address,
							isVisible = showEntry,
							tint = tint,
							onClick = {
								if (isSelected) {
									isSelected = true
									if (diaryDbEntry.primaryKey in selectedItemList) {
										selectedItemList.remove(diaryDbEntry.primaryKey)
									} else {
										selectedItemList.add(diaryDbEntry.primaryKey)
									}
								} else {
									Intent(context, NoteActivity::class.java).apply {
										putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, false)
										putExtra(Konstant.Companion.Konstant.DIARY_KEY.name, diaryDbEntry.primaryKey)
										context.startActivity(this)
									}
								}
							},
							onLongClick = {
								isSelected = true
								selectedItemList.add(diaryDbEntry.primaryKey)
							},
						).apply {
							EntryCard(entryCard = this)
						}

						EntryTimelineSpacer(
							tint = tint,
							isVisible = diaryDbEntry.primaryKey != lastEntryKey && showEntry
						)
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
@Composable
private fun ProgressCard() {
	val localDateTime = LocalDateTime.now()
	val isLeapYear = ((localDateTime.year % 4 == 0) && (localDateTime.year % 100 != 0)) || (localDateTime.year % 400 == 0)
	val totalTime = 24 * 60 * if (isLeapYear) 366 else 365
	val currentTime = (localDateTime.dayOfYear * 24 * 60) + (localDateTime.hourOfDay * 60) + localDateTime.minuteOfHour

	var yearProgressValue by remember { mutableStateOf(0f) }
	val yearProgressAnimation by animateFloatAsState(
		targetValue = yearProgressValue,
		animationSpec = FloatTweenSpec(
			1600, 400, FastOutSlowInEasing
		)
	)

	SideEffect { yearProgressValue = (currentTime.toFloat() / totalTime) }

	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.height(80.dp)
			.fillMaxWidth()
			.padding(12.dp, 8.dp, 12.dp, 4.dp)
	) {
		Column(
			horizontalAlignment = Alignment.Start,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Text(
				text = "Year in progress ${localDateTime.year}",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.primary
			)

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Card(
					elevation = 0.dp,
					shape = RoundedCornerShape(4.dp),
					backgroundColor = Color.LightGray,
					modifier = Modifier
						.weight(1f)
						.height(6.dp),
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.height(6.dp),
					) {
						Card(
							elevation = 0.dp,
							shape = RoundedCornerShape(4.dp),
							backgroundColor = MaterialTheme.colorScheme.primary,
							modifier = Modifier
								.fillMaxWidth(yearProgressAnimation)
								.height(6.dp),
						) {

						}
					}
				}

				Text(
					text = String.format("%.2f%%", yearProgressAnimation * 100),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.padding(16.dp, 0.dp, 0.dp, 0.dp)
				)
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
