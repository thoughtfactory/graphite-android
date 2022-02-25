package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.noteComponent.NoteViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet() {
	val noteViewModel: NoteViewModel = viewModel()
	val scope = rememberCoroutineScope()


	val hideSheet: () -> Unit = {
		scope.launch {
			noteViewModel.activityState.bottomSheetState.hide()
		}
	}

	val menuBottomSheetButtonDataLists: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Pin to top", imageVector = TablerIcons.Pinned) {},
		MenuBottomSheetButtonData(title = "Pin to notification", imageVector = TablerIcons.Notification) {},
		MenuBottomSheetButtonData(title = "Add to notebook", imageVector = TablerIcons.Notebook) {},
		MenuBottomSheetButtonData(title = "Duplicate", imageVector = TablerIcons.Copy) {},

		MenuBottomSheetButtonData(title = "Discard changes", imageVector = TablerIcons.X) {},
		MenuBottomSheetButtonData(title = "Share", imageVector = TablerIcons.Share) {},
		MenuBottomSheetButtonData(title = "Export", imageVector = TablerIcons.FileExport) {},
		null
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			imageVector = TablerIcons.Dots
		)

		TimestampCard(
			createdTimestamp = noteViewModel.note.createdTimestamp,
			modifiedTimestamp = noteViewModel.note.modifiedTimestamp
		)

		Spacer(modifier = Modifier.height(12.dp))

		FlowRow(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			mainAxisAlignment = MainAxisAlignment.SpaceBetween,
		) {
			menuBottomSheetButtonDataLists.forEach {
				MenuBottomSheetButton(
					menuBottomSheetButtonData = it,
					modifier = Modifier
						.width(80.dp)
				)
			}
		}


//		LazyVerticalGrid(
//			cells = GridCells.Adaptive(72.dp),
//			modifier = Modifier
//				.padding(24.dp, 0.dp)
//		) {
//			itemsIndexed(menuBottomSheetButtonDataLists) { _, menuBottomSheetButtonData ->
//				MenuBottomSheetButton(menuBottomSheetButtonData)
//			}
//		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
