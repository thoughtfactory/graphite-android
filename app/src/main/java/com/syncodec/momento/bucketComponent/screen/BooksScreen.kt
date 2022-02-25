package com.syncodec.momento.bucketComponent.screen

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BooksScreen(
	bucketItemList: List<BucketItemDbEntry>,
	isSelected: Boolean,
	selectedBucketItemList: SnapshotStateList<String>,
	getThumbnailPath: (String) -> String
) {
	val context = LocalContext.current

	LazyVerticalGrid(
		cells = GridCells.Adaptive(96.dp),
		modifier = Modifier
			.padding(12.dp, 12.dp, 12.dp, 0.dp)
	) {
		bucketItemList.forEach { bucketItemDbEntry ->
			item {
				BookItem(
					title = bucketItemDbEntry.title!!,
					thumbnail = getThumbnailPath(bucketItemDbEntry.primaryKey),
					highlight = false
				) {
					if (isSelected) {
						if (bucketItemDbEntry.primaryKey in selectedBucketItemList) {
							selectedBucketItemList.remove(bucketItemDbEntry.primaryKey)
						} else {
							selectedBucketItemList.add(bucketItemDbEntry.primaryKey)
						}
					} else {
						Intent(context, BucketItemActivity::class.java).apply {
							putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, bucketItemDbEntry.bucketKey)
							putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, bucketItemDbEntry.primaryKey)
							putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.BOOKS.ordinal)

							context.startActivity(this)
						}
					}
				}
			}
		}
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {

	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookItem(
	title: String,
	thumbnail: String?,
	highlight: Boolean,
	onClick: () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp)
	) {
		Card(
			elevation = 16.dp,
			border = BorderStroke(4.dp, if (highlight) MaterialTheme.colorScheme.primary else Color.Transparent),
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f),
			onClick = { onClick() }
		) {
			if (thumbnail != null) {
				Image(
					painter = rememberImagePainter(
						data = File(thumbnail),
						builder = {
							crossfade(false)
						}
					),
					contentDescription = null,
					contentScale = ContentScale.Crop,
				)
			}
		}

		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
