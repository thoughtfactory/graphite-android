package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.calendarView.Calendar
import com.syncodec.momento.custom.entry.EntryCard
import com.syncodec.momento.custom.entry.EntryHeaderCard
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CalendarScreen() {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val scope = rememberCoroutineScope()
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	SideEffect {
		scope.launch {
			delay(2000)
			bottomSheetScaffoldState.bottomSheetState
		}
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = { BottomSheetContent() },
		modifier = Modifier,
		topBar = { TopBar() },
		sheetElevation = 32.dp,
		sheetPeekHeight = screenHeight.times(0.2f),
		sheetBackgroundColor = MaterialTheme.colorScheme.background
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			Calendar()
		}
	}
}

@Composable
private fun BottomSheetContent() {
	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			EntryHeaderCard(
				title = "4th May, 2021",
				noEntries = "13 entries"
			)
		}
		for (i in 0 until 13) {
			item {
				EntryCard(
					timestamp = System.currentTimeMillis(),
					isLocked = false,
					isSelected = false,
					isArchived = false,
					isFavourite = false,
					isDeleted = false,
					isLast = false,
					title = "Title",
					contentThumbnail = "diaryDbEntry.contentThumbnail",
					attachmentCount = 0,
					attachmentThumbnail = null,
					address = "diaryDbEntry.address",
					tint = MaterialTheme.colorScheme.primaryContainer,
					onClick = {
					},
					onLongClick = {
					},
				).apply {
					EntryCard(entryCard = this)
				}
			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
