package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@Preview
@Composable
fun BucketLinkListScreen(
	bucketItemList : List<BucketItemObject> = listOf(),
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	onSelect : (RealmUUID) -> Unit = {},
	onReorderBucketItemList : (List<RealmUUID>) -> Unit = {},
	onClickBucketItem : (RealmUUID) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var isLoading by remember { mutableStateOf(true) }

	val bucketItemListOrdered : SnapshotStateList<BucketItemObject> = remember { mutableStateListOf() }
	LaunchedEffect(bucketItemList) {
		isLoading = true
		bucketItemListOrdered.clear()
		bucketItemListOrdered.addAll(bucketItemList)
		isLoading = false
	}

	val state = rememberReorderableLazyListState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketItemListOrdered.apply { add(to.index, removeAt(from.index)) }
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) }
		}
	)

	if (isLoading) {
		LoadingView()
	} else {
		if (bucketItemListOrdered.isEmpty()) {
			EmptyView(bucketType = BucketType.LINK)
		} else {
			LazyColumn(
				state = state.listState,
				modifier = Modifier
					.fillMaxSize()
					.reorderable(state)
			) {
				items(
					items = bucketItemListOrdered,
					key = { it.id.toString() }
				) { bucketItemObject ->
					ReorderableItem(
						reorderableState = state,
						key = bucketItemObject.id.toString(),
					) { isDragging ->
						val openGraphResult = bucketItemObject.getOpenGraphResult()

						LinkItem(
							handleModifier = Modifier.detectReorder(state),
							title = bucketItemObject.title,
							thumbnail = bucketItemObject.thumbnail,
							openGraphResult = openGraphResult,
							url = bucketItemObject.key,
							isLocked = bucketItemObject.isLocked,
							isFavourite = bucketItemObject.isFavourite,
							isSelected = bucketItemObject.id in selectedIdList,
							onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject.id) },
							onLongClick = { onSelect(bucketItemObject.id) }
						)
					}
				}
			}
		}
	}
}

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkItem(
	handleModifier : Modifier = Modifier,
	title : String? = null,
	thumbnail : String? = null,
	openGraphResult : OpenGraphResult? = null,
	url : String? = null,
	isLocked : Boolean = false,
	isFavourite : Boolean = true,
	isSelected : Boolean = false,
	isDragging : Boolean = false,
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {},
) {
	val hapticFeedback = LocalHapticFeedback.current

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f) else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(300)
	)

	Box(
		contentAlignment = Alignment.CenterStart,
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.background(containerColor)
			.combinedClickable(
				enabled = true,
				onClick = onClick,
				onLongClick = {
					hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
					onLongClick()
				}
			)
	) {
		Row(
			verticalAlignment = Alignment.Top,
			modifier = Modifier.padding(8.dp, 4.dp),
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_reorder),
				contentDescription = null,
				tint = contentColor.copy(alpha = 0.47f),
				modifier = handleModifier
					.size(16.dp)
					.align(Alignment.CenterVertically)
			)

			Spacer(modifier = Modifier.width(8.dp))

			var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
			LaunchedEffect(key1 = thumbnail) {
				_thumbnail = thumbnail?.decodeBase64ToBitmap()
			}
			Thumbnail(
				thumbnail = _thumbnail,
				contentDescription = title,
				isSelected = isSelected,
			)

			Spacer(modifier = Modifier.width(8.dp))

			Column(
				verticalArrangement = Arrangement.Top,
				modifier = Modifier.weight(1f)
			) {

				TitleText(
					title = title,
					contentColor = contentColor,
					isLocked = isLocked,
					isFavourite = isFavourite
				)

				UrlText(
					url = url,
					contentColor = contentColor
				)

				SiteNameText(
					siteName = openGraphResult?.siteName,
					contentColor = contentColor
				)

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
	thumbnail : Bitmap? = null,
	contentDescription : String? = null,
	isSelected : Boolean = false,
) {
	val context = LocalContext.current
	var isThumbnailLoaded by remember { mutableStateOf(false) }

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.71f),
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
		animationSpec = tween(300)
	)

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
				.clip(MaterialTheme.shapes.medium),
		)

		AnimatedVisibility(
			visible = ! isThumbnailLoaded,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300))
		) {
			Box(
				modifier = Modifier
					.requiredSize(96.dp)
					.clip(MaterialTheme.shapes.medium)
					.background(containerColor)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_link),
					contentDescription = null,
					tint = contentColor,
					modifier = Modifier
						.size(32.dp)
						.align(Alignment.Center)
				)
			}

		}
	}
}

@Preview
@Composable
private fun TitleText(
	modifier : Modifier = Modifier,
	title : String? = "Title",
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
	isLocked : Boolean = false,
	isFavourite : Boolean = true,
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
			overflow = TextOverflow.Ellipsis,
			maxLines = 1,
			modifier = Modifier.weight(1f)
		)

		if (isLocked || isFavourite) {
			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f), MaterialTheme.shapes.small)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(8.dp, 4.dp)
				) {
					if (isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_shield),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (isFavourite) Text(
							text = "·",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier.padding(horizontal = 2.dp)
						)
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
		}
	}
}

@Preview
@Composable
private fun DescriptionText(
	description : String = "Description",
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
) {
	Text(
		text = description,
		style = MaterialTheme.typography.bodySmall,
		color = contentColor,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}

@Preview
@Composable
private fun SiteNameText(
	siteName : String? = "Site Name",
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
) {
	Text(
		text = siteName ?: "",
		style = MaterialTheme.typography.bodyMedium,
		color = contentColor.copy(alpha = 0.47f),
		maxLines = 2,
		overflow = TextOverflow.Ellipsis
	)
}

@Preview
@Composable
private fun UrlText(
	url : String? = "https://www.example.com",
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
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
