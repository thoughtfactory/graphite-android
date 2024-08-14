package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketTodoScreen(
    pagerState: PagerState = rememberPagerState(initialPage = 0) { 1 },
    isSelecting: Boolean = false,
    selectedIdList: List<RealmUUID> = listOf(),
    onSelect: (RealmUUID) -> Unit = {},
    onClickBucketItem: (RealmUUID) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: BucketScreenCommonViewModel = koinViewModel()

    val isAuthenticated = LocalIsAuthenticated.current
    val onAuthenticationAction = LocalAuthenticatorAction.current

    val dataStoreInstance = remember { DataStoreInstance(context = context) }

    val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.Timestamp)
    val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.Descending)
    val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.List)

    val bucketId by viewModel.id.collectAsState()
    val bucketItemList by viewModel.bucketItemList.collectAsState()
    val isLoadedFirstTime by viewModel.isLoadedFirstTime.collectAsState()

    Crossfade(
        targetState = isLoadedFirstTime,
        animationSpec = tween(300),
        modifier = Modifier.fillMaxSize(),
        label = "bucketTodoScreen_animation"
    ) { isLoaded ->
        if (isLoaded) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isSelecting,
            ) { pageIndex ->
                val filteredBucketItemList = when (pageIndex) {
                    0 -> bucketItemList
                    1 -> bucketItemList.filter { it.state == BucketItemState.ALPHA.name }
                    2 -> bucketItemList.filter { it.state == BucketItemState.BETA.name }
                    3 -> bucketItemList.filter { it.state == BucketItemState.GAMMA.name }
                    else -> bucketItemList
                }.filter { !it.isLocked || isAuthenticated }
                BucketTodoListScreen(
                    bucketItemList = filteredBucketItemList,
                    isSelecting = isSelecting,
                    selectedIdList = selectedIdList,
                    onSelect = onSelect,
                    onClickBucketItem = onClickBucketItem,
                    onClickFavourite = viewModel::toggleFavourite,
                    onClickLock = { if (isAuthenticated) viewModel.toggleLock(it) else onAuthenticationAction(AuthenticatorScreen.Authenticate) },
                    onCheckedChange = viewModel::toggleBucketItemState,
                )
            }
        } else {
            LoadingView()
        }
    }
}
