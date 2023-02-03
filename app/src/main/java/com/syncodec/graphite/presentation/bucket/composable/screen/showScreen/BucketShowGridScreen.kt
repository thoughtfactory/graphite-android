package com.syncodec.graphite.presentation.bucket.composable.screen.showScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.GridItem
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDIdList
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID


@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BucketShowGridScreen(
	bucketId: RealmUUID? = null,
	bucketItemList : List<BucketItemObject> = listOf(),
) {
	val context = LocalContext.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelect.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDIdList.current

	val onDelete = LocalCompositionOnDelete.current

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.let {
				val hasIntentAction = it.hasExtra(Extra.Companion.Extra.INTENT_ACTION.name)
				if (hasIntentAction) {
					val intentAction = it.getStringExtra(Extra.Companion.Extra.INTENT_ACTION.name)?.let { it1 ->
						Extra.Companion.IntentAction.valueOf(it1)
					}
					if (intentAction == Extra.Companion.IntentAction.DELETE) {
						val hasObjectId = it.hasExtra(Extra.Companion.Extra.OBJECT_ID.name)
						if (hasObjectId) {
							val realmUUID = it.getByteArrayExtra(Extra.Companion.Extra.OBJECT_ID.name)?.let { RealmUUID.from(it) }
							if (realmUUID != null) {
								selectedRealmUUIDList.add(realmUUID)
								onDelete()
							}
						}
					}
				}
				Extra.Companion.Extra.INTENT_ACTION.name
				Extra.Companion.Extra.OBJECT_ID.name
			}
		} catch (e : Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	if (bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.SHOW)
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
							isSelected = bucketItemObject.id in selectedRealmUUIDList,
							onLongClick = {
								if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
								else selectedRealmUUIDList.add(bucketItemObject.id)
								onSelected(true)
							}
						) {
							if (isSelected) {
								if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
								else selectedRealmUUIDList.add(bucketItemObject.id)
							} else {
								Intent(context, BucketItemActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.IsNew.name, false)
									putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketId?.bytes)
									putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.SHOW.name)
									putExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name, bucketItemObject.id.bytes)
									putExtra(Extra.Companion.Extra.SHOW_TYPE.name, bucketItemObject.getShowData()?.type?.name)

									activityLauncher.launch(this)
								}
							}
						}
					}
				}
			}
		}
	}
}
