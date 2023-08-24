package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.di.network.OpenGraphApi
import com.syncodec.graphite.di.network.OpenGraphResponse
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.ClearButton
import com.syncodec.graphite.presentation.common.button.PasteButton
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.utils.ContentStatus


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddLinkBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	onAddLink: (String, Int) -> Unit = { _, _ -> },
) {
	var urlText by rememberSaveable { mutableStateOf("") }
	var contentStatus by remember { mutableStateOf<ContentStatus<Pair<OpenGraphResult, Bitmap?>>>(ContentStatus.Init) }

	fun getLinkPreview(url: String) {
		OpenGraphApi.getData(url = url) { openGraphResponse ->
			contentStatus = when (openGraphResponse) {
				is OpenGraphResponse.Loading -> ContentStatus.Loading
				is OpenGraphResponse.Success -> ContentStatus.Loaded(Pair(openGraphResponse.openGraphResult, openGraphResponse.bitmap))
				is OpenGraphResponse.InvalidUrl -> ContentStatus.Error(message = "Invalid url")
				is OpenGraphResponse.Error -> ContentStatus.Error(message = "Unknown error")
			}
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
						ClearButton { urlText = "" }
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
					contentStatus = ContentStatus.Init
				},
				modifier = Modifier.fillMaxWidth()
			)

			AnimatedContent(
				targetState = contentStatus,
				label = "linkPreview_animation",
				modifier = Modifier.fillMaxWidth()
			) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					when (it) {
						is ContentStatus.Init -> Unit
						is ContentStatus.Loading -> LoadingView(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
						)

						is ContentStatus.LoadedEmpty -> Unit
						is ContentStatus.Loaded -> LinkPreview(openGraphResult = it.data.first, bitmap = it.data.second)
						is ContentStatus.Error -> InfoCard(
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
					contentStatus = ContentStatus.Init
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
	openGraphResult: OpenGraphResult = OpenGraphResult(
		title = "Title",
		description = "Description",
		url = "https://www.google.com",
	),
	bitmap: Bitmap? = null
) {
	val context = LocalContext.current

	val clipboardManager = LocalClipboardManager.current

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		AnimatedVisibility(
			visible = bitmap != null,
			label = "thumbnail_visibility"
		) {
			Column {
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
							.data(bitmap)
							.build(),
						placeholder = null,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
				}
				Spacer(modifier = Modifier.height(4.dp))
			}
		}

		GenericBottomSheetInfo2(
			key = stringResource(id = R.string.url),
			value = openGraphResult.url ?: stringResource(id = R.string.unavailable),
			onLongClick = { openGraphResult.url?.let { clipboardManager.setText(AnnotatedString(it)) } }
		)

		openGraphResult.title?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.title),
				value = it,
				onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
			)
		}

		openGraphResult.description?.let {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.description),
				value = it,
				onLongClick = { clipboardManager.setText(AnnotatedString(it)) }
			)
		}
	}
}
