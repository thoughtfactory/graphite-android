package com.syncodec.momento.mainComponent.screen

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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


sealed class MomentoScreenType {
	object Diary : MomentoScreenType()
	object Notebook : MomentoScreenType()
	object Scratchpad : MomentoScreenType()
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun MomentoScreen() {
	val viewModel: MainViewModel = viewModel()

	Box(
		modifier = Modifier
			.padding(0.dp, 0.dp, 0.dp, 64.dp)
	) {
		Crossfade(targetState = viewModel.mainActivityState.momentoScreenType.value) { momentoScreenType ->
			when (momentoScreenType) {
				MomentoScreenType.Diary -> DiaryScreen()
				MomentoScreenType.Notebook -> NotebookScreen()
				MomentoScreenType.Scratchpad -> ScratchpadScreen()
			}
		}
	}
}
