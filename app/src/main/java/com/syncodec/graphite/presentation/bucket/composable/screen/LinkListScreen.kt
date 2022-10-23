package com.syncodec.graphite.presentation.bucket.composable.screen

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Composable
fun LinkListScreen(
	bucketObject: BucketObject
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		bucketObject.bucketItemList.forEachIndexed { index, bucketItemObject ->
			item {
				LinkItem(bucketItemObject = bucketItemObject) {

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
	}
}

@Composable
private fun LinkItem(
	bucketItemObject: BucketItemObject,
	onClick: () -> Unit
) {
	val context = LocalContext.current

	val openGraphResult = bucketItemObject.toOpenGraphResult()

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.clickable { onClick() }
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp, 6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Thumbnail(
				thumbnail = bucketItemObject.thumbnail?.decodeBase64ToBitmap(),
				contentDescription = bucketItemObject.title
			)

			Spacer(modifier = Modifier.width(8.dp))

			Column(
				modifier = Modifier.weight(1f)
			) {

				TitleText(title = bucketItemObject.title)

				Spacer(modifier = Modifier.height(4.dp))

				UrlText(url = openGraphResult?.url)

				SiteNameText(siteName = openGraphResult?.siteName)

				Spacer(modifier = Modifier.height(4.dp))

				openGraphResult?.description?.let {
					DescriptionText(description = it)
					Spacer(modifier = Modifier.height(4.dp))
				}
			}

			Spacer(modifier = Modifier.width(8.dp))

			ActionButton(modifier = Modifier.fillMaxHeight())
		}
	}
}

@Composable
private fun Thumbnail(
	thumbnail: Bitmap?,
	contentDescription: String?
) {
	val context = LocalContext.current
	AsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail)
			.crossfade(300)
			.build(),
		placeholder = null,
		contentDescription = contentDescription,
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.requiredSize(96.dp)
			.clip(RoundedCornerShape(16.dp)),
	)
}

@Composable
private fun TitleText(title: String?) {
	Text(
		text = title ?: "Untitled",
		style = MaterialTheme.typography.titleMedium,
		color = MaterialTheme.colorScheme.onBackground,
		fontWeight = if (title?.isNotEmpty() == true) FontWeight.Black else FontWeight.Normal,
		maxLines = 1
	)
}

@Composable
private fun DescriptionText(
	description: String
) {
	Text(
		text = description,
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onBackground,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun SiteNameText(
	siteName: String?
) {
	Text(
		text = siteName ?: "",
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
		fontWeight = if (siteName?.isNotEmpty() == true) FontWeight.Bold else FontWeight.Normal,
		maxLines = 2,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun UrlText(
	url: String?,
) {
	Text(
		text = url ?: "",
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
		fontWeight = if (url?.isNotEmpty() == true) FontWeight.Bold else FontWeight.Normal,
		maxLines = 2,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun ActionButton(
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.SpaceBetween
	) {
		MenuButton(icon = R.drawable.ic_lock_close) {

		}

		MenuButton(icon = R.drawable.ic_favourite) {

		}
	}
}
