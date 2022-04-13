package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.foundation.isSystemInDarkTheme
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
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.notebookComponent.NotebookActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	onAction: (NotebookActivity.Action) -> Unit
) {

	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Attachment", icon = R.drawable.ic_attachment) {
			onAction(NotebookActivity.Action.ATTACHMENT)
		},
		MenuBottomSheetButtonData(title = "Set as default", icon = R.drawable.ic_state) {
			onAction(NotebookActivity.Action.SET_AS_DEFAULT)
		},
		MenuBottomSheetButtonData(title = "Atlas", icon = R.drawable.ic_atlas) {
			onAction(NotebookActivity.Action.ATLAS)
		},
		MenuBottomSheetButtonData(title = "Edit", icon = R.drawable.ic_pencil) {
			onAction(NotebookActivity.Action.EDIT_NOTEBOOK)
		},
		MenuBottomSheetButtonData(title = "Vault", icon = R.drawable.ic_vault) {
			onAction(NotebookActivity.Action.VAULT)
		},
		MenuBottomSheetButtonData(title = "Favourite", icon = R.drawable.ic_favourite) {
			onAction(NotebookActivity.Action.TOGGLE_FAVOURITE)
		},
		MenuBottomSheetButtonData(title = "Archived", icon = R.drawable.ic_archive) {
			onAction(NotebookActivity.Action.TOGGLE_ARCHIVED)
		},
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 2),
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(180.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(title = "Menu", icon = R.drawable.ic_menu)

			LazyVerticalGrid(
				columns = GridCells.Fixed(4),
				modifier = Modifier.padding(24.dp, 0.dp),
			) {
				buttonDataList.forEach { item { MenuBottomSheetButton(it) } }
			}

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
