package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemFavourite
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemLock
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenGraphResult
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.text.KeyValueText
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Preview
@Composable
fun CurrentLinkBottomSheet() {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current

	val openGraphResult = LocalCompositionOpenGraphResult.current

	val bucketItemObject = LocalCompositionBucketItemObject.current
	val onClickLock = LocalCompositionOnClickBucketItemLock.current
	val onClickFavourite = LocalCompositionOnClickBucketItemFavourite.current
	val onClickDelete = LocalCompositionOnClickBucketItemDelete.current

	val closeSheet = LocalCompositionCloseBottomSheet.current

	var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = bucketItemObject?.thumbnail) {
		_thumbnail = bucketItemObject?.thumbnail?.decodeBase64ToBitmap()
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(rememberScrollState())
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Link",
			icon = R.drawable.ic_link,
		)

		Spacer(modifier = Modifier.height(8.dp))

		_thumbnail?.let {
			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(it)
					.crossfade(300)
					.build(),
				placeholder = null,
				contentDescription = null,
				contentScale = ContentScale.FillHeight,
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1.91f)
					.padding(24.dp, 0.dp)
					.clip(RoundedCornerShape(24.dp)),
			)
		}

		Spacer(modifier = Modifier.height(4.dp))

		KeyValueText(
			key = "Title",
			value = openGraphResult?.title,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(4.dp))

		KeyValueText(
			key = "Description",
			value = openGraphResult?.description,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(4.dp))

		KeyValueText(
			key = "URL",
			value = bucketItemObject?.key,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
			maxLines = 8,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			try {
				uriHandler.openUri(bucketItemObject?.key !!)
			} catch (e : Exception) {
				Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			Button(
				onClick = { if (bucketItemObject != null) onClickLock(bucketItemObject) },
				colors = ButtonDefaults.buttonColors(
					containerColor = if (bucketItemObject?.isLocked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
					contentColor = if (bucketItemObject?.isLocked == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
				),
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_lock_close),
					contentDescription = "Lock",
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Lock")
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				onClick = { if (bucketItemObject != null) onClickFavourite(bucketItemObject) },
				colors = ButtonDefaults.buttonColors(
					containerColor = if (bucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
					contentColor = if (bucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
				),
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_favourite),
					contentDescription = "Favorite",
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Favourite")
			}
		}

		Button(
			onClick = {
				if (bucketItemObject != null) onClickDelete(bucketItemObject)
				else Toast.makeText(context, "Error deleting the item", Toast.LENGTH_SHORT).show()
				closeSheet()
			},
			colors = ButtonDefaults.buttonColors(
				containerColor = Color.Companion.DeleteContainer,
				contentColor = Color.Companion.DeleteContent,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			Text(text = "Delete")
		}

		Button(
			onClick = {
				try {
					uriHandler.openUri(openGraphResult?.url !!)
				} catch (e : Exception) {
					Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
				}
			},
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			Text(text = "Open Link")
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
