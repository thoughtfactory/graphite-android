package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewLinkBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	bucketItemObject: BucketItemObject? = null,
	onToggleFavourite: (BucketItemObject) -> Unit = {},
	onToggleLock: (BucketItemObject) -> Unit = {},
) {
	val context = LocalContext.current

	val clipboardManager = LocalClipboardManager.current

	val isAuthenticated = LocalIsAuthenticated.current

	val openGraphResult by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.getOpenGraphResult() } }
	val thumbnail by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.thumbnail?.decodeBase64ToBitmap() } }
	val isFavourite by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.isFavourite == true } }
	val isLocked by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.isLocked == true } }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.link),
		) {
			thumbnail?.let {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(192.dp)
						.padding(horizontal = 2.dp)
						.clip(MaterialTheme.shapes.medium)
				) {
//					if (Build.VERSION.SDK_INT >= 31) {
//						AsyncImage(
//							model = ImageRequest.Builder(context)
//								.data(bitmap)
//								.build(),
//							placeholder = null,
//							contentDescription = null,
//							contentScale = ContentScale.Crop,
//							modifier = Modifier
//								.fillMaxSize()
//								.blur(24.dp)
//						)
//					} else {
//						Box(
//							modifier = Modifier
//								.fillMaxSize()
//								.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f))
//						)
//					}
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(thumbnail)
							.build(),
						placeholder = null,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
				}
			}

			Spacer(modifier = Modifier.height(4.dp))

			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.url),
				value = openGraphResult?.url ?: bucketItemObject?.key ?: stringResource(id = R.string.unavailable),
				onClick = {
					(openGraphResult?.url ?: bucketItemObject?.key)?.let {
						Intent(Intent.ACTION_VIEW).apply {
							data = Uri.parse(it)
							context.startActivity(this)
						}
					}
				},
				onLongClick = { openGraphResult?.url?.let { clipboardManager.setText(AnnotatedString(it)) } }
			)
			openGraphResult?.title?.let {
				GenericBottomSheetInfo2(
					key = stringResource(id = R.string.title),
					value = it,
					onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
				)
			}
			openGraphResult?.description?.let {
				GenericBottomSheetInfo2(
					key = stringResource(id = R.string.description),
					value = it,
					onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
				)
			}

			Spacer(modifier = Modifier.height(2.dp))

			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Button(
					shape = MaterialTheme.shapes.medium,
					colors = if (isFavourite) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
					modifier = Modifier.weight(1f),
					onClick = { bucketItemObject?.let { onToggleFavourite(it) } }
				) {
					if (isFavourite) Icon(
						painter = painterResource(id = R.drawable.ic_fa_heart_solid),
						contentDescription = stringResource(id = R.string.toggle_favourite),
						tint = Color.FavouriteContainer,
						modifier = Modifier.requiredSize(IconButtonSize)
					)
					else Icon(
						painter = painterResource(id = R.drawable.ic_fa_heart),
						contentDescription = stringResource(id = R.string.toggle_favourite),
						modifier = Modifier.requiredSize(IconButtonSize)
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(text = stringResource(id = R.string.favourite))
				}
				Spacer(modifier = Modifier.width(6.dp))
				Button(
					shape = MaterialTheme.shapes.medium,
					colors = if (isLocked) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
					modifier = Modifier.weight(1f),
					onClick = {
						if (isAuthenticated) bucketItemObject?.let { onToggleLock(it) }
						else Toast.makeText(context, context.getText(R.string.toast_not_authenticated), Toast.LENGTH_SHORT).show()
					}
				) {
					if (isLocked) Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
						contentDescription = stringResource(id = R.string.toggle_lock),
						tint = Color.LockClosedContainer,
						modifier = Modifier.requiredSize(IconButtonSize)
					)
					else Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_open),
						contentDescription = stringResource(id = R.string.toggle_lock),
						modifier = Modifier.requiredSize(IconButtonSize)
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(text = stringResource(id = R.string.lock))
				}
			}
		}
	}
}
