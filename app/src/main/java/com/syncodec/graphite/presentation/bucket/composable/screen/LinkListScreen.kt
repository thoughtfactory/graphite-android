package com.syncodec.graphite.presentation.bucket.composable.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetOpenGraphResult
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkListScreen(
	bucketItemList : List<BucketItemObject> = listOf(),
) {
	val setOpenGraphResult = LocalCompositionSetOpenGraphResult.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelect.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDList.current

	val setBucketItemObject = LocalCompositionSetBucketItemObject.current
	val openSheet = LocalCompositionOpenBottomSheet.current

	if (bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.LINK)
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			bucketItemList.forEach { bucketItemObject ->
				item(
					key = bucketItemObject.id.toString(),
				) {
					val openGraphResult = bucketItemObject.getOpenGraphResult()
					Box(
						modifier = Modifier.animateItemPlacement()
					) {
						LinkItem(
							title = bucketItemObject.title,
							thumbnail = bucketItemObject.thumbnail,
							openGraphResult = openGraphResult,
							url = bucketItemObject.key,
							isLocked = bucketItemObject.isLocked,
							isFavourite = bucketItemObject.isFavourite,
							isSelected = bucketItemObject.id in selectedRealmUUIDList,
							onClick = {
								if (isSelected) {
									if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
									else selectedRealmUUIDList.add(bucketItemObject.id)
								} else {
									setBucketItemObject(bucketItemObject)
									setOpenGraphResult(openGraphResult)
									openSheet(BucketBottomSheetType.CURRENT_LINK)
								}
							},
							onLongClick = {
								if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
								else selectedRealmUUIDList.add(bucketItemObject.id)
								onSelected(true)
							}
						)
					}
				}

				item {
					Spacer(
						modifier = Modifier
							.fillMaxWidth()
							.height(2.dp)
							.padding(24.dp, 0.dp)
							.background(MaterialTheme.colorScheme.onBackground.copy(0.13f))
					)
				}
			}

			item { Spacer(modifier = Modifier.height(32.dp)) }
		}
	}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LinkItem(
	title : String?,
	thumbnail : String?,
	openGraphResult : OpenGraphResult?,
	url : String?,
	isLocked : Boolean,
	isFavourite : Boolean,
	isSelected : Boolean,
	onClick : () -> Unit,
	onLongClick : () -> Unit
) {

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(300)
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.background(containerColor)
			.combinedClickable(
				enabled = true,
				onClick = onClick,
				onLongClick = onLongClick
			)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp, 6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
			LaunchedEffect(key1 = thumbnail) {
				_thumbnail = thumbnail?.decodeBase64ToBitmap()
			}
			Thumbnail(
				thumbnail = _thumbnail,
				contentDescription = title
			)

			Spacer(modifier = Modifier.width(8.dp))

			Column(
				modifier = Modifier.weight(1f)
			) {

				TitleText(
					title = title,
					contentColor = contentColor,
					isLocked = isLocked,
					isFavourite = isFavourite
				)

				Spacer(modifier = Modifier.height(4.dp))

				UrlText(
					url = url,
					contentColor = contentColor
				)

				SiteNameText(
					siteName = openGraphResult?.siteName,
					contentColor = contentColor
				)

				Spacer(modifier = Modifier.height(4.dp))

				openGraphResult?.description?.let {
					DescriptionText(
						description = it,
						contentColor = contentColor
					)
					Spacer(modifier = Modifier.height(4.dp))
				}
			}
		}
	}
}

@Composable
private fun Thumbnail(
	thumbnail : Bitmap?,
	contentDescription : String?
) {
	val context = LocalContext.current
	var isThumbnailLoaded by remember { mutableStateOf(false) }

	Box(modifier = Modifier) {
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail)
				.crossfade(300)
				.build(),
			contentDescription = contentDescription,
			onSuccess = { isThumbnailLoaded = true },
			onError = { isThumbnailLoaded = false },
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.requiredSize(96.dp)
				.clip(RoundedCornerShape(16.dp)),
		)

		AnimatedVisibility(
			visible = ! isThumbnailLoaded,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300))
		) {
			Box(
				modifier = Modifier
					.requiredSize(96.dp)
					.clip(RoundedCornerShape(16.dp))
					.background(MaterialTheme.colorScheme.surface.copy(0.71f))
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_link),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground.copy(0.47f),
					modifier = Modifier
						.size(48.dp)
						.align(Alignment.Center)
				)
			}

		}
	}
}

@Composable
private fun TitleText(
	modifier : Modifier = Modifier,
	title : String?,
	contentColor : Color,
	isLocked : Boolean,
	isFavourite : Boolean
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.titleMedium,
			color = contentColor,
			fontWeight = if (title?.isNotEmpty() == true) FontWeight.Black else FontWeight.Normal,
			maxLines = 1,
			modifier = Modifier.weight(1f)
		)

		if (isLocked) {
			Icon(
				painter = painterResource(id = R.drawable.ic_lock_close),
				contentDescription = "Locked",
				tint = Color.LockClosedContainer,
				modifier = Modifier.requiredSize(14.dp)
			)
			if (isFavourite) {
				Spacer(modifier = Modifier.width(2.dp))
				Text(
					text = "·",
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					modifier = Modifier
				)
				Spacer(modifier = Modifier.width(2.dp))
			}
		}
		if (isFavourite) {
			Icon(
				painter = painterResource(id = R.drawable.ic_favourite),
				contentDescription = "Favourite",
				tint = Color.FavouriteContainer,
				modifier = Modifier.requiredSize(14.dp)
			)
		}
	}
}

@Composable
private fun DescriptionText(
	description : String,
	contentColor : Color
) {
	Text(
		text = description,
		style = MaterialTheme.typography.bodyMedium,
		color = contentColor,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun SiteNameText(
	siteName : String?,
	contentColor : Color
) {
	Text(
		text = siteName ?: "",
		style = MaterialTheme.typography.bodyMedium,
		color = contentColor.copy(alpha = 0.47f),
		fontWeight = if (siteName?.isNotEmpty() == true) FontWeight.Bold else FontWeight.Normal,
		maxLines = 2,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun UrlText(
	url : String?,
	contentColor : Color
) {
	Text(
		text = url ?: "",
		style = MaterialTheme.typography.bodyMedium,
		color = contentColor.copy(alpha = 0.31f),
		fontWeight = if (url?.isNotEmpty() == true) FontWeight.Bold else FontWeight.Normal,
		maxLines = 2,
		overflow = TextOverflow.Ellipsis
	)
}
