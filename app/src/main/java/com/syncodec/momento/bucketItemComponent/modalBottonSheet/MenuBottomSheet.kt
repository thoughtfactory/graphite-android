package com.syncodec.momento.bucketItemComponent.modalBottonSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.syncodec.momento.noteComponent.modalBottomSheet.TimestampCard
import compose.icons.TablerIcons
import compose.icons.tablericons.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuBottomSheet(
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	val scope = rememberCoroutineScope()

	val menuBottomSheetButtonDataLists: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Favourite", imageVector = TablerIcons.Copy) {},
		MenuBottomSheetButtonData(title = "Archive", imageVector = TablerIcons.Copy) {},
		MenuBottomSheetButtonData(title = "Lock", imageVector = TablerIcons.Copy) {},
		MenuBottomSheetButtonData(title = "Duplicate", imageVector = TablerIcons.Copy) {},

		MenuBottomSheetButtonData(title = "Share", imageVector = TablerIcons.Share) {},
		MenuBottomSheetButtonData(title = "Export", imageVector = TablerIcons.FileExport) {},
		MenuBottomSheetButtonData(title = "Delete", imageVector = TablerIcons.FileExport) {},
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
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp
		)

		Spacer(modifier = Modifier.height(12.dp))

		LazyVerticalGrid(
			cells = GridCells.Adaptive(72.dp),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(menuBottomSheetButtonDataLists) { _, menuBottomSheetButtonData ->
				MenuBottomSheetButton(menuBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
