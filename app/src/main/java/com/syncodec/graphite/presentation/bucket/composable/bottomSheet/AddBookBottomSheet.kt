package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.R
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.OpenLibraryTitleSearchResult
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.custom.ClimateChangeMessage
import com.syncodec.graphite.presentation.custom.text.LargeTextField
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.tone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddBookBottomSheet(
	closeSheet: () -> Unit
) {
	val objectMapper = jsonMapper { addModule(kotlinModule()) }
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val activity: BucketActivity = LocalContext.current as BucketActivity
	val viewModel: BucketViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }
	var isTextFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	var status: Status by remember { mutableStateOf(Status.INIT) }
	var openLibraryTitleSearchResult: OpenLibraryTitleSearchResult? by remember { mutableStateOf(null) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Umm... What was that book",
			icon = R.drawable.ic_book,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier.padding(24.dp, 0.dp),
			text = queryText,
			placeholder = "Search for books",
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			isFocused = isTextFocused,
			focusRequester = focusRequester,
			onFocusChanged = { isTextFocused = it },
			onValueChanged = { queryText = it },
			keyboardActions = KeyboardActions(
				onSearch = {
					status = Status.LOADING
					focusRequester.freeFocus()
					keyboardController?.hide()
					openLibraryTitleSearchResult = null
					scope.launch(Dispatchers.IO) {
						try {
							Repository
								.openLibraryApi
								.searchForTitle(queryText) { response ->
									if (response?.body == null) {
										status = Status.ERROR
									} else {
										openLibraryTitleSearchResult =
											objectMapper.readValue(response.body!!.string())
										status = Status.LOADED
									}
								}
						} catch (e: SocketTimeoutException) {
//								TODO update error message and image
							scope.launch(Dispatchers.Main) {
								Toast.makeText(
									activity,
									"Timeout getting search results",
									Toast.LENGTH_SHORT
								).show()
							}
							status = Status.ERROR
							e.printStackTrace()
						} catch (e: Exception) {
//								TODO update error message and image
							status = Status.ERROR
							e.printStackTrace()
						}
					}
				}
			)
		)

		Spacer(modifier = Modifier.height(8.dp))

		AnimatedContent(targetState = status) {
			when (it) {
				Status.INIT -> ClimateChangeMessage()
				Status.LOADING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp),
					) { LoadingView() }
				}
				Status.LOADED -> {
//						TODO    What if list is empty?
					LazyVerticalGrid(
						columns = GridCells.Fixed(3),
						modifier = Modifier.padding(8.dp)
					) {
						openLibraryTitleSearchResult?.docs?.forEach { bookMetadata ->
							item {
								BookCard(
									bookData = bookMetadata
								) {
									focusRequester.freeFocus()
									keyboardController?.hide()

									if (viewModel.bucketObject.value != null && bookMetadata.key != null) {
										Intent(activity, BucketItemActivity::class.java).apply {
											putExtra(Extra.Companion.Constant.IS_NEW.name, true)
											putExtra(Extra.Companion.Constant.BUCKET_ID.name, viewModel.bucketObject.value!!.id.toString())
											putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.BOOK.name)
											putExtra(Extra.Companion.Constant.BOOK_KEY.name, bookMetadata.key)
											putExtra(Extra.Companion.Constant.EXTRA_DATA.name, bookMetadata)

											activity.startActivity(this)
										}
									} else {
										Toast.makeText(activity, "Error adding book to bucket", Toast.LENGTH_SHORT).show()
									}
								}
							}
						}
					}
				}
				Status.ERROR -> {
					Column(
						modifier = Modifier.heightIn(256.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Spacer(modifier = Modifier.height(24.dp))
						Image(
							painter = painterResource(id = R.drawable.il_error),
							contentDescription = "No result found",
							modifier = Modifier.fillMaxWidth(0.71f)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = "Sorry, we could not find that",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							textAlign = TextAlign.Center,
							modifier = Modifier.fillMaxWidth()
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookCard(
	bookData: BookData,
	onClick: () -> Unit
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp),
	) {
		Card(
			colors = CardDefaults.cardColors(
				MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
			),
			elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier.aspectRatio(0.75f),
			onClick = { onClick() }
		) {
			if (bookData.coverI != null) {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data("https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg")
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = bookData.key,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}

		Text(
			text = bookData.title ?: "",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)

		var author = ""
		bookData.authorList?.forEach { author += " $it" }
		Text(
			text = "~ $author",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
