package com.syncodec.graphite.noteComponent.modalBottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.MenuBottomSheetButton
import com.syncodec.graphite.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.R


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Print", icon = R.drawable.ic_printer) {
			onAction(NoteActivity.Action.PRINT, null)
		},
		MenuBottomSheetButtonData(title = "Export", icon = R.drawable.ic_export) {
			onAction(NoteActivity.Action.EXPORT, null)
		},
		MenuBottomSheetButtonData(title = "Copy", icon = R.drawable.ic_copy) {
			onAction(NoteActivity.Action.COPY, null)
		},
		MenuBottomSheetButtonData(title = "Delete", icon = R.drawable.ic_trash) {
			onAction(NoteActivity.Action.DELETE, null)
		}
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(180.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(
				title = "Menu",
				icon = R.drawable.ic_menu
			)

			LazyVerticalGrid(
				columns = GridCells.Fixed(4),
				modifier = Modifier.padding(24.dp, 0.dp),
			) { buttonDataList.forEach { item { MenuBottomSheetButton(it) } } }

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
