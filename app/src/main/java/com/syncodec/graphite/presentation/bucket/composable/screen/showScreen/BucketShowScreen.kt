package com.syncodec.graphite.presentation.bucket.composable.screen.showScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import org.koin.androidx.compose.koinViewModel


@Preview
@OptIn(ExperimentalPagerApi::class)
@Composable
fun BucketShowScreen(pagerState : PagerState = rememberPagerState()) {
	val context = LocalContext.current
	val viewModel : BucketScreenCommonViewModel = koinViewModel()
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isSelected = LocalCompositionIsSelected.current

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.Timestamp)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.Descending)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.LIST)

	val bucketId by viewModel.id
	val bucketItemList by viewModel.bucketItemList

	HorizontalPager(
		count = 4,
		state = pagerState,
		userScrollEnabled = ! isSelected,
	) {
		val filteredBucketItemList = when (it) {
			0 -> bucketItemList
			1 -> bucketItemList.filter { it.state == BucketItemState.ALPHA.name }
			2 -> bucketItemList.filter { it.state == BucketItemState.BETA.name }
			3 -> bucketItemList.filter { it.state == BucketItemState.GAMMA.name }
			else -> bucketItemList
		}
		BucketShowGridScreen(
			bucketId = bucketId,
			bucketItemList = filteredBucketItemList
		)
	}
}
