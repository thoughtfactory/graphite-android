package com.syncodec.graphite.presentation.bucket.composable.screen

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
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
fun LinkListScreen(
	bucketItemList : List<BucketItemObject> = listOf(),
	onReorderBucketItemList : (List<RealmUUID>) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val setOpenGraphResult = LocalCompositionSetOpenGraphResult.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelect.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current

	val setBucketItemObject = LocalCompositionSetBucketItemObject.current
	val openSheet = LocalCompositionOpenBottomSheet.current

	var _bucketItemList : SnapshotStateList<BucketItemObject> = remember { mutableStateListOf() }
	LaunchedEffect(bucketItemList) {
		_bucketItemList.clear()
		_bucketItemList.addAll(bucketItemList)
	}

	val state = rememberReorderableLazyListState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			_bucketItemList.apply { add(to.index, removeAt(from.index)) }
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketItemList(_bucketItemList.map { it.id }) }
		}
	)

	if (_bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.LINK)
	} else {
		LazyColumn(
			state = state.listState,
			modifier = Modifier
				.fillMaxSize()
				.reorderable(state)
		) {
			items(
				items = _bucketItemList,
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
		}
	}
}

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LinkItem(
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

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
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
				.padding(8.dp, 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_reorder),
				contentDescription = null,
				tint = contentColor.copy(alpha = 0.47f),
				modifier = handleModifier.size(16.dp)
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
		targetValue = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
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
