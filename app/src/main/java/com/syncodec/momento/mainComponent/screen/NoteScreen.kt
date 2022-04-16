package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.notebook.*
import com.syncodec.momento.custom.squircle.Squircle
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.TimeUtils
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.timeStampToPrettyDay
import com.syncodec.momento.miscellaneous.filterData
import com.syncodec.momento.todayComponent.TodayActivity


@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalFoundationApi::class,
	ExperimentalAnimationApi::class
)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NoteScreen(
	noteMap: Map<String, NoteDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	filterTag: List<String>,
	onClick: (MainActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)

	val vaultState = Momento.Companion.VaultState.OPENED
	val showArchived = false
	val showFavourite = false
	val showLocked = false

	val noteDbEntryDayMap: MutableMap<Long, MutableList<NoteDbEntry>> = mutableMapOf()
	noteMap.forEach { (_, note) ->
		val timestamp = TimeUtils.timestampToCalendarDay(note.userTimestamp)
		if (noteDbEntryDayMap.containsKey(timestamp)) noteDbEntryDayMap[timestamp]!!.add(note)
		else noteDbEntryDayMap[timestamp] = mutableListOf(note)
	}

	if (noteMap.isEmpty()) {
		Column(
			modifier = Modifier.fillMaxSize()
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

			noteDbEntryDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
				val filteredEntries = noteList.filter {
					filterData(
						showArchived = showArchived,
						isArchived = false,
						showFavourite = showFavourite,
						isFavourite = false,
						showLocked = showLocked,
						isLocked = false
					)
				}

				val entrySize = filteredEntries.size
				val lastEntryKey = if (entrySize != 0) filteredEntries.last().key else null

				stickyHeader {
					AnimatedVisibility(visible = entrySize != 0) {
						NotebookHeaderCard(
							title = timeStampToPrettyDay(day),
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
							color = MaterialTheme.colorScheme.background
						)
					}
				}

				noteList.forEach { noteDbEntry ->
					item {
						val showEntry: Boolean = filterData(
							showArchived = showArchived,
							isArchived = false,
							showFavourite = showFavourite,
							isFavourite = false,
							showLocked = showLocked,
							isLocked = false
						)

						NoteCard(
							key = noteDbEntry.key,
							timestamp = noteDbEntry.userTimestamp,
							showFullTime = false,
							isLocked = noteDbEntry.isLocked,
							isSelected = noteDbEntry.key in selectedItemList,
							isArchived = noteDbEntry.isArchived,
							isFavourite = noteDbEntry.isFavourite,
							isDeleted = noteDbEntry.deletedTimestamp != -1L,
							isLast = noteDbEntry.key == lastEntryKey,
							title = noteDbEntry.title,
							contentThumbnail = noteDbEntry.contentThumbnail,
							attachmentCount = noteDbEntry.attachmentKeyList.size,
							attachmentThumbnail = noteDbEntry.attachmentThumbnail,
							address = noteDbEntry.address,
							latLng = noteDbEntry.latLng,
							isVisible = showEntry,
							selectedColor = MaterialTheme.colorScheme.surface,
							onClick = { onClick(MainActivity.Action.CLICK_NOTE, noteDbEntry.key) },
							onLongClick = {
								onClick(MainActivity.Action.LONG_CLICK_NOTE, noteDbEntry.key)
							},
						)

						NotebookTimelineSpacer(isVisible = noteDbEntry.key != lastEntryKey && showEntry)
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
//			Image(
//				painter = painterResource(id = R.drawable.background_1),
//				contentDescription = null,
//				contentScale = ContentScale.Crop,
//				modifier = Modifier
//					.fillMaxWidth()
//					.fillMaxHeight()
//					.blur(8.dp, BlurredEdgeTreatment.Rectangle),
//			)

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
//					Image(
//						painter = painterResource(id = R.drawable.background_1),
//						contentDescription = null,
//						contentScale = ContentScale.Crop,
//						colorFilter = ColorFilter.tint(
//							Color.Black.copy(alpha = 0.31f),
//							BlendMode.SrcOver
//						)
//					)

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
