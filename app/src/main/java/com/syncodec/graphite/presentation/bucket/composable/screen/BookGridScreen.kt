package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.bucketItem2.BucketItemActivity2
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Preview
@Composable
fun BookGridScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
) {
	val context = LocalContext.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelected.current
	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	val bucketObjectId = LocalCompositionBucketObject.current?.id

	LazyVerticalGrid(
		columns = GridCells.Adaptive(128.dp),
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.padding(4.dp, 0.dp),
	) {
		bucketItemList.forEach { bucketItemObject ->
			item {
				GridItem(
					title = bucketItemObject.title,
					thumbnail = bucketItemObject.thumbnail,
					isSelected = bucketItemObject.id in selectedObjectIdList,
					onLongClick = {
						if (bucketItemObject.id in selectedObjectIdList) selectedObjectIdList.remove(bucketItemObject.id)
						else selectedObjectIdList.add(bucketItemObject.id)
						onSelected(true)
					}
				) {
					if (isSelected) {
						if (bucketItemObject.id in selectedObjectIdList) selectedObjectIdList.remove(bucketItemObject.id)
						else selectedObjectIdList.add(bucketItemObject.id)
					} else {
						Intent(context, BucketItemActivity2::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.BUCKET_ID.name, bucketObjectId.toString())
							putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.BOOK.name)
							putExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name, bucketItemObject.id.toString())

							context.startActivity(this)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridItem(
	title : String?,
	thumbnail : String?,
	isSelected : Boolean,
	onLongClick : () -> Unit,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	val borderColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
		animationSpec = tween(300)
	)
	val scaleContent by animateFloatAsState(targetValue = if (isSelected) 0.9f else 1f)

	var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = thumbnail) {
		_thumbnail = thumbnail?.decodeBase64ToBitmap()
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
				.border(2.dp, borderColor, RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick() }
				),
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(12.dp))
					.graphicsLayer {
						scaleX = scaleContent
						scaleY = scaleContent
						shape = RoundedCornerShape(12.dp)
					},
				contentAlignment = Alignment.Center
			) {
				if (_thumbnail != null) {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(_thumbnail)
							.crossfade(300)
							.build(),
						placeholder = null,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxSize()
							.clip(RoundedCornerShape(12.dp)),
					)
				} else {
					Text(
						text = "Thumbnail unavailable",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
						textAlign = TextAlign.Center,
						modifier = Modifier
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
		)
	}
}
