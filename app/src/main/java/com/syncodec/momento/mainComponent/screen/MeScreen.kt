package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.database.note.NoteDbEntry
import compose.icons.TablerIcons
import compose.icons.tablericons.Bucket
import compose.icons.tablericons.Notes
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Signature
import java.util.*
import kotlin.math.max


@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MeScreen(
	noteList: List<NoteDbEntry>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val last5NoteEntry: MutableList<NoteDbEntry> = remember { mutableListOf() }
	val lastDiarySize: MutableMap<Int, Int> = remember { mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 6 to 0) }

	val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

	noteList.forEach {
		if (last5NoteEntry.size < 6) {
			last5NoteEntry.add(it)
		}
		val dayBefore = ((calendar.timeInMillis - it.createdTimestamp )).toInt()
		if (lastDiarySize.containsKey(dayBefore)) {
			lastDiarySize[dayBefore] = lastDiarySize[dayBefore]!! + 1
		} else {
			lastDiarySize[dayBefore] = 1
		}
	}

	val scrollState = rememberScrollState()

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Image(
			painter = painterResource(id = R.drawable.home_background),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.height(screenHeight / 3)
				.graphicsLayer {
					this.alpha = 1 - (scrollState.value / (screenHeight / 3).toPx())
					this.translationY = -(screenHeight / 8).toPx() * scrollState.value / (screenHeight / 3).toPx()
				},
		)

		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState),
		) {
			Spacer(modifier = Modifier.height((screenHeight / 3) - 54.dp))

			ProfileCard { click, data -> onClick(click, data) }

			StatsCard()
			Spacer(modifier = Modifier.height(8.dp))

			GraphCard(lastDiarySize = lastDiarySize)
			Spacer(modifier = Modifier.height(8.dp))

			RecentCard()

			Spacer(modifier = Modifier.height(192.dp))
		}
	}
}

