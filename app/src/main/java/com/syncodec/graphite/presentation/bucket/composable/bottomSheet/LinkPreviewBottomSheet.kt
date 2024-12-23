package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.text.KeyValueText
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun LinkPreviewBottomSheet(
	previewBucketItemObjectId : RealmUUID? = null,
	closeSheet : () -> Unit = {}
) {
	val context = LocalContext.current

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	val previewBucketItemObject by viewModel.previewBucketItemObject.collectAsState()

	var openGraphResult by remember { mutableStateOf<OpenGraphResult?>(null) }

	LaunchedEffect(key1 = previewBucketItemObjectId) {
		previewBucketItemObjectId?.let { viewModel.setPreviewBucketItemObject(it) }
	}
	LaunchedEffect(key1 = previewBucketItemObject) {
		openGraphResult = previewBucketItemObject?.getOpenGraphResult()
	}

	GenericBottomSheet(
		title = "Link",
		icon = R.drawable.ic_link,
		enableScroll = true,
	) {

		var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
		LaunchedEffect(key1 = previewBucketItemObject?.thumbnail) {
			_thumbnail = previewBucketItemObject?.thumbnail?.decodeBase64ToBitmap()
		}
		Thumbnail(thumbnail = _thumbnail)

		KeyValueText(
			key = "Title",
			value = openGraphResult?.title,
			modifier = Modifier.fillMaxWidth()
		)

		KeyValueText(
			key = "Description",
			value = openGraphResult?.description,
			modifier = Modifier.fillMaxWidth()
		)

		KeyValueText(
			key = "URL",
			value = previewBucketItemObject?.key,
			maxLines = 8,
			modifier = Modifier.fillMaxWidth()
		) {
			try {
				context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(previewBucketItemObject?.key)))
			} catch (e : Exception) {
				Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			val lockContainerColor by animateColorAsState(
				targetValue = if (previewBucketItemObject?.isLocked == true) MaterialTheme.colorScheme.primary
				else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
			)
			val favouriteContainerColor by animateColorAsState(
				targetValue = if (previewBucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.primary
				else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
			)
			val lockContentColor by animateColorAsState(targetValue = if (previewBucketItemObject?.isLocked == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
			val favouriteContentColor by animateColorAsState(targetValue = if (previewBucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
			Button(
				onClick = {
					if (isAuthenticated) viewModel.toggleLock(bucketItemObject = previewBucketItemObject)
					else onAuthenticationAction(AuthenticatorScreen.Authenticate)
				},
				colors = ButtonDefaults.buttonColors(containerColor = lockContainerColor, contentColor = lockContentColor),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_lock_close),
					contentDescription = "Lock",
					modifier = Modifier.requiredSize(ICON_SIZE)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Lock")
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				onClick = { viewModel.toggleFavourite(bucketItemObject = previewBucketItemObject) },
				colors = ButtonDefaults.buttonColors(containerColor = favouriteContainerColor, contentColor = favouriteContentColor),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_favourite),
					contentDescription = "Favorite",
					modifier = Modifier.requiredSize(ICON_SIZE)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Favourite")
			}
		}

		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer,
			),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				if (previewBucketItemObject != null) viewModel.deleteBucketItem(realmUUIDList = previewBucketItemObject?.id?.let { listOf(it) } ?: listOf())
				else Toast.makeText(context, "Error deleting the item", Toast.LENGTH_SHORT).show()
				closeSheet()
			}
		) {
			Text(text = "Delete")
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth(),
		) {
			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
				),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = {
					try {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openGraphResult?.url)))
					} catch (e : Exception) {
						Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
					}
				}
			) {
				Text(text = "Open Link")
				Spacer(modifier = Modifier.width(6.dp))
				Icon(
					painter = painterResource(id = R.drawable.ic_launch),
					contentDescription = "Open Link",
					modifier = Modifier.requiredSize(20.dp)
				)
			}
			Spacer(modifier = Modifier.width(2.dp))
			MenuButton(
				icon = R.drawable.ic_share,
				colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			) {
				Intent(Intent.ACTION_SEND).apply {
					type = "text/html"
					previewBucketItemObject?.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
					putExtra(Intent.EXTRA_TEXT, openGraphResult?.url)

					if (resolveActivity(context.packageManager) != null) context.startActivity(Intent.createChooser(this, "Share using"))
					else Toast.makeText(context, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}
}

@Composable
private fun ColumnScope.Thumbnail(
	thumbnail : Bitmap?
) {
	val context = LocalContext.current

	this.apply {
		thumbnail?.let {
			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(it)
					.build(),
				placeholder = null,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(0.dp, 128.dp)
					.clip(MaterialTheme.shapes.medium)
			)
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}
