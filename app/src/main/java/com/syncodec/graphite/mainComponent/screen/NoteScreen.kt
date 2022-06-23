package com.syncodec.graphite.mainComponent.screen

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.animation.AnimatedText
import com.syncodec.graphite.custom.notebook.NoEntryCard
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookHeaderCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
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
import java.util.*
import kotlin.Comparator
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
				YearProgressCard()
				Spacer(modifier = Modifier.height(8.dp))

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
						YearProgressCard()
						Spacer(modifier = Modifier.height(8.dp))
						QuoteCard(
							quote = quote,
							quoteBg = quoteBg,
							showCard = showQuoteCard && !isFilterActive
						) { onAction(MainActivity.Action.EN_QUOTE, null) }

						if (showMembershipCard && !isFilterActive) {
							Spacer(modifier = Modifier.height(8.dp))
							MembershipCard(
								showCard = showMembershipCard && !isFilterActive && !isPremium,
								onAction = onAction
							)
						}
						Spacer(modifier = Modifier.height(8.dp))
					}
				}

				noteDbEntryDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
					val sortedList = noteList.sortedBy { it.userTimestamp }.reversed()

					val entrySize = sortedList.size

					stickyHeader {
						AnimatedVisibility(visible = entrySize != 0) {
							NotebookHeaderCard(
								title = day.timeStampToPrettyDay(),
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
					Box(
						modifier = Modifier
							.aspectRatio(1f)
							.clip(RoundedCornerShape(12.dp))
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
@Preview
@Composable
private fun YearProgressCard(
	showCard: Boolean = true,
) {
	var startAnimation by remember { mutableStateOf(false) }
	val calendar = remember { Calendar.getInstance() }
	val totalDays = remember { if (calendar.get(Calendar.YEAR) % 4 == 0) 366 else 365 }
	val progress by animateIntAsState(
		targetValue = if (startAnimation) calendar.get(Calendar.DAY_OF_YEAR) else 0,
		animationSpec = tween(2400)
	)

	LaunchedEffect(key1 = startAnimation) { startAnimation = true }

	AnimatedVisibility(
		visible = showCard,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Card(
			colors = CardDefaults.cardColors(Color.Transparent),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 8.dp, 12.dp, 4.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = calendar.get(Calendar.YEAR).toString(),
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp,
					lineHeight = 24.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onBackground
				)

				Spacer(modifier = Modifier.width(12.dp))

				Box(
					modifier = Modifier
						.weight(1f)
						.clip(RoundedCornerShape(50))
						.background(Color.Transparent)
						.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(progress.toFloat() / totalDays)
							.height(12.dp)
							.background(MaterialTheme.colorScheme.primary)
					)
				}

				Spacer(modifier = Modifier.width(12.dp))

				AnimatedText(
					animatedText = (progress.toFloat() * 100/ totalDays).toInt().toString(),
					staticText = "%",
					color = MaterialTheme.colorScheme.onBackground
				)
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
		OutlinedCard(
			colors = CardDefaults.outlinedCardColors(
				containerColor = Color.Transparent
			),
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
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
		}
	}
}
