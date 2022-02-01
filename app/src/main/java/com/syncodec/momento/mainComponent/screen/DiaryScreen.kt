package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.miscellaneous.timeStampToPrettyDay
import com.syncodec.momento.todayComponent.TodayActivity
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import org.joda.time.LocalDateTime
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun DiaryScreen() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()

	val diaryList by viewModel.diaryRepository.diaryDbEntryListLiveData.observeAsState()
	val diaryDbEntryDayMap: MutableMap<Long, MutableList<DiaryDbEntry>> = mutableMapOf()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	val scaffoldScale by animateFloatAsState(
		targetValue = if (viewModel.mainActivityState.bottomSheetState.progress.to == ModalBottomSheetValue.Hidden) 1f else 0.95f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioHighBouncy,
			stiffness = Spring.StiffnessMedium
		),
	)

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

	collapsingToolbarScaffoldState.toolbarState.progress

	CollapsingToolbarScaffold(
		state = collapsingToolbarScaffoldState,
		scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
		modifier = Modifier,
		toolbar = {
			MainTopBar(openSheet = openSheet)
			Image(
				painter = painterResource(id = R.drawable.home_background2),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.height(256.dp)
					.blur(4.dp)
					.parallax(0.4f)
			)
		}
	) {
		LazyColumn(
			modifier = Modifier
				.graphicsLayer {
					this.scaleX = scaffoldScale
					this.scaleY = scaffoldScale
				}
		) {
			item {
				Spacer(modifier = Modifier.height(4.dp))
				ProgressCard(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 8.dp, 12.dp, 4.dp)
				)
			}

			item {
				Spacer(modifier = Modifier.height(4.dp))
				QuoteCard(
					elevation = 8.dp,
					shape = RoundedCornerShape(12.dp),
					modifier = Modifier
						.height(80.dp)
						.fillMaxWidth()
						.padding(12.dp, 8.dp, 12.dp, 4.dp)
				)
			}

			diaryDbEntryDayMap.forEach { (day, diaryList) ->
				stickyHeader {
					GroupHeaderCard(
						title = timeStampToPrettyDay(day),
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp, 16.dp, 12.dp, 4.dp)
					)
				}

				diaryList.forEach { diary ->
					item {
						DiaryCard(
							diaryDbEntry = diary,
							modifier = Modifier
								.fillMaxWidth()
								.height(128.dp)
								.padding(12.dp, 8.dp, 12.dp, 4.dp)
								.animateItemPlacement(
									animationSpec = tween(
										durationMillis = 800,
										easing = FastOutSlowInEasing
									)
								)
						)
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun ProgressCard(
	modifier: Modifier = Modifier
		.height(80.dp)
		.fillMaxWidth()
		.padding(12.dp, 8.dp, 12.dp, 4.dp)
) {
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
		modifier = modifier
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
private fun QuoteCard(
	modifier: Modifier = Modifier,
	elevation: Dp = 0.dp,
	shape: Shape = RoundedCornerShape(8.dp),
) {
	val context = LocalContext.current
	Card(
		elevation = elevation,
		shape = shape,
		modifier = modifier,
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
					.padding(10.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Card(
					modifier = Modifier
						.width(48.dp)
						.height(48.dp),
					shape = RoundedCornerShape(12.dp),
					elevation = 0.dp
				) {
					Image(
						painter = painterResource(id = R.drawable.background),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.31f), BlendMode.SrcOver)
					)

					Column(
						modifier = Modifier
							.height(64.dp),
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
						fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
private fun GroupHeaderCard(
	modifier: Modifier = Modifier,
	title: String
) {
	Row(
		verticalAlignment = Alignment.Bottom,
		modifier = modifier,
	) {
		Box(
			modifier = Modifier
				.width(3.dp)
				.height(32.dp)
				.background(MaterialTheme.colorScheme.primary)
		)

		Spacer(modifier = Modifier.width(8.dp))

		Text(
			text = title,
			color = MaterialTheme.colorScheme.primary,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}

@Composable
private fun DiaryCard(
	modifier: Modifier,
	diaryDbEntry: DiaryDbEntry
) {
	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.primaryContainer,
		modifier = modifier
			.alpha(0.71f)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(12.dp, 8.dp, 12.dp, 4.dp)
		) {
			Text(
				text = "${diaryDbEntry.contentThumbnail}",
				style = MaterialTheme.typography.bodySmall,
			)
		}
	}
}
