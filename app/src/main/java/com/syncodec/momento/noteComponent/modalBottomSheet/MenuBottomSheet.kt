package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.noteComponent.NoteViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import com.syncodec.momento.R
import com.syncodec.momento.noteComponent.NoteActivity


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Print", icon = R.drawable.ic_printer) {
		},
		MenuBottomSheetButtonData(title = "Export", icon = R.drawable.ic_export) {
		},
		MenuBottomSheetButtonData(title = "Share", icon = R.drawable.ic_share) {
		},
		MenuBottomSheetButtonData(title = "Delete", icon = R.drawable.ic_trash) {
		}
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
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
