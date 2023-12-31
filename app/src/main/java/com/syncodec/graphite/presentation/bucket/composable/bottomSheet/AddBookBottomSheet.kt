package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.OpenLibraryTitleSearchResult
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.bucketItem.activity.BookBucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.getGraphiteTextFieldColors
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddBookBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	parentId: RealmUUID? = null,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusManager = LocalFocusManager.current

	val openLibraryApi : OpenLibraryApi = koinInject()

	var queryText by rememberSaveable { mutableStateOf("") }

	var openLibraryNetworkRequest by remember { mutableStateOf<NetworkRequest<OpenLibraryTitleSearchResult>>(NetworkRequest.Init) }
	fun searchForBook(title: String) {
		scope.launch(Dispatchers.IO) {
			openLibraryApi.searchForBook(query = title) { openLibraryResponse -> openLibraryNetworkRequest = openLibraryResponse }
		}
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.add_book),
		) {
			OutlinedTextField(
				value = queryText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { queryText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.search_for_book)) },
				trailingIcon = {
					Row {
						CancelButton { queryText = "" }
						SearchButton {
							focusManager.clearFocus()
							keyboardController?.hide()
							searchForBook(queryText)
						}
						Spacer(modifier = Modifier.width(4.dp))
					}
				},
				maxLines = 1,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search,
				),
				keyboardActions = KeyboardActions(
					onSearch = {
						focusManager.clearFocus()
						keyboardController?.hide()
						searchForBook(queryText)
					}
				),
				colors = getGraphiteTextFieldColors(),
				modifier = Modifier.fillMaxWidth()
			)

			AnimatedContent(
				targetState = openLibraryNetworkRequest,
				label = "bookSearchPreview_animation",
				modifier = Modifier.fillMaxWidth()
			) { openLibraryRequest1 ->
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					when (openLibraryRequest1) {
						is NetworkRequest.Init -> Unit
						is NetworkRequest.Loading -> LoadingView(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
						)

						is NetworkRequest.Success -> openLibraryRequest1.data.docs?.filterNotNull()?.let {
							BookGrid(
								bookDataList = openLibraryRequest1.data.docs.filterNotNull(),
							) {
								Intent(context, BookBucketItemActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.IsNew.name, true)
									putExtra(Extra.Companion.Extra.BUCKET_ID.name, parentId?.bytes)
									putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.BOOK.name)
									putExtra(Extra.Companion.Extra.BOOK_ID.name, it.key)

									context.startActivity(this)
								}
							}
						} ?: Unit

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
		}
	}
}

@Preview
@Composable
private fun BookGrid(
	bookDataList: List<BucketItemData.BookData.OpenLibraryBookData> = listOf(),
	onClick: (BucketItemData.BookData.OpenLibraryBookData) -> Unit = {}
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(3),
		modifier = Modifier
	) {
		items(bookDataList) { bookData ->
			BookCard(
				bookData = bookData,
				onClick = { onClick(bookData) }
			)
		}
	}
}

@Composable
private fun BookCard(
	bookData: BucketItemData.BookData.OpenLibraryBookData,
	onClick: () -> Unit
) {
	val context = LocalContext.current
	val openLibraryApi : OpenLibraryApi = koinInject()

	var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = bookData.coverI) {
		withContext(Dispatchers.IO) {
			thumbnail = null
			thumbnail = openLibraryApi.retrieveBookCover(bookData.coverI)
		}
	}

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.padding(8.dp),
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail)
				.diskCachePolicy(CachePolicy.ENABLED)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.diskCacheKey("book_${bookData.coverI}")
				.memoryCacheKey("book_${bookData.coverI}")
				.crossfade(ANIMATION_DURATION_MILLIS)
				.build(),
			loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp)) },
			error = {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier.fillMaxSize()
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_bucket_book),
						contentDescription = stringResource(id = R.string.thumbnail),
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
						modifier = Modifier.requiredSize(32.dp)
					)
				}
			},
			contentDescription = bookData.title,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
				.clip(MaterialTheme.shapes.large)
				.clickable { onClick() }
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = bookData.title ?: stringResource(R.string.untitled),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (bookData.title == null) FontStyle.Italic else FontStyle.Normal,
		)

		Spacer(modifier = Modifier.height(4.dp))
		var author = ""

		bookData.authorList.forEachIndexed { index, s -> author += if (index == 0) s else ", $s" }
		Text(
			text = "~ $author",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
		)

		bookData.firstPublishYear?.let {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = "$it",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				fontStyle = FontStyle.Italic,
			)
		}
	}
}
