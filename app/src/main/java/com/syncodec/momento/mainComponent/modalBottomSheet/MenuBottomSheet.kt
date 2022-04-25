package com.syncodec.momento.mainComponent.modalBottomSheet

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
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	onAction: (MainActivity.Action) -> Unit
) {
	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Attachment", icon = R.drawable.ic_attachment) {
			onAction(MainActivity.Action.ATTACHMENT)
		},
		MenuBottomSheetButtonData(title = "Tags", icon = R.drawable.ic_hashtag) {
			onAction(MainActivity.Action.TAGS)
		},
		MenuBottomSheetButtonData(title = "Vault", icon = R.drawable.ic_vault) {
			onAction(MainActivity.Action.VAULT)
		},
		MenuBottomSheetButtonData(title = "Settings", icon = R.drawable.ic_settings) {
			onAction(MainActivity.Action.SETTINGS)
		},

		MenuBottomSheetButtonData(title = "Favourite", icon = R.drawable.ic_favourite) {
			onAction(MainActivity.Action.TOGGLE_FAVOURITE)
		},
		MenuBottomSheetButtonData(title = "Archived", icon = R.drawable.ic_archive) {
			onAction(MainActivity.Action.TOGGLE_ARCHIVED)
		},
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
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
