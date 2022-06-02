package com.syncodec.graphite.bucketItemComponent.modalBottonSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.syncodec.graphite.noteComponent.modalBottomSheet.TimestampCard
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuBottomSheet(
	createdTimestamp: Long,
	modifiedTimestamp: Long,
	onAction: (BucketItemActivity.Action, Any?) -> Unit
) {
	val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(
			title = "Delete",
			icon = R.drawable.ic_trash,
			highlight = false
		) { onAction(BucketItemActivity.Action.DELETE, null) },
		MenuBottomSheetButtonData(
			title = "Export",
			icon = R.drawable.ic_export,
			highlight = false
		) { onAction(BucketItemActivity.Action.EXPORT, null) },
		MenuBottomSheetButtonData(
			title = "Share",
			icon = R.drawable.ic_share,
			highlight = false
		) { onAction(BucketItemActivity.Action.SHARE, null) },
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
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

			TimestampCard(
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp
			)

			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(72.dp),
				modifier = Modifier
					.padding(24.dp, 0.dp),
			) {
				itemsIndexed(menuBottomSheetButtonDataList) { _, menuBottomSheetButtonData ->
					MenuBottomSheetButton(menuBottomSheetButtonData)
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}
