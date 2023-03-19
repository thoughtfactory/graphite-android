package com.syncodec.graphite.presentation.bucket.composable.screen.showScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
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
fun BucketShowScreen(
	pagerState : PagerState = rememberPagerState(),
	isSelecting : Boolean = false,
	onSelect : (RealmUUID) -> Unit = {},
	selectedIdList : List<RealmUUID> = listOf(),
) {
	val context = LocalContext.current
	val viewModel : BucketScreenCommonViewModel = koinViewModel()

	val isAuthenticated = LocalIsAuthenticated.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.Timestamp)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.Descending)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.List)

	val bucketId by viewModel.id.collectAsState()
	val bucketItemList by viewModel.bucketItemList.collectAsState()

	HorizontalPager(
		pageCount = 4,
		state = pagerState,
		userScrollEnabled = ! isSelecting,
	) { pageIndex ->
		val filteredBucketItemList = when (pageIndex) {
			0 -> bucketItemList
			1 -> bucketItemList.filter { it.state == BucketItemState.ALPHA.name }
			2 -> bucketItemList.filter { it.state == BucketItemState.BETA.name }
			3 -> bucketItemList.filter { it.state == BucketItemState.GAMMA.name }
			else -> bucketItemList
		}.filter { !it.isLocked || isAuthenticated }
		BucketShowGridScreen(
			bucketId = bucketId,
			bucketItemList = filteredBucketItemList,
			isSelecting = isSelecting,
			onSelect = onSelect,
			selectedIdList = selectedIdList,
		)
	}
}
