package com.syncodec.momento.attachmentComponent.modalBottomSheet

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
import com.syncodec.momento.attachmentComponent.AttachmentActivity
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	onAction: (AttachmentActivity.Action) -> Unit
) {
	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Share", icon = R.drawable.ic_share) {
			onAction(AttachmentActivity.Action.SHARE)
		},
		MenuBottomSheetButtonData(title = "Delete", icon = R.drawable.ic_trash) {
			onAction(AttachmentActivity.Action.DELETE)
		}
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
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
			) { buttonDataList.forEach { item { MenuBottomSheetButton(it) } } }

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
