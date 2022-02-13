package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.MockDiaryDbEntry
import com.syncodec.momento.database.diary.MockDiaryDbEntryList
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.miscellaneous.base64stringToBitmap
import com.syncodec.momento.miscellaneous.timeStampToPrettyDay
import com.syncodec.momento.miscellaneous.timeStampToTime
import com.syncodec.momento.todayComponent.TodayActivity
import org.joda.time.LocalDateTime
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun DiaryScreen() {
	val viewModel: MainViewModel = viewModel()

	val diaryList by viewModel.diaryRepository.diaryDbEntryListLiveData.observeAsState()
	val diaryDbEntryDayMap: MutableMap<Long, MutableList<DiaryDbEntry>> = mutableMapOf()

	val calendar = Calendar.getInstance()
	diaryList?.forEach { diary ->
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

	LazyColumn(
		modifier = Modifier
	) {
//			item {
//				Spacer(modifier = Modifier.height(4.dp))
//				ProgressCard(
//					modifier = Modifier
//						.fillMaxWidth()
//						.padding(12.dp, 8.dp, 12.dp, 4.dp)
//				)
//			}

		item {
			Spacer(modifier = Modifier.height(8.dp))
			QuoteCard()
			Spacer(modifier = Modifier.height(16.dp))
		}

		diaryDbEntryDayMap.forEach { (day, diaryList) ->
			stickyHeader {
				DayHeaderCard(
					title = timeStampToPrettyDay(day),
					noEntries = diaryList.size
				)
			}

			item {
				DiaryDayCard(
					diaryList = diaryList
				)
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

@ExperimentalMaterialApi
@Composable
private fun QuoteCard() {
	val context = LocalContext.current
	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(20.dp),
		modifier = Modifier
			.height(80.dp)
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
		onClick = { context.startActivity(Intent(context, TodayActivity::class.java)) }
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
					.padding(16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.requiredSize(56.dp)
						.clip(RoundedCornerShape(12.dp)),
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

@Composable
private fun DayHeaderCard(
	title: String,
	noEntries: Int
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Row(
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier
				.fillMaxWidth()
				.padding(14.dp, 0.dp, 12.dp, 12.dp)
		) {
			Box(
				modifier = Modifier
					.width(4.dp)
					.height(32.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(MaterialTheme.colorScheme.primary)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = title,
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.bodyLarge
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				text = "$noEntries entries",
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.bodyMedium,
			)
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun DiaryDayCard(
	@PreviewParameter(MockDiaryDbEntryList::class)
	diaryList: List<DiaryDbEntry>
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(0.dp, 0.dp, 8.dp, 0.dp),
		backgroundColor = MaterialTheme.colorScheme.background,
		shape = RoundedCornerShape(12.dp),
		elevation = 0.dp
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(0.dp))
			diaryList.forEachIndexed { index, diaryDbEntry ->
				val tint = MaterialTheme.colorScheme.secondaryContainer
				DiaryCard(
					diaryDbEntry = diaryDbEntry,
					isFirst = index == 0,
					isLast = index == diaryList.size - 1,
					tint = tint
				)
				if (index != diaryList.size - 1) {
					DiaryDaySpacer(tint = tint)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun DiaryCard(
	@PreviewParameter(MockDiaryDbEntry::class)
	diaryDbEntry: DiaryDbEntry,
	isFirst: Boolean = false,
	isLast: Boolean = false,
	tint: Color = Color.LightGray
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(if (isLast) 152.dp else 144.dp)
			.padding(8.dp, 0.dp, 8.dp, if (isLast) 8.dp else 0.dp),
	) {
		DiarySpacer(
			isFirst = isFirst,
			isLast = isLast,
			tint = tint
		)
		Spacer(modifier = Modifier.width(4.dp))
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = MaterialTheme.colorScheme.background,
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
			modifier = Modifier
				.fillMaxSize(),
			onClick = {}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp, 8.dp, 12.dp, 8.dp)
			) {
				Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {

					Text(
						text = timeStampToTime(diaryDbEntry.userTimestamp),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)

					Spacer(modifier = Modifier.weight(1f))

					Icon(
						painter = painterResource(id = R.drawable.ic_lock_3),
//						imageVector = TablerIcons.LockOff,
						contentDescription = "Locked",
//						tint = Color.Unspecified,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(14.dp)
					)

					Spacer(modifier = Modifier.width(2.dp))

					Text(
						text = "·",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)

					Spacer(modifier = Modifier.width(2.dp))

					Icon(
						painter = painterResource(id = R.drawable.ic_archive_3),
//						imageVector = TablerIcons.Archive,
						contentDescription = "Locked",
//						tint = Color.Unspecified,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(14.dp)
					)

					Spacer(modifier = Modifier.width(2.dp))

					Text(
						text = "·",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)

					Spacer(modifier = Modifier.width(2.dp))

					Icon(
						painter = painterResource(id = R.drawable.ic_pin_3),
//						imageVector = TablerIcons.Pin,
						contentDescription = "Locked",
//						tint = Color.Unspecified,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(14.dp)
					)

					Spacer(modifier = Modifier.width(2.dp))

					Text(
						text = "·",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)

					Spacer(modifier = Modifier.width(2.dp))

					Icon(
						painter = painterResource(id = R.drawable.ic_heart_3),
//						imageVector = TablerIcons.Heart,
						contentDescription = "Locked",
//						tint = Color.Unspecified,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(14.dp)
					)
				}

				Spacer(modifier = Modifier.height(8.dp))

				if (diaryDbEntry.attachmentThumbnail==null) {
					Text(
						text = "${diaryDbEntry.contentThumbnail}",
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 4,
						modifier = Modifier
							.height(80.dp)
					)
				} else {
					Row(
						modifier = Modifier
							.fillMaxWidth()
					) {
						Text(
							text = "${diaryDbEntry.contentThumbnail}",
							style = MaterialTheme.typography.bodyMedium,
							maxLines = 4,
							modifier = Modifier
								.weight(1f)
						)
						Card(
							modifier = Modifier
								.requiredSize(80.dp),
							elevation = 0.dp,
							shape = RoundedCornerShape(12.dp)
						) {
							Image(
								painter = rememberImagePainter(data = diaryDbEntry.attachmentThumbnail!!.base64stringToBitmap()),
								contentDescription = null,
								modifier = Modifier
									.fillMaxSize()
							)
						}
					}
				}

				Spacer(modifier = Modifier.weight(1f))

				if (diaryDbEntry.address != null) {
					Row(
						modifier = Modifier
							.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_location_pin_3),
//							imageVector = TablerIcons.MapPin,
							contentDescription = null,
//							tint = Color.Unspecified,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier
								.aspectRatio(1f)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "${diaryDbEntry.address}",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							fontStyle = FontStyle.Italic,
							color = MaterialTheme.colorScheme.primary,
							maxLines = 1,
							modifier = Modifier
						)
					}
				}
			}

			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.CenterEnd
			) {
				Box(
					modifier = Modifier
						.width(8.dp)
						.height(80.dp)
						.clip(CutCornerShape(16.dp, 0.dp, 0.dp, 16.dp))
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
			}
		}
	}
}

@Composable
private fun DiaryDaySpacer(
	tint: Color
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(10.dp)
			.padding(8.dp, 0.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.width(16.dp)
				.height(10.dp),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.width(4.dp)
					.fillMaxHeight()
					.background(tint)
			)
		}
	}
}

@Composable
private fun DiarySpacer(
	isFirst: Boolean = false,
	isLast: Boolean = false,
	tint: Color
) {
	Box(
		modifier = Modifier
			.width(16.dp)
			.fillMaxHeight()
			.background(Color.Transparent)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Box(
				modifier = Modifier
					.width(4.dp)
					.height(33.dp)
					.background(if (isFirst) Color.Transparent else tint)
			)
			Box(
				modifier = Modifier
					.width(16.dp)
					.height(16.dp)
					.padding(2.dp)
					.clip(CircleShape)
					.background(tint)
			)
			Box(
				modifier = Modifier
					.width(4.dp)
					.fillMaxHeight()
					.background(if (isLast) Color.Transparent else tint)
			)
		}
	}
}
