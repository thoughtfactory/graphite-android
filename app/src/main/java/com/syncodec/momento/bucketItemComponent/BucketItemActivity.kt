package com.syncodec.momento.bucketItemComponent

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import com.syncodec.momento.bucketItemComponent.screen.BookItemScreen
import com.syncodec.momento.bucketItemComponent.screen.ShowItemScreen
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Status
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

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
	@Composable
	private fun Screen() {
		val scope = rememberCoroutineScope()

		val status by viewModel.status
		val thoughtList = viewModel.thoughtList
		val tvData by viewModel.tvData

		var hash = 0
		thoughtList.forEach { hash = it.hashCode() }
		LaunchedEffect(
			key1 = hash
		) {
			if (viewModel.bucketItemKey.value != null) {
				viewModel.updateThought()
			}
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				MenuBottomSheet(
					createdTimestamp = viewModel.bucketItemDbEntry.value.createdTimestamp,
					modifiedTimestamp = viewModel.bucketItemDbEntry.value.modifiedTimestamp
				)
			},
		) {
			Scaffold(
				topBar = {
					TopBar(
						primaryKey = viewModel.bucketItemKey.value,
						onClickMenu = { scope.launch { viewModel.activityState.bottomSheetState.show() } }
					) {
						if (viewModel.bucketItemKey.value == null) {
							viewModel.putItem()
						} else {
							finish()
						}
					}
				}
			) {
				when (viewModel.bucketItemType) {
					BucketItemType.Type.TODO -> null
					BucketItemType.Type.BOOKS -> BookItemScreen(
						bookData = viewModel.bookData.value!!,
						thumbnail = viewModel.thumbnail.value,
						thoughtList = thoughtList,
						currentBookState = viewModel.bucketItemDbEntry.value.state,
					) {
						viewModel.bucketItemDbEntry.value.state = it
						viewModel.updateItem()
					}
					BucketItemType.Type.SHOWS -> {
						Crossfade(targetState = tvData) {
							if (it == null) {
								LoadingView()
							} else {
								when (viewModel.tvData.value!!.showType) {
									ShowType.TV -> ShowItemScreen(
										tvData = tvData!!,
										thumbnail = viewModel.thumbnail.value,
										thoughtList = thoughtList,
										currentMovieState = viewModel.bucketItemDbEntry.value.state,
									)
									ShowType.MOVIE -> null
								}
							}
						}
					}
					BucketItemType.Type.MEDIA -> null
					BucketItemType.Type.LINKS -> null
				}

//				AnimatedContent(targetState = status) {
//					when (it) {
//						Status.INIT -> LoadingView()
//						Status.LOADING -> LoadingView()
//						Status.LOADED -> when (viewModel.bucketItemType) {
//							BucketItemType.Type.TODO -> null
//							BucketItemType.Type.BOOKS -> BookItemScreen(
//								bookData = viewModel.bookData.value!!,
//								thumbnail = if (viewModel.bucketItemKey.value == null)
//									"https://covers.openlibrary.org/b/id/${viewModel.bookData.value!!.coverI}-M.jpg"
//								else BitmapFactory.decodeFile(viewModel.thumbnail.value),
//								thoughtList = thoughtList,
//								currentBookState = viewModel.bucketItemDbEntry.value.state,
//							) {
//								viewModel.bucketItemDbEntry.value.state = it
//								viewModel.updateItem()
//							}
//							BucketItemType.Type.SHOWS -> ShowItemScreen(
//								showData = viewModel.showData.value!!,
//								thumbnail = if (viewModel.bucketItemKey.value == null)
//									"https://image.tmdb.org/t/p/w500${viewModel.showData.value!!.posterPath}"
//								else BitmapFactory.decodeFile(viewModel.thumbnail.value),
//								thoughtList = thoughtList,
//								currentMovieState = viewModel.bucketItemDbEntry.value.state,
//							) {
//								viewModel.bucketItemDbEntry.value.state = it
//								viewModel.updateItem()
//							}
//							BucketItemType.Type.MEDIA -> null
//							BucketItemType.Type.LINKS -> null
//						}
//						else -> {}
//					}
//				}
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
}
