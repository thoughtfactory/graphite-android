package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.di.network.OpenGraphApi
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.button.PasteButton
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetInfo
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.getGraphiteTextFieldColors
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddLinkBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	onAddLink: (String, Int) -> Unit = { _, _ -> },
) {
	val scope = rememberCoroutineScope()
	val openGraphApi: OpenGraphApi = koinInject()

	var urlText by rememberSaveable { mutableStateOf("") }
	var linkDataNetworkRequest by remember { mutableStateOf<NetworkRequest<BucketItemData.LinkData>>(NetworkRequest.Init) }

	fun getLinkPreview(url: String) {
		scope.launch(Dispatchers.IO) {
			openGraphApi.getLinkData(url = url) { linkDataNetworkRequest1 -> linkDataNetworkRequest = linkDataNetworkRequest1 }
		}
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.add_link),
		) {
			OutlinedTextField(
				value = urlText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { urlText = it },
				label = { Text(text = stringResource(id = R.string.link)) },
				placeholder = { Text(text = stringResource(id = R.string.link_example)) },
				trailingIcon = {
					Row {
						CancelButton { urlText = "" }
						PasteButton { urlText = it }
						Spacer(modifier = Modifier.width(4.dp))
					}
				},
				maxLines = 1,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions {
					onAddLink(urlText, 0)
					urlText = ""
					linkDataNetworkRequest = NetworkRequest.Init
				},
				colors = getGraphiteTextFieldColors(),
				modifier = Modifier.fillMaxWidth()
			)

			AnimatedContent(
				targetState = linkDataNetworkRequest,
				label = "linkPreview_animation",
				modifier = Modifier.fillMaxWidth()
			) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					when (it) {
						is NetworkRequest.Init -> Unit
						is NetworkRequest.Loading -> LoadingView(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
						)

						is NetworkRequest.Success -> LinkPreview(linkData = it.data)
						is NetworkRequest.Error -> InfoCard(
							title = stringResource(id = R.string.link_preview_error_title),
							description = stringResource(id = R.string.link_preview_error_description),
							icon = R.drawable.ic_fa_warning,
							colors = InfoCardDefaults.errorCardColors()
						)
					}
					Spacer(modifier = Modifier.height(4.dp))
				}
			}

			OutlinedButton(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.medium,
				enabled = urlText.isNotEmpty(),
				onClick = { getLinkPreview(urlText) }
			) {
				Text(text = stringResource(id = R.string.preview))
			}

			Button(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.medium,
				enabled = urlText.isNotEmpty(),
				onClick = {
					onAddLink(urlText, 0)
					urlText = ""
					linkDataNetworkRequest = NetworkRequest.Init
				}
			) {
				Text(text = stringResource(id = R.string.add_link))
			}
		}
	}
}

@Preview
@Composable
private fun LinkPreview(
	linkData: BucketItemData.LinkData = BucketItemData.LinkData(),
) {
	val context = LocalContext.current

	val clipboardManager = LocalClipboardManager.current

	var isImageAvailable by remember { mutableStateOf(false) }
	LaunchedEffect(key1 = linkData.imagePath) {
		isImageAvailable = true
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		AnimatedVisibility(
			visible = isImageAvailable,
			enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
			exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS)),
			label = "thumbnail_animation"
		) {
			Column {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(linkData.imagePath)
						.diskCachePolicy(CachePolicy.ENABLED)
						.memoryCachePolicy(CachePolicy.ENABLED)
						.diskCacheKey(linkData.imagePath)
						.memoryCacheKey(linkData.imagePath)
						.crossfade(ANIMATION_DURATION_MILLIS)
						.build(),
					loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp)) },
					onError = { isImageAvailable = false },
					contentDescription = stringResource(id = R.string.thumbnail),
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxWidth()
						.height(192.dp)
						.padding(horizontal = 2.dp)
						.clip(MaterialTheme.shapes.medium)
				)

				Spacer(modifier = Modifier.height(4.dp))
			}
		}

		GenericBottomSheetInfo(
			key = stringResource(id = R.string.url),
			value = linkData.url ?: stringResource(id = R.string.unavailable),
			onLongClick = { linkData.url?.let { clipboardManager.setText(AnnotatedString(it)) } }
		)

		linkData.title?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.title),
				value = it,
				onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
			)
		}

		linkData.description?.let {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.description),
				value = it,
				onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
			)
		}
	}
}
