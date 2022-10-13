package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.OpenLibraryTitleSearchResult
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.common.ClimateChangeMessage
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.text.LargeTextField
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddBookBottomSheet(
	closeSheet : () -> Unit
) {
	val activity : BucketActivity = LocalContext.current as BucketActivity
	val viewModel : BucketViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }
	var isTextFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	var status : Status by remember { mutableStateOf(Status.INIT) }
	var openLibraryTitleSearchResult : OpenLibraryTitleSearchResult? by remember { mutableStateOf(null) }

	val lazyGridState = rememberLazyGridState()

	val openLibrarySearchTypeList = listOf(
		StateData(title = "All", icon = R.drawable.ic_state,),
		StateData(title = "Title", icon = R.drawable.ic_title,),
		StateData(title = "Author", icon = R.drawable.ic_book,),
		StateData(title = "ISBN", icon = R.drawable.ic_book,),
	)
	var openLibrarySearchType by rememberSaveable { mutableStateOf(0) }


	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
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
							val onSearchResult : (OpenLibraryTitleSearchResult?) -> Unit = {
								if (it == null) {
									status = Status.ERROR
								} else {
									openLibraryTitleSearchResult = it
									status = Status.LOADED
								}
							}

							when (openLibrarySearchType) {
								0 -> Repository.openLibraryApi.searchForBook(
									query = queryText,
									requestType = OpenLibraryApi.OpenLibraryApiRequestType.QUERY,
									onResponse = onSearchResult
								)
								1 -> Repository.openLibraryApi.searchForBook(
									query = queryText,
									requestType = OpenLibraryApi.OpenLibraryApiRequestType.TITLE,
									onResponse = onSearchResult
								)
								2 -> Repository.openLibraryApi.searchForBook(
									query = queryText,
									requestType = OpenLibraryApi.OpenLibraryApiRequestType.QUERY,
									onResponse = onSearchResult
								)
							}
						} catch (e : SocketTimeoutException) {
//							TODO update error message and image
							scope.launch(Dispatchers.Main) {
								Toast.makeText(activity, "Timeout getting search results", Toast.LENGTH_SHORT).show()
							}
							status = Status.ERROR
							e.printStackTrace()
						} catch (e : Exception) {
//							TODO update error message and image
							status = Status.ERROR
							e.printStackTrace()
						}
					}
				}
			)
		)

		Spacer(modifier = Modifier.height(8.dp))

//		StateButton(
//			stateList = openLibrarySearchTypeList,
//			containerColor = MaterialTheme.colorScheme.background,
//			currentState = openLibrarySearchType,
//			modifier = Modifier
//				.fillMaxWidth()
//				.height(36.dp)
//				.padding(24.dp, 0.dp)
//		) { openLibrarySearchType = it }
//
//		Spacer(modifier = Modifier.height(4.dp))

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
//					TODO    What if list is empty?
					LazyVerticalGrid(
						columns = GridCells.Fixed(3),
						state = lazyGridState,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						openLibraryTitleSearchResult?.docs?.forEach { bookData ->
							if (bookData != null) {
								item {
									BookCard(
										bookData = bookData,
									) {
										focusRequester.freeFocus()
										keyboardController?.hide()

										if (viewModel.bucketObject.value == null || bookData.key == null) {
											Toast.makeText(activity, "Error adding book to bucket", Toast.LENGTH_SHORT).show()
										} else {
											Intent(activity, BucketItemActivity::class.java).apply {
												putExtra(Extra.Companion.Constant.IS_NEW.name, true)
												putExtra(Extra.Companion.Constant.BUCKET_ID.name, viewModel.bucketObject.value !!.id.toString())
												putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.BOOK.name)
												putExtra(Extra.Companion.Constant.BOOK_ID.name, bookData.key)
												putExtra(Extra.Companion.Constant.BUCKET_EXTRA_DATA.name, bookData)

												activity.startActivity(this)
											}
										}
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

@Composable
private fun BookCard(
	bookData : BookData,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.padding(8.dp),
	) {
		Box(
			modifier = Modifier
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.47f), RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.clickable { onClick() }
		) {
			if (bookData.coverI != null) {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data("https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg")
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = bookData.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = bookData.title ?: "Untitled",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontStyle = if (bookData.title == null) FontStyle.Italic else FontStyle.Normal,
		)

		Spacer(modifier = Modifier.height(4.dp))
		var author = ""

		bookData.authorList?.forEachIndexed { index, s -> author += if (index == 0) s else ", $s" }
		Text(
			text = "~ $author",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)

		bookData.firstPublishYear?.take(4)?.let {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontStyle = FontStyle.Italic,
			)
		}
	}
}
