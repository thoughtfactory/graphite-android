package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.calenderView.CalendarView
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CalendarScreen() {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	val calendarState = rememberLazyListState()

	val scrollToToday: () -> Unit = {
		scope.launch {
			calendarState.scrollToItem(2020, 1)
		}
	}
	scrollToToday()

	Scaffold(
		topBar = {
			MainTopBar(
				showBackground = true,
				openSheet = openSheet
			)
		}
	) {
		Column(
			modifier = Modifier
				.padding(1.dp, 0.dp)
				.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.71f))
		) {
			CalendarView(
				calendarState = calendarState,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			)

		}
	}
}
