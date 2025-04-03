package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.trakt.TraktApi
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddShowBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    onClickShow: (TraktShowSearchResult) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val traktApi: TraktApi = koinInject()

    val showTitleTextFieldController = rememberTextField2Controller(initialFocus = true)
    var traktSearchResultNetworkResponse: NetworkResponse<List<TraktShowSearchResult>> by remember { mutableStateOf(value = NetworkResponse.Init) }
    var currentPage by remember { mutableStateOf(value = 1) }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {

//        Hiding keyboard doesn't work if controller is initialized outside bottom sheet scope
        val keyboardController = LocalSoftwareKeyboardController.current

        fun searchForShowUsingTitle(showType: BucketItemShow.ShowType, page: Int) {
            keyboardController?.hide()
            val showTitleValidationResult = showTitleTextFieldController.validate { it.isNotBlank() }
            if (showTitleValidationResult.isValidated) {
                scope.launch(context = Dispatchers.IO) {
                    traktApi.searchForShow(query = showTitleValidationResult.text, showType = showType, page = page) { networkResponse ->
                        traktSearchResultNetworkResponse = networkResponse
                        currentPage = page
                    }
                }
            }
        }

        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.add_show),
            scrollable = false
        ) {

            var isShowTypeMenuVisible by remember { mutableStateOf(value = false) }
            var showType by remember { mutableStateOf(BucketItemShow.ShowType.Movie) }

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2(
                controller = showTitleTextFieldController,
                label = stringResource(id = R.string.search_on_trakt_tv),
                placeholder = stringResource(id = R.string.search_for_title),
                errorMessage = stringResource(id = R.string.book_item_title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextSearchKeyboardOptionsDefault(),
                keyboardActions = GenericTextField2Defaults.Actions.getKeyboardActions { searchForShowUsingTitle(showType = showType, page = 1) },
                suffixIcon = {
                    GraIconButton.ClearTextButton { showTitleTextFieldController.reset() }
                    GraIconButton.SearchButton { searchForShowUsingTitle(showType = showType, page = 1) }
                },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(
                    expanded = isShowTypeMenuVisible,
                    onExpandedChange = { isShowTypeMenuVisible = false },
                ) {

                    SuggestionChip(
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.background, labelColor = MaterialTheme.colorScheme.onBackground),
                        shape = MaterialTheme.shapes.medium,
                        onClick = { isShowTypeMenuVisible = true },
                        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                        label = {
                            Text(text = if (showType == BucketItemShow.ShowType.Movie) stringResource(id = R.string.movie) else stringResource(id = R.string.series))
                            Spacer(modifier = Modifier.width(width = 6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fa_caret_down),
                                contentDescription = null,
                                modifier = Modifier.size(size = 18.dp)
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = isShowTypeMenuVisible,
                        onDismissRequest = { isShowTypeMenuVisible = false },
                        containerColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.widthIn(min = 96.dp)
                    ) {
                        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.movie)) }, onClick = { showType = BucketItemShow.ShowType.Movie; isShowTypeMenuVisible = false })
                        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.series)) }, onClick = { showType = BucketItemShow.ShowType.Series; isShowTypeMenuVisible = false })
                    }
                }

                Spacer(modifier = Modifier.weight(weight = 1f))

                AnimatedVisibility(
                    visible = traktSearchResultNetworkResponse is NetworkResponse.Success,
                    enter = AnimationDefaults.FadeEnter,
                    exit = AnimationDefaults.FadeExit,
                    modifier = Modifier
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (currentPage != 1) Row {
                            GraIconButton.PreviousButton { searchForShowUsingTitle(showType = showType, page = currentPage - 1) }
                            Spacer(modifier = Modifier.width(width = 4.dp))
                        }
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        Text(text = "page : $currentPage", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(width = 8.dp))
                        GraIconButton.NextButton { searchForShowUsingTitle(showType = showType, page = currentPage + 1) }
                    }
                }
            }


            AnimatedContent(
                targetState = traktSearchResultNetworkResponse,
                transitionSpec = { AnimationDefaults.Fade },
                modifier = Modifier.fillMaxWidth()
            ) {
                when (it) {
                    is NetworkResponse.Init -> InitView()
                    is NetworkResponse.Loading -> LoadingView()
                    is NetworkResponse.Error -> ErrorView()
                    is NetworkResponse.Success -> SuccessView(
                        traktSearchResultList = it.data,
                        onClickShow = onClickShow,
                    )
                }
            }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}

@Composable
private fun InitView() {
    Text("init view")
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
    traktSearchResultList: List<TraktShowSearchResult>,
    onClickShow: (TraktShowSearchResult) -> Unit = {}
) {
    Column {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
        ) {
            itemsIndexed(
                items = traktSearchResultList,
                key = { index, it -> it.showSearchResult?.ids?.trakt ?: index }
            ) { index, traktSearchResult ->
                ShowView(traktSearchResult = traktSearchResult) { onClickShow(traktSearchResult) }
            }
        }
    }
}

@Composable
private fun ShowView(
    traktSearchResult: TraktShowSearchResult,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val posterPath = traktSearchResult.showSearchResult?.traktImages?.poster?.firstOrNull()

    Column(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        if (posterPath == null) Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 0.675f)
                .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.large)
                .clip(shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick, onClickLabel = traktSearchResult.showSearchResult?.title),
        ) {
            Text(
                text = stringResource(id = R.string.no_cover_image),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(data = "https://$posterPath")
//                .crossfade(durationMillis = AnimationDefaults.ANIMATION_TIME)
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
                .clickable(onClick = onClick, onClickLabel = traktSearchResult.showSearchResult?.title)
        )
        Spacer(modifier = Modifier.height(height = 6.dp))
        traktSearchResult.showSearchResult?.title?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 4.dp)) }
        Spacer(modifier = Modifier.height(height = 2.dp))
        traktSearchResult.showSearchResult?.year?.let { Text(text = "[$it]", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp)) }
    }
}
