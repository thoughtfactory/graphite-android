package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.syncodec.graphite.R
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryApi2
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryTitleSearchResult2
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.SearchInitView
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun AddBookBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    onAddBook: (OLBookSearchResult) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val openLibraryApi2: OpenLibraryApi2 = koinInject()

    val bookTitleTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = true)

    var olTitleSearchResultNetworkResponse: NetworkResponse<OpenLibraryTitleSearchResult2> by remember { mutableStateOf(value = NetworkResponse.Init) }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.add_book),
            scrollable = olTitleSearchResultNetworkResponse is NetworkResponse.Init,
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            fun searchForBookUsingTitle(page: Int) {
                keyboardController?.hide()
                val bookTitleValidationResult = bookTitleTextFieldController.validate { it.isNotBlank() }
                if (bookTitleValidationResult.isValidated) {
                    scope.launch(context = Dispatchers.IO) {
                        openLibraryApi2.searchForBook(query = bookTitleValidationResult.text, page = page) { networkResponse -> olTitleSearchResultNetworkResponse = networkResponse }
                    }
                }
            }

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2.BottomSheetTextField(
                controller = bookTitleTextFieldController,
                label = stringResource(id = R.string.search_on_open_library),
                placeholder = stringResource(id = R.string.search_for_title),
                errorMessage = stringResource(id = R.string.book_item_title_error),
                keyboardOptions = GenericTextField2.Options.getTextSearchKeyboardOptionsDefault(),
                keyboardActions = GenericTextField2.Actions.getKeyboardActions { searchForBookUsingTitle(page = 1) },
                suffixIcon = {
                    GraIconButton.ClearTextButton { bookTitleTextFieldController.reset() }
                    GraIconButton.SearchButton { searchForBookUsingTitle(page = 1) }
                },
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            AnimatedContent(
                targetState = olTitleSearchResultNetworkResponse,
                transitionSpec = { AnimationDefaults.Fade },
                modifier = Modifier.fillMaxWidth()
            ) {
                when (it) {
                    is NetworkResponse.Init -> SearchInitView()
                    is NetworkResponse.Loading -> LoadingView()
                    is NetworkResponse.Error -> ErrorView()
                    is NetworkResponse.Success -> SuccessView(
                        olTitleSearchResult = it.data,
                        onClickPrevious = { searchForBookUsingTitle(page = it) },
                        onClickNext = { searchForBookUsingTitle(page = it) },
                        onClickBook = onAddBook,
                    )
                }
            }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.requiredSize(size = 32.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun ErrorView() {
    Text("ErrorView")
}

@Composable
private fun SuccessView(
    olTitleSearchResult: OpenLibraryTitleSearchResult2,
    onClickPrevious: (page: Int) -> Unit = {},
    onClickNext: (page: Int) -> Unit = {},
    onClickBook: (OLBookSearchResult) -> Unit = {}
) {
    val page by remember(key1 = olTitleSearchResult) { derivedStateOf { 1 + ((olTitleSearchResult.start ?: 0) / 9) } }
    val totalPages by remember(key1 = olTitleSearchResult) { derivedStateOf { 1 + ((olTitleSearchResult.numFound ?: 0) / 9) } }

    Column {
        Spacer(modifier = Modifier.height(height = 8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (page != 1) Row {
                GraIconButton.PreviousButton { onClickPrevious(page - 1) }
                Spacer(modifier = Modifier.width(width = 4.dp))
            }
            Spacer(modifier = Modifier.width(width = 8.dp))
            Text(text = "Found ${olTitleSearchResult.numFound} results")
            Spacer(modifier = Modifier.weight(weight = 1f))

            Text(text = "page : $page", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(width = 8.dp))

            if (page < totalPages) GraIconButton.NextButton { onClickNext(page + 1) }
        }

        Spacer(modifier = Modifier.height(height = 4.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
        ) {
            itemsIndexed(
                items = olTitleSearchResult.docs,
                key = { index, it -> it.key ?: index }
            ) { index, bookSearchResult ->
                ShowView(olBookSearchResult = bookSearchResult) { onClickBook(bookSearchResult) }
            }
        }
    }
}

@Composable
private fun ShowView(
    olBookSearchResult: OLBookSearchResult,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        if (olBookSearchResult.coverI == null) Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 0.675f)
                .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.large)
                .clip(shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick, onClickLabel = olBookSearchResult.title),
        ) {
            Text(
                text = stringResource(id = R.string.no_cover_image),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://covers.openlibrary.org/b/id/${olBookSearchResult.coverI}-M.jpg")
                .crossfade(durationMillis = AnimationDefaults.ANIMATION_TIME)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 0.675f)
                .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.large)
                .clip(shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick, onClickLabel = olBookSearchResult.title)
        )
        Spacer(modifier = Modifier.height(height = 6.dp))
        olBookSearchResult.title?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp)) }
        Spacer(modifier = Modifier.height(height = 2.dp))
        olBookSearchResult.authorName.firstOrNull()?.let { Text(text = "~ $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp)) }
    }
}
