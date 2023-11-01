package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.BookTitleView
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.BucketItemScreenSkeleton
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.BucketThumbnail
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabDefaults
import com.syncodec.graphite.presentation.common.tab.TabItem


@Preview
@Composable
fun BookBucketItemScreen(
	isNew: Boolean = false,
	bucketItemObject: BucketItemObject? = null,
	onClickSave: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickShare : () -> Unit = {},
	onConfirmDelete : () -> Unit= {},
	onUpdateState: (BucketItemState) -> Unit = {},
) {
	val context = LocalContext.current
	val clipboardManager = LocalClipboardManager.current

	val bookData by remember(bucketItemObject?.data) { derivedStateOf { BucketItemObject.Companion.BucketItemData.BookData(jsonString = bucketItemObject?.data) } }
	val state by remember(bucketItemObject?.state) { derivedStateOf { bucketItemObject?.state } }

	BucketItemScreenSkeleton(
		isNew = isNew,
		bucketItemObject = bucketItemObject,
		onClickSave = onClickSave,
		onClickFavourite = onClickFavourite,
		onClickLock = onClickLock,
		onClickShare = onClickShare,
		onConfirmDelete = onConfirmDelete
	) {
		Spacer(modifier = Modifier.height(12.dp))
		BucketThumbnail(
			key = bucketItemObject?.id?.toString(),
			thumbnail = bucketItemObject?.thumbnail
		)
		Spacer(modifier = Modifier.height(24.dp))
		BookTitleView(
			title = bookData.title,
			author = bookData.authorList?.firstNotNullOfOrNull { it }
		)
		Spacer(modifier = Modifier.height(8.dp))

		GenericTabRow(
			tabItemList = listOf(
				TabItem(text = stringResource(id = R.string.to_read), icon = R.drawable.ic_fa_clock, onClick = { onUpdateState(BucketItemState.ALPHA) }),
				TabItem(text = stringResource(id = R.string.reading), icon = R.drawable.ic_fa_bucket_book, onClick = { onUpdateState(BucketItemState.BETA) }),
				TabItem(text = stringResource(id = R.string.read), icon = R.drawable.ic_fa_circle_check, onClick = { onUpdateState(BucketItemState.GAMMA) }),
			),
			selectedTabIndex = maxOf(0, BucketItemState.entries.indexOfFirst { it.name == state }),
			modifier = Modifier.fillMaxWidth(),
			colors = TabDefaults.tabColors(containerColor = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)))
		)
		Spacer(modifier = Modifier.height(8.dp))
		bookData.key?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.open_library_id),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		bookData.description?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.description),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		bookData.firstPublishYear?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.first_published_year),
				value = it,
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it)) }
			)
		}
		bookData.numberOfPages?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.page_count),
				value = it.toString(),
				onLongClick = { clipboardManager.setText(annotatedString = AnnotatedString(it.toString())) }
			)
		}
		Button(
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				bookData.key?.let {
					try {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://openlibrary.org$it")))
					} catch (e: Exception) {
						Toast.makeText(context, context.getText(R.string.toast_error_opening_link), Toast.LENGTH_SHORT).show()
					}
				}
			}
		) {
			Text(text = stringResource(id = R.string.open_in_open_library))
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_open_externally),
				contentDescription = stringResource(id = R.string.open_in_open_library),
				modifier = Modifier.requiredSize(16.dp)
			)
		}
		Spacer(modifier = Modifier.height(12.dp))
	}
}
