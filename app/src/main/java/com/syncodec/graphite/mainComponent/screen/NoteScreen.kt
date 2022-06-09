package com.syncodec.graphite.mainComponent.screen

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.custom.notebook.NoEntryCard
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookHeaderCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.custom.squircle.Squircle
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.quote.QuoteDbEntry
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.miscellaneous.TimeUtils
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.timeStampToPrettyDay
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import kotlin.random.Random


@OptIn(
	ExperimentalFoundationApi::class,
)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NoteScreen(
	noteMap: Map<String, NoteDbEntry>,
	selectedItemList: List<String>,
	isFilterActive: Boolean,
	quote: QuoteDbEntry?,
	quoteBg: Drawable?,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val isPremium = PremiumCompositionLocal.current
	val showMembershipCardProb = remember { Random.nextDouble() }
	var showMembershipCard by remember { mutableStateOf(false) }
	var showQuoteCard by remember { mutableStateOf(false) }
	LaunchedEffect(key1 = null) {
		withContext(Dispatchers.IO) {
			delay(1600)
			showMembershipCard = showMembershipCardProb > 0.25
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

	Crossfade(targetState = noteMap.isEmpty()) {
		if (it) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				QuoteCard(
					quote = quote,
					quoteBg = quoteBg,
					showCard = showQuoteCard && !isFilterActive
				) { onAction(MainActivity.Action.EN_QUOTE, null) }
				Spacer(modifier = Modifier.height(8.dp))

				NoEntryCard()
			}
		} else {
			LazyColumn(
				modifier = Modifier
			) {
				item {
					Column(modifier = Modifier.fillMaxWidth()) {
						QuoteCard(
							quote = quote,
							quoteBg = quoteBg,
							showCard = showQuoteCard && !isFilterActive
						) { onAction(MainActivity.Action.EN_QUOTE, null) }
						if (showMembershipCard && !isFilterActive) {
							Spacer(modifier = Modifier.height(8.dp))
						}
						MembershipCard(
							showCard = showMembershipCard && !isFilterActive && !isPremium,
							onAction = onAction
						)
						Spacer(modifier = Modifier.height(8.dp))
					}
				}

				noteDbEntryDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
					val sortedList = noteList.sortedBy { it.userTimestamp }.reversed()

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
								isVisible = true,
								selectedColor = MaterialTheme.colorScheme.surface,
								onClick = { onAction(MainActivity.Action.CLICK_NOTE, noteDbEntry.key) },
								onLongClick = {
									onAction(MainActivity.Action.LONG_CLICK_NOTE, noteDbEntry.key)
								},
							)

							NotebookTimelineSpacer(isVisible = noteDbEntry.key != lastEntryKey && true)
						}
					}
				}

				item { Spacer(modifier = Modifier.height(128.dp)) }
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@Composable
private fun QuoteCard(
	quote: QuoteDbEntry?,
	quoteBg: Drawable?,
	showCard: Boolean,
	onClick: () -> Unit
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
				.clickable { onClick() },
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
			) {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(quoteBg)
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
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
						AsyncImage(
							model = ImageRequest.Builder(context)
								.data(quoteBg)
								.crossfade(300)
								.build(),
							placeholder = null,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							colorFilter = ColorFilter.tint(
								Color.Black.copy(alpha = 0.47f),
								BlendMode.SrcOver
							),
							modifier = Modifier.fillMaxSize(),
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
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
			containerColor = Color.Transparent,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp),
			onClick = { onAction(MainActivity.Action.TRY_PREMIUM, null) }
		) {
			Text(
				text = "Add some style in your notes with premium",
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(12.dp)
			)

//			Row(
//				modifier = Modifier
//					.fillMaxWidth()
//					.padding(12.dp),
//				verticalAlignment = Alignment.CenterVertically
//			) {
//				Text(
//					text = "Add some style in your notes",
//					style = MaterialTheme.typography.bodyLarge,
//					color = MaterialTheme.colorScheme.onBackground,
//					modifier = Modifier.weight(1f)
//				)
//
//				Spacer(modifier = Modifier.width(32.dp))
//
//				Button(
//					onClick = { onAction(MainActivity.Action.TRY_PREMIUM, null) }
//				) {
//					Text(
//						text = "Try premium",
//						style = MaterialTheme.typography.bodyLarge,
//					)
//				}
//			}
		}
	}
}