@Composable
private fun ProfileCard(
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(108.dp),
			contentAlignment = Alignment.Center
		) {
			Card(
				modifier = Modifier.requiredSize(108.dp),
				shape = RoundedCornerShape(12.dp),
				elevation = 16.dp
			) {
				Image(
					painter = painterResource(id = R.drawable.debug_profile_picture),
					contentDescription = null
				)
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp, 0.dp),
				contentAlignment = Alignment.BottomEnd
			) {
				IconButton(onClick = { onClick(MainActivity.Click.SETTINGS, null) }) {
					Icon(imageVector = TablerIcons.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Bruce Wayne",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground
		)

		Text(
			text = "bruce.wayne@gmail.com",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun StatsCard() {
	Box(
		modifier = Modifier
			.padding(12.dp)
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant),
		contentAlignment = Alignment.Center
	) {
		Row(
			modifier = Modifier
				.padding(12.dp, 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				modifier = Modifier
					.weight(1f)
					.padding(8.dp, 4.dp),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = TablerIcons.Signature,
					contentDescription = "Entries",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.requiredSize(20.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "13 entries",
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Box(
				modifier = Modifier
					.width(3.dp)
					.height(24.dp)
					.clip(RoundedCornerShape(50))
					.background(MaterialTheme.colorScheme.onBackground)
			)

			Row(
				modifier = Modifier
					.weight(1f)
					.padding(8.dp, 4.dp),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = TablerIcons.Bucket,
					contentDescription = "Bucket",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.requiredSize(20.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "47 bucket items",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Box(
				modifier = Modifier
					.width(3.dp)
					.height(24.dp)
					.clip(RoundedCornerShape(50))
					.background(MaterialTheme.colorScheme.onBackground)
			)

			Row(
				modifier = Modifier
					.weight(1f)
					.padding(8.dp, 4.dp),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = TablerIcons.Notes,
					contentDescription = "Notes",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.requiredSize(20.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "71 notes",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun GraphCard(lastDiarySize: MutableMap<Int, Int>) {
	val lineColor = MaterialTheme.colorScheme.secondary.toArgb()

	AndroidView(
		factory = {
			LineChart(it)
		},
		modifier = Modifier
			.fillMaxWidth()
			.height(256.dp)
			.padding(12.dp, 0.dp)
	) {
		it.apply {
			setTouchEnabled(false)

			val dataArray: MutableList<Entry> = mutableListOf()
			var maxValue = 0
			lastDiarySize.forEach { (key, value) ->
				dataArray.add(Entry((6 - key).toFloat(), value.toFloat()))
				maxValue = max(maxValue, value)
			}
			dataArray.reverse()

			xAxis.valueFormatter = IndexAxisValueFormatter(listOf("S", "M", "T", "W", "T", "F", "S"))

			axisLeft.isGranularityEnabled = true
			axisRight.isGranularityEnabled = true
			axisLeft.granularity = 1f
			axisRight.granularity = 1f

			axisLeft.valueFormatter = object : ValueFormatter() {
				override fun getFormattedValue(value: Float): String {
					return value.toInt().toString()
				}
			}
			axisRight.valueFormatter = object : ValueFormatter() {
				override fun getFormattedValue(value: Float): String {
					return value.toInt().toString()
				}
			}


			LineDataSet(dataArray, "Number of entries").apply {
				mode = LineDataSet.Mode.HORIZONTAL_BEZIER
				color = lineColor
				lineWidth = 3.1f
				setDrawCircleHole(false)
				setDrawCircles(false)

				data = LineData(this)
				notifyDataSetChanged()
			}
			invalidate()
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RecentCard() {
//	val viewModel: MainViewModel = viewModel()
//
//	var showRecent by remember { mutableStateOf(true) }
//
//	val vaultState by viewModel.activityState.vaultState
//	val diaryList by viewModel.noteRepository.noteDbEntryListLiveData.observeAsState()
//	diaryList?.sortedBy { it.createdTimestamp }
//
//	var currentState by remember { mutableStateOf(0) }
//
//	Column(
//		modifier = Modifier.fillMaxWidth()
//	) {
//		Box(
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(8.dp, 0.dp, 6.dp, 0.dp)
//				.clip(RoundedCornerShape(12.dp))
//				.background(MaterialTheme.colorScheme.background)
//				.clickable { showRecent = !showRecent }
//		) {
//			Row(
//				verticalAlignment = Alignment.Bottom,
//				modifier = Modifier
//					.fillMaxWidth()
//					.padding(6.dp, 0.dp)
//			) {
//				Box(
//					modifier = Modifier
//						.width(4.dp)
//						.height(40.dp)
//						.clip(RoundedCornerShape(4.dp))
//						.background(MaterialTheme.colorScheme.primary)
//				)
//
//				Spacer(modifier = Modifier.width(8.dp))
//
//				Box(
//					modifier = Modifier
//						.fillMaxWidth()
//						.padding(0.dp, 8.dp),
//					contentAlignment = Alignment.BottomStart
//				) {
//					Text(
//						text = "Recently edited",
//						color = MaterialTheme.colorScheme.primary,
//						style = MaterialTheme.typography.bodyLarge
//					)
//				}
//			}
//		}
//
//		Spacer(modifier = Modifier.height(8.dp))
//
//		StateButton(
//			stateList = listOf(
//				StateData(title = "Diary", icon = TablerIcons.Signature, color = MaterialTheme.colorScheme.primary),
//				StateData(title = "Notebook", icon = TablerIcons.Notebook, color = MaterialTheme.colorScheme.primary),
//			),
//			currentState = currentState,
//			modifier = Modifier
//				.height(32.dp)
//				.padding(12.dp, 0.dp)
//		) { currentState = it }
//
//		Spacer(modifier = Modifier.height(12.dp))
//
//		diaryList?.forEachIndexed { index, diaryDbEntry ->
//			if (index < 5) {
//				NoteCardData(
//					timestamp = diaryDbEntry.userTimestamp,
//					showFullTime = true,
//					isLocked = diaryDbEntry.isLocked,
//					isSelected = false,
//					isArchived = diaryDbEntry.isArchived,
//					isFavourite = diaryDbEntry.isFavourite,
//					isDeleted = diaryDbEntry.deletedTimestamp != -1L,
//					isLast = index == diaryList!!.size - 1 || index == 4,
//					title = diaryDbEntry.title,
//					contentThumbnail = diaryDbEntry.contentThumbnail,
//					attachmentCount = diaryDbEntry.attachmentCount,
//					attachmentThumbnail = diaryDbEntry.attachmentThumbnail,
//					address = diaryDbEntry.address,
//					isVisible = currentState == 0 && showRecent,
//					onClick = {},
//				).apply {
//					NoteCard(noteCardData = this)
//				}
//
//				NotebookTimelineSpacer(isVisible = index != diaryList!!.size - 1 && index != 4 && currentState == 0 && showRecent)
//			}
//		}
//	}
}
