package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.BucketItemScreenSkeleton
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.BucketThumbnail
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.MovieTitleView
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetInfo
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.OpenExternallyButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabDefaults
import com.syncodec.graphite.presentation.common.tab.TabItem


@Preview
@Composable
fun TvBucketItemScreen(
	isNew: Boolean = false,
	bucketItemObject: BucketItemObject? = null,
	tvData: BucketItemData.ShowData.TMDbData.TMDbTvData? = null,
	onClickSave: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onConfirmDelete : () -> Unit= {},
	onClickShare : () -> Unit = {},
	onUpdateState: (BucketItemState) -> Unit = {},
) {
	val context = LocalContext.current
	val clipboardManager = LocalClipboardManager.current

	val state by remember(bucketItemObject?.state) { derivedStateOf { bucketItemObject?.state } }

	BucketItemScreenSkeleton(
		isNew = isNew,
		bucketItemObject = bucketItemObject,
		onClickSave = onClickSave,
		onClickFavourite = onClickFavourite,
		onClickLock = onClickLock,
		onClickShare = onClickShare,
		onConfirmDelete = onConfirmDelete,
	) {
		Spacer(modifier = Modifier.height(12.dp))

		BucketThumbnail(
			key = bucketItemObject?.id?.toString(),
			thumbnail = bucketItemObject?.thumbnail
		)

		Spacer(modifier = Modifier.height(24.dp))

		MovieTitleView(
			title = tvData?.title,
			tagLine = tvData?.tagline,
		)
		Spacer(modifier = Modifier.height(8.dp))

		GenericTabRow(
			tabItemList = listOf(
				TabItem(text = stringResource(id = R.string.to_watch), icon = R.drawable.ic_fa_clock, onClick = { onUpdateState(BucketItemState.ALPHA) }),
				TabItem(text = stringResource(id = R.string.watching), icon = R.drawable.ic_fa_bucket_show, onClick = { onUpdateState(BucketItemState.BETA) }),
				TabItem(text = stringResource(id = R.string.watched), icon = R.drawable.ic_fa_circle_check, onClick = { onUpdateState(BucketItemState.GAMMA) }),
			),
			selectedTabIndex = maxOf(0, BucketItemState.entries.indexOfFirst { it.name == state }),
			modifier = Modifier.fillMaxWidth(),
			colors = TabDefaults.tabColors(containerColor = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)))
		)
		Spacer(modifier = Modifier.height(8.dp))
		tvData?.firstAirDate?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.first_air_date),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		tvData?.description?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.overview),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		tvData?.originalName?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.original_title),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		GenericBottomSheetInfo(
			key = "${stringResource(id = R.string.season)} : ${stringResource(id = R.string.episode)}",
			value = "${tvData?.numberOfSeasons ?: "-"} : ${tvData?.numberOfEpisodes ?: "-"}"
		)
		tvData?.key?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.tmdb_id),
				value = it,
				suffix = {
					OpenExternallyButton(
						colors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onSurface)
					) {
						try {
							context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/tv/$it")))
						} catch (e: Exception) {
							Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
						}
					}
				},
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString("https://www.themoviedb.org/tv/$it")) }
			)
		}
		if (!tvData?.homepage.isNullOrEmpty()) {
			tvData?.homepage?.let {
				GenericBottomSheetInfo(
					key = stringResource(id = R.string.homepage),
					value = it,
					suffix = {
						OpenExternallyButton(
							colors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onSurface)
						) {
							try {
								context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
							} catch (e: Exception) {
								if (BuildConfig.DEBUG) e.printStackTrace()
								Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
							}
						}
					},
					onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString("https://www.themoviedb.org/tv/$it")) }
				)
			}
		}
		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			tvData?.getGenreList()?.forEach { genre ->
				SuggestionChip(
					label = { Text(text = genre) },
					onClick = { },
				)
				Spacer(modifier = Modifier.width(4.dp))
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
	}
}
