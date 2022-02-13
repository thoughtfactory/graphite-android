package com.syncodec.momento.mainComponent.screen

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.ComponentChooser
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import kotlinx.coroutines.launch


sealed class MomentoScreenType {
	object Diary : MomentoScreenType()
	object Notebook : MomentoScreenType()
	object Scratchpad : MomentoScreenType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun MomentoScreen() {
	val viewModel: MainViewModel = viewModel()

	val scope = rememberCoroutineScope()
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
			stiffness = Spring.StiffnessMediumLow
		),
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		MainTopBar(
			openSheet = openSheet,
			showBackground = true
		)

		ComponentChooser()

		Crossfade(
			targetState = viewModel.mainActivityState.momentoScreenType.value,
			modifier = Modifier
				.graphicsLayer {
					this.scaleX = scaffoldScale
					this.scaleY = scaffoldScale
				}
		) { momentoScreenType ->
			when (momentoScreenType) {
				MomentoScreenType.Diary -> DiaryScreen()
				MomentoScreenType.Notebook -> NotebookScreen()
				MomentoScreenType.Scratchpad -> ScratchpadScreen()
			}
		}
	}
}
