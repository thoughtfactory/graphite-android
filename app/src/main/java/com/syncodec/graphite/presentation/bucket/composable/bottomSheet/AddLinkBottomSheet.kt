package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.di.network.ApiStatus
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.SearchResultStatusView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextFieldDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.text.KeyValueText
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Preview
@Composable
fun AddLinkBottomSheet(
	closeSheet : () -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	val keyboardController = LocalSoftwareKeyboardController.current
	val clipboardManager = LocalClipboardManager.current

	var urlText by remember { mutableStateOf("") }

	var status : Status by remember { mutableStateOf(Status.INIT) }

	var openGraphResult by remember { mutableStateOf<OpenGraphResult?>(null) }
	var bitmap by remember { mutableStateOf<Bitmap?>(null) }

	fun getLinkPreview(url : String) {
		status = Status.LOADING
		openGraphResult = null
		bitmap = null
		viewModel.getLinkPreview(url) { apiResult ->
			scope.launch(Dispatchers.Main) {
				when (apiResult.status) {
					ApiStatus.LOADING -> status = Status.LOADING
					ApiStatus.SUCCESS -> {
						openGraphResult = apiResult.data?.first
						bitmap = apiResult.data?.second
						status = Status.LOADED
					}

					ApiStatus.ERROR -> {
						status = Status.ERROR
						openGraphResult = null
						bitmap = null
					}
				}
			}
		}
	}

	fun putLink(url : String) {
		closeSheet()
		viewModel.putLink(url = url)
	}

	GenericBottomSheet(
		title = "Add Link",
		icon = R.drawable.ic_link,
	) {
		BottomSheetTextField(
			value = urlText,
			placeholder = "http:// or https://",
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_paste,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface()
				) {
					try {
						if (clipboardManager.hasText()) clipboardManager.getText()?.let { clipboardText -> urlText = clipboardText.text }
						else Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
					} catch (e : Exception) {
						Toast.makeText(context, "Error copying text from clipboard", Toast.LENGTH_SHORT).show()
					}
				}
			},
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = {
					putLink(urlText)
					urlText = ""
				},
				onDone = {
					putLink(urlText)
					urlText = ""
				}
			),
			colors = BottomSheetTextFieldDefaults.textFieldColors(),
			onValueChange = { urlText = it },
		)

		Spacer(modifier = Modifier.height(2.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
					contentColor = MaterialTheme.colorScheme.onSurface
				),
				shape = MaterialTheme.shapes.medium,
				onClick = {
					getLinkPreview(urlText)
					keyboardController?.hide()
				},
				modifier = Modifier.weight(1f),
			) {
				Text(text = "Preview")
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				onClick = {
					putLink(urlText)
					urlText = ""
					closeSheet()
				},
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Save")
			}
		}

		Spacer(modifier = Modifier.height(2.dp))

		AnimatedContent(
			targetState = status,
			transitionSpec = { scaleIn(tween(300)) + fadeIn(tween(300)) with scaleOut(tween(300)) + fadeOut(tween(300)) }
		) { status1 ->
			when (status1) {
				Status.INIT -> SearchResultStatusView(
					imageId = R.drawable.il_bucket_link_search,
					text = "Spotify, YouTube, Netflix anything you want to save!",
					contentDescription = "Add Link",
				)

				Status.LOADING -> Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxWidth()
						.heightIn(256.dp)
				) {
					CircularProgressIndicator(
						color = MaterialTheme.colorScheme.primary,
						strokeWidth = 4.dp
					)
				}

				Status.LOADED -> {
					openGraphResult?.let {
						LinkPreview(
							openGraphResult = it,
							bitmap = bitmap,
						)
					} ?: SearchResultStatusView(
						imageId = R.drawable.il_bucket_link_search,
						text = "No preview available",
						contentDescription = "No preview available",
					)
				}

				Status.ERROR -> SearchResultStatusView(
					imageId = R.drawable.il_bucket_link_search,
					text = "Something went wrong",
					contentDescription = "Something went wrong",
				)
			}
		}
	}
}

@Preview
@Composable
private fun LinkPreview(
	openGraphResult : OpenGraphResult = OpenGraphResult(
		title = "Title",
		description = "Description",
		url = "https://www.google.com",
	),
	bitmap : Bitmap? = null,
) {
	val context = LocalContext.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
	) {
		Thumbnail(thumbnail = bitmap)

		KeyValueText(
			key = "Title",
			value = openGraphResult.title,
			modifier = Modifier.fillMaxWidth()
		)

		KeyValueText(
			key = "Description",
			value = openGraphResult.description,
			modifier = Modifier.fillMaxWidth()
		)

		KeyValueText(
			key = "URL",
			value = openGraphResult.url,
			maxLines = 8,
			modifier = Modifier.fillMaxWidth()
		) {
			try {
				context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openGraphResult.url)))
			} catch (e : Exception) {
				Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
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
