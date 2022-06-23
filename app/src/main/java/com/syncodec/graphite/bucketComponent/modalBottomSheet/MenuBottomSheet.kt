package com.syncodec.graphite.bucketComponent.modalBottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.custom.bottomSheet.BottomSheetTitleCard
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.MenuBottomSheetButton
import com.syncodec.graphite.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.konstant.ResourceMap


@Composable
fun MenuBottomSheet(
	key: String?,
	createdTimestamp: Long,
	modifiedTimestamp: Long,
	bucketTitle: String,
	bucketItemType: BucketItemType,
	alphaCount: Int,
	betaCount: Int,
	gammaCount: Int,
	totalCount: Int,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData?> = listOf(
//		MenuBottomSheetButtonData(
//			title = "Delete",
//			icon = R.drawable.ic_delete,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Export",
//			icon = R.drawable.ic_export,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Share",
//			icon = R.drawable.ic_share,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Edit",
//			icon = R.drawable.ic_pencil,
//			highlight = false
//		) { onAction(BucketActivity.Action.EDIT_BUCKET, null) }
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

			BottomSheetTitleCard(
				title = bucketTitle,
				placeholder = "Bucket name"
			) { onAction(BucketActivity.Action.UPDATE_BUCKET, it) }

			Spacer(modifier = Modifier.height(8.dp))

			BottomSheetKeyCard(
				key = key,
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp
			)

			if (bucketItemType!=BucketItemType.LINK) {
				Spacer(modifier = Modifier.height(8.dp))

				DataCard(
					bucketItemType = bucketItemType,
					alphaCount = alphaCount,
					betaCount = betaCount,
					gammaCount = gammaCount,
					totalCount = totalCount,
				)
			}

			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(72.dp),
				modifier = Modifier.padding(24.dp, 0.dp),
			) {
				itemsIndexed(menuBottomSheetButtonDataList) { _, menuBottomSheetButtonData ->
					MenuBottomSheetButton(menuBottomSheetButtonData)
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
fun DataCard(
	bucketItemType: BucketItemType,
	alphaCount: Int,
	betaCount: Int,
	gammaCount: Int,
	totalCount: Int,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Row(
			Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {
			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_state,
				title = "All",
				count = totalCount
			)

			Spacer(modifier = Modifier.width(8.dp))

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_clock,
				title = when (bucketItemType) {
					BucketItemType.TODO -> "To Do"
					BucketItemType.BOOK -> "To Read"
					BucketItemType.SHOW -> "To Watch"
					BucketItemType.LINK -> "To Visit"
				},
				count = alphaCount
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = ResourceMap.bucketTypeToIcon[bucketItemType]!!,
				title = when (bucketItemType) {
					BucketItemType.TODO -> "Doing"
					BucketItemType.BOOK -> "Reading"
					BucketItemType.SHOW -> "Watching"
					BucketItemType.LINK -> "Opened"
				},
				count = betaCount
			)

			Spacer(modifier = Modifier.width(8.dp))

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_done,
				title = when (bucketItemType) {
					BucketItemType.TODO -> "Done"
					BucketItemType.BOOK -> "Read"
					BucketItemType.SHOW -> "Watched"
					BucketItemType.LINK -> "Done"
				},
				count = gammaCount
			)
		}
	}
}

@Composable
private fun DataButton(
	modifier: Modifier,
	icon: Int,
	title: String,
	count: Int
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = title,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(20.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = "$count",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
