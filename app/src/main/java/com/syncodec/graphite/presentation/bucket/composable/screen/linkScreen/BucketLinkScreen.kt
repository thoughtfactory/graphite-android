package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import org.koin.androidx.compose.koinViewModel


@Composable
fun BucketLinkScreen() {
	val context = LocalContext.current
	val viewModel : BucketScreenCommonViewModel = koinViewModel()
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isSelected = LocalCompositionIsSelected.current

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.Timestamp)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.Descending)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.LIST)

	val bucketId by viewModel.id
	val bucketItemList by viewModel.bucketItemList
	val isLoadedFirstTime by viewModel.isLoadedFirstTime

	Crossfade(
		targetState = isLoadedFirstTime,
		animationSpec = tween(300),
		modifier = Modifier.fillMaxSize()
	) {
		if (it) {
			BucketLinkListScreen(
				bucketItemList = bucketItemList,
				onReorderBucketItemList = viewModel::onReorderBucketItem
			)
		} else {
			LoadingView()
		}
	}
}
