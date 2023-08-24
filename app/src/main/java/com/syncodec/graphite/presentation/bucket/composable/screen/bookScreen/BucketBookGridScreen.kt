package com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.GridItem
import com.syncodec.graphite.presentation.bucketItem.activity.BookBucketItemActivity
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketBookGridScreen(
	bucketId : RealmUUID? = null,
	bucketItemList : List<BucketItemObject> = listOf(),
	isSelecting : Boolean = false,
	selectedIdList : Set<RealmUUID> = setOf(),
	onSelect : (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val hapticFeedback = LocalHapticFeedback.current

	if (bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.BOOK)
	} else {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(96.dp),
			horizontalArrangement = Arrangement.Center,
			contentPadding = PaddingValues(8.dp, 0.dp),
			modifier = Modifier.fillMaxSize()
		) {
			bucketItemList.forEach { bucketItemObject ->
				item(
					key = bucketItemObject.id.toString(),
				) {
					Box(
						modifier = Modifier.animateItemPlacement()
					) {
						GridItem(
							title = bucketItemObject.title,
							thumbnail = bucketItemObject.thumbnail,
							isSelected = bucketItemObject.id in selectedIdList,
							onLongClick = {
								hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
								onSelect(bucketItemObject.id)
							}
						) {
							if (isSelecting) onSelect(bucketItemObject.id)
							else Intent(context, BookBucketItemActivity::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, false)
								putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketId?.bytes)
								putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.BOOK.name)
								putExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name, bucketItemObject.id.bytes)

								context.startActivity(this)
							}
						}
					}
				}
			}
		}
	}
}
