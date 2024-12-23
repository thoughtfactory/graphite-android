package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ApiStatus
import com.syncodec.graphite.di.network.OpenLibraryTitleSearchResult
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.SearchResultStatusView
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextFieldDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Preview
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Preview
@Composable
fun AddBookBottomSheet() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	val bucketObject by viewModel.bucketObject.collectAsState()

	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }
	var isTextFocused by remember { mutableStateOf(false) }

	var status : Status by remember { mutableStateOf(Status.INIT) }
	var openLibraryTitleSearchResult : OpenLibraryTitleSearchResult? by remember { mutableStateOf(null) }

	val onSearch = {
		status = Status.LOADING
		keyboardController?.hide()
		openLibraryTitleSearchResult = null
		viewModel.searchForBook(query = queryText) { apiResult ->
			scope.launch(Dispatchers.Main) {
				when (apiResult.status) {
					ApiStatus.LOADING -> {
						status = Status.LOADING
					}

					ApiStatus.SUCCESS -> {
						status = Status.LOADED
						openLibraryTitleSearchResult = apiResult.data
					}

					ApiStatus.ERROR -> {
						status = Status.ERROR
						openLibraryTitleSearchResult = null
					}
				}
			}
		}
	}

	GenericBottomSheet(
		title = "Umm... What was that book",
		icon = R.drawable.ic_book,
	) {
		BottomSheetTextField(
			value = queryText,
			placeholder = "Search for books",
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_search,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface()
				) { onSearch() }
			},
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = { onSearch() },
				onDone = { onSearch() }
			),
			colors = BottomSheetTextFieldDefaults.textFieldColors(),
			onValueChange = { queryText = it },
		)

		Spacer(modifier = Modifier.height(8.dp))

		AnimatedContent(
			targetState = status,
			label = "status_animation"
		) {
			when (it) {
				Status.INIT -> {
					SearchResultStatusView(
						imageId = R.drawable.il_bucket_book_search,
						text = "A fiction, a biography maybe?",
						contentDescription = "Search for books"
					)
				}

				Status.LOADING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp),
					) { LoadingView() }
				}

				Status.LOADED -> LoadedView(openLibraryTitleSearchResult = openLibraryTitleSearchResult) { bookData ->
					keyboardController?.hide()

					if (bucketObject == null || bookData.key == null) {
						Toast.makeText(context, "Error adding book to bucket", Toast.LENGTH_SHORT).show()
					} else {
						Intent(context, BucketItemActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.IsNew.name, true)
							putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketObject?.id?.bytes)
							putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.BOOK.name)
							putExtra(Extra.Companion.Extra.BOOK_ID.name, bookData.key)
							putExtra(Extra.Companion.Extra.BUCKET_EXTRA_DATA.name, bookData)

							context.startActivity(this)
						}
					}
				}

				Status.ERROR -> {
					SearchResultStatusView(
						imageId = R.drawable.il_bucket_search_error,
						text = "Oops, something went wrong. Try again?",
						contentDescription = "Error getting search results"
					)
				}
			}
		}
	}
}

@Composable
private fun LoadedView(
	openLibraryTitleSearchResult : OpenLibraryTitleSearchResult? = null,
	onClickBook : (BucketItemObject.Companion.BucketItemData.BookData) -> Unit = {}
) {
	if (openLibraryTitleSearchResult?.docs?.isEmpty() == true) {
		SearchResultStatusView(
			imageId = R.drawable.il_bucket_search_not_found,
			text = "Uh oh, we couldn't find anything. Try again?",
			contentDescription = "Book not found"
		)
	} else {
		LazyVerticalGrid(
			columns = GridCells.Fixed(3),
			modifier = Modifier
		) {
			openLibraryTitleSearchResult?.docs?.forEach { bookData ->
				if (bookData != null) {
					item {
						BookCard(
							bookData = bookData,
							onClick = { onClickBook(bookData) }
						)
					}
				}
			}
		}
	}
}

@Composable
private fun BookCard(
	bookData : BucketItemObject.Companion.BucketItemData.BookData,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.padding(8.dp),
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.71f), MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onClick() }
		) {
			var isError by remember { mutableStateOf(false) }
			bookData.coverI?.let {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data("https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg")
						.crossfade(300)
						.build(),
					placeholder = null,
					onError = { isError = true },
					contentDescription = bookData.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
				)
			} ?: Text(
				text = "No cover",
				modifier = Modifier.padding(8.dp),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
			)

			if (isError) {
				Text(
					text = "No cover",
					modifier = Modifier.padding(8.dp),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = bookData.title ?: "Untitled",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (bookData.title == null) FontStyle.Italic else FontStyle.Normal,
		)

		Spacer(modifier = Modifier.height(4.dp))
		var author = ""

		bookData.authorList?.forEachIndexed { index, s -> author += if (index == 0) s else ", $s" }
		Text(
			text = "~ $author",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
		)

		bookData.firstPublishYear?.take(4)?.let {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = it,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				fontStyle = FontStyle.Italic,
			)
		}
	}
}
