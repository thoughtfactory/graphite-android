package com.syncodec.graphite.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.MainActivity
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.custom.notebook.*
import com.syncodec.graphite.custom.squircle.Squircle
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.quote.QuoteDbEntry
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.miscellaneous.DataStore
import com.syncodec.graphite.miscellaneous.TimeUtils
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.timeStampToPrettyDay
import com.syncodec.graphite.miscellaneous.filterData
import com.syncodec.graphite.todayComponent.TodayActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.io.File
import kotlin.random.Random


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
	selectedItemList: List<String>,
	filterTag: List<String>,
	quote: QuoteDbEntry?,
	quoteBg: File?,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)

	val vaultState = Graphite.Companion.VaultState.OPENED
	val showArchived = false
	val showFavourite = false
	val showLocked = false

	val showMembershipCardProb = remember { Random.nextDouble() }
	var showMembershipCard by remember { mutableStateOf(false) }
	var showQuoteCard by remember { mutableStateOf(false) }
	LaunchedEffect(key1 = null) {
		withContext(Dispatchers.IO) {
			delay(1600)
			showMembershipCard = showMembershipCardProb > 0
		}
		withContext(Dispatchers.IO) {
			delay(1200)
			showQuoteCard = true
		}
	}

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
			Spacer(modifier = Modifier.height(8.dp))
			QuoteCard(
				quote = quote,
				quoteBg = quoteBg,
				showCard = showQuoteCard && !(showArchived || showFavourite || showLocked)
			)
			Spacer(modifier = Modifier.height(8.dp))
			MembershipCard(
				showCard = showMembershipCard && !(showArchived || showFavourite || showLocked),
				onAction = onAction
			)
			Spacer(modifier = Modifier.height(36.dp))

			NoEntryCard()
		}
	} else {
		LazyColumn(
			modifier = Modifier
		) {
			item {
				Column(modifier = Modifier.fillMaxWidth()) {
					if (showQuoteCard && quote != null && !(showArchived || showFavourite || showLocked)) {
						Spacer(modifier = Modifier.height(8.dp))
					}
					QuoteCard(
						quote = quote,
						quoteBg = quoteBg,
						showCard = showQuoteCard && !(showArchived || showFavourite || showLocked)
					)
					if (showMembershipCard && !(showArchived || showFavourite || showLocked)) {
						Spacer(modifier = Modifier.height(8.dp))
					}
					MembershipCard(
						showCard = showMembershipCard && !(showArchived || showFavourite || showLocked),
						onAction = onAction
					)
					Spacer(modifier = Modifier.height(8.dp))
				}
			}

			noteDbEntryDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
				val sortedList = noteList.filter {
					filterData(
						showArchived = showArchived,
						isArchived = false,
						showFavourite = showFavourite,
						isFavourite = false,
						showLocked = showLocked,
						isLocked = false
					)
				}.sortedBy { it.userTimestamp }.reversed()

				val entrySize = sortedList.size

				stickyHeader {
					AnimatedVisibility(visible = entrySize != 0) {
						NotebookHeaderCard(
							title = timeStampToPrettyDay(day),
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
							color = MaterialTheme.colorScheme.background
						)
					}
				}

				val lastEntryKey = if (entrySize != 0) sortedList.last().key else null

				sortedList.forEach { noteDbEntry ->
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
							onClick = { onAction(MainActivity.Action.CLICK_NOTE, noteDbEntry.key) },
							onLongClick = {
								onAction(MainActivity.Action.LONG_CLICK_NOTE, noteDbEntry.key)
							},
						)

						NotebookTimelineSpacer(isVisible = noteDbEntry.key != lastEntryKey && showEntry)
					}
				}
			}

			item { Spacer(modifier = Modifier.height(128.dp)) }
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@Composable
private fun QuoteCard(
	quote: QuoteDbEntry?,
	quoteBg: File?,
	showCard: Boolean
) {
	val context = LocalContext.current

	val date = remember { DateTime.now() }
	val d = remember { date.dayOfMonth }
	val m = remember { date.monthOfYear }

	AnimatedVisibility(
		visible = showCard && quote != null,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
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
					painter = rememberImagePainter(
						data = quoteBg,
						builder = { crossfade(300) }
					),
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
							painter = rememberImagePainter(
								data = quoteBg,
								builder = { crossfade(300) }
							),
							contentDescription = null,
							contentScale = ContentScale.Crop,
							colorFilter = ColorFilter.tint(
								Color.Black.copy(alpha = 0.31f),
								BlendMode.SrcOver
							)
						)

						Column(
							modifier = Modifier
								.fillMaxSize(),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.Center
						) {
							Text(
								text = d.toString().padStart(2, '0'),
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold,
								color = Color.White,
								overflow = TextOverflow.Ellipsis,
								maxLines = 1
							)

							Text(
								text = Konstant.monthNameShort[m - 1],
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
							text = "${quote?.quote}",
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun MembershipCard(
	showCard: Boolean,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	AnimatedVisibility(
		visible = showCard,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp),
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
			containerColor = Color.Transparent
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Want to add style to your notes?",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.weight(1f)
				)

				Button(
					onClick = { onAction(MainActivity.Action.TRY_PREMIUM, null) }
				) {
					Text(
						text = "Try premium",
						style = MaterialTheme.typography.bodyLarge,
					)
				}
			}
		}
	}
}
