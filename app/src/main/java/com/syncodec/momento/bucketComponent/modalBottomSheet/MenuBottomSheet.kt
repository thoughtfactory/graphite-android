package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.noteComponent.modalBottomSheet.TimestampCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuBottomSheet(
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData?> = listOf(
//			MenuBottomSheetButtonData(
//				title = "Favourite",
//				resourceId = if (bucketDbEntry!!.isFavourite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
//				highlight = bucketDbEntry!!.isFavourite
//			) { },
//			MenuBottomSheetButtonData(
//				title = "Archive",
//				resourceId = R.drawable.ic_box,
//				highlight = bucketDbEntry!!.isArchived
//			) { },
//			MenuBottomSheetButtonData(
//				title = "Lock",
//				resourceId = R.drawable.ic_locked,
//				highlight = bucketDbEntry!!.isLocked
//			) { },
		MenuBottomSheetButtonData(title = "Delete", icon = R.drawable.ic_trash, highlight = false) { },

		MenuBottomSheetButtonData(title = "Export", icon = R.drawable.ic_export, highlight = false) { },
		MenuBottomSheetButtonData(title = "Share", icon = R.drawable.ic_share, highlight = false) { },
		null,
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
