package com.syncodec.graphite.presentation.bucketItem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BookScreen
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.custom.ErrorView
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.screen.MovieScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen() {
	val viewModel: BucketItemViewModel = viewModel()

	val bucketType by viewModel.bucketType

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = { TopBar() },
		floatingActionButton = {
			FloatingActionButton(onClick = { viewModel.putBucketItem() }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_check),
					contentDescription = "Save"
				)
			}
		},
		floatingActionButtonPosition = FabPosition.End,
		containerColor = MaterialTheme.colorScheme.background
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			when (bucketType) {
				BucketType.TODO -> ErrorView()
				BucketType.BOOK -> BookScreen()
				BucketType.SHOW -> MovieScreen()
				BucketType.LINK -> ErrorView()
				null -> LoadingView()
			}
		}
	}
}
