package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBucketType
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnShare
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowType
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.BucketItemDialog
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen() {

	val context = LocalContext.current

	val bucketType = LocalCompositionBucketType.current
	val showType = LocalCompositionShowType.current

	val openDialog = LocalCompositionOpenDialog.current

	val onShare = LocalCompositionOnShare.current

	Scaffold(
		topBar = { TopBar() },
		bottomBar = {
			BottomBar(
				onClickShare = onShare,
				onClickDelete = { openDialog(DialogType.DELETE) },
				onClickAddReminder = {
					Toast.makeText(context, "Add reminder and due dates are under development. Stay tuned...", Toast.LENGTH_SHORT).show()
				}
			)
		},
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Crossfade(targetState = bucketType) {
				when (it) {
					BucketType.BOOK -> BookScreen()
					BucketType.SHOW -> when (showType) {
						ShowType.TV -> TvScreen()
						ShowType.MOVIE -> MovieScreen()
						null -> null
					}

					else -> ErrorView()
				}
			}
		}
	}

	BucketItemDialog()
}
