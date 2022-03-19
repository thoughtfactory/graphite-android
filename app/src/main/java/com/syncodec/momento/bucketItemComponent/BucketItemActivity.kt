package com.syncodec.momento.bucketItemComponent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.bucketItemComponent.miscellaneous.TopBar
import com.syncodec.momento.bucketItemComponent.modalBottonSheet.MenuBottomSheet
import com.syncodec.momento.bucketItemComponent.screen.ShowMovieItemScreen
import com.syncodec.momento.bucketItemComponent.screen.ShowTvItemScreen
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.bucket.BucketItemState
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BucketItemActivity : ComponentActivity() {

	val viewModel by viewModels<BucketItemViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, -1).also {
			if (it == -1) {
				finish()
			} else {
				viewModel.bucketItemType = BucketItemType.Type.values()[it]
			}
		}

		viewModel.bucketKey = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_KEY.name)!!
		viewModel.bucketItemKey.value = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name)

		viewModel.getItem(intent = intent)

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)
				viewModel.activityState = rememberBucketActivityState()

				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	private fun onClick(click: Click, data: Any?) {
		when (click) {
			Click.TOP_BAR_PRIMARY -> {
				if (viewModel.bucketItemKey.value == null) {
					viewModel.putItem()
				} else {
					finish()
				}
			}
			Click.TOP_BAR_SECONDARY -> viewModel.activityState.coroutineScope.launch { viewModel.activityState.bottomSheetState.show() }
			Click.STATE -> {
				data as Int
				viewModel.bucketItemDbEntry.value?.state = BucketItemState.values()[data]
				viewModel.updateItem()
			}
			Click.ADD_THOUGHT -> viewModel.updateThought()
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
	@Composable
	private fun Screen() {
		val bucketItemDbEntry by viewModel.bucketItemDbEntry

		if (viewModel.bucketItemKey.value == null) Content()
		else Crossfade(targetState = bucketItemDbEntry) { if (it == null) LoadingView() else Content() }
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Composable
	private fun Content() {
		val bucketItemDbEntry by viewModel.bucketItemDbEntry
		val tvData by viewModel.tvData
		val movieData by viewModel.movieData
		val thoughtList = viewModel.thoughtList

		Log.i("npr71", "tvData : $tvData")
		Log.i("npr71", "movieData : $movieData")

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				MenuBottomSheet(
					createdTimestamp = bucketItemDbEntry!!.createdTimestamp,
					modifiedTimestamp = bucketItemDbEntry!!.modifiedTimestamp
				)
			},
		) {
			Scaffold(
				topBar = { TopBar(key = viewModel.bucketItemKey.value) { onClick(it, null) } }
			) {
				when (viewModel.bucketItemType) {
					BucketItemType.Type.TODO -> null
					BucketItemType.Type.BOOKS -> null
					BucketItemType.Type.SHOWS -> {
						Crossfade(targetState = tvData == null && movieData == null) {
							if (it) {
								LoadingView()
							} else {
								when (viewModel.showData.value!!.showType) {
									ShowType.TV -> ShowTvItemScreen(
										tvData = tvData!!,
										thumbnail = viewModel.thumbnail.value,
										thoughtList = thoughtList,
										currentState = bucketItemDbEntry!!.state.ordinal,
									) { click, i -> onClick(click, i) }
									ShowType.MOVIE -> ShowMovieItemScreen(
										movieData = movieData!!,
										thumbnail = viewModel.thumbnail.value,
										thoughtList = thoughtList,
										currentState = bucketItemDbEntry!!.state.ordinal,
									) { click, i -> onClick(click, i) }
								}
							}
						}
					}
					BucketItemType.Type.MEDIA -> null
					BucketItemType.Type.LINKS -> null
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class ActivityState(
		val coroutineScope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false),
	) {
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberBucketActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(coroutineScope, bottomSheetState)
	}

	enum class Click {
		TOP_BAR_PRIMARY,
		TOP_BAR_SECONDARY,
		STATE,
		ADD_THOUGHT
	}
}
