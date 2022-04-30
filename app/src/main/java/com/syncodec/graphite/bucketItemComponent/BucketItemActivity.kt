package com.syncodec.graphite.bucketItemComponent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.graphite.bucketItemComponent.miscellaneous.TopBar
import com.syncodec.graphite.bucketItemComponent.modalBottonSheet.MenuBottomSheet
import com.syncodec.graphite.bucketItemComponent.screen.BookItemScreen
import com.syncodec.graphite.bucketItemComponent.screen.ShowMovieItemScreen
import com.syncodec.graphite.bucketItemComponent.screen.ShowTvItemScreen
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.bucketItem.BucketItemState
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.ui.theme.GraphiteTheme
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

		intent.hasExtra(Konstant.Companion.Konstant.IS_NEW.name).also {
			if (it) {
				viewModel.isNew =
					intent.getBooleanExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
				viewModel.bucketKey =
					intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_KEY.name)!!
				viewModel.bucketItemKey.value =
					intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name)
				intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, -1).also {
					if (it == -1) {
						finish()
					} else {
						viewModel.bucketItemType = BucketItemType.values()[it]
					}
				}

			} else {
				finish()
			}
		}

		viewModel.getItem(intent = intent)

		setContent {
			GraphiteTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				viewModel.activityState = rememberBucketActivityState()

				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	private fun onPerformAction(action: Action, data: Any? = null) {
		val bucketItemDbEntry by viewModel.bucketItemDbEntry

		when (action) {
			Action.TOP_BAR_PRIMARY -> {
				if (viewModel.bucketItemKey.value == null) {
					viewModel.putItem()
				} else {
					finish()
				}
			}
			Action.MENU -> viewModel.activityState.coroutineScope.launch { viewModel.activityState.bottomSheetState.show() }
			Action.STATE -> {
//				WARN    Cant update state before saving and reloading
				data as Int
				viewModel.bucketItemDbEntry.value?.state = BucketItemState.values()[data]
				viewModel.updateItem()
			}
			Action.ADD_THOUGHT -> { viewModel.updateItem() }
			Action.OPEN_LINK -> {
				Intent(Intent.ACTION_VIEW, Uri.parse(data as String)).apply {
					startActivity(this)
				}
			}
			Action.FAVOURITE -> {
//				bucketItemDbEntry!!.isFavourite = !bucketItemDbEntry!!.isFavourite
				viewModel.updateItem()
			}
			Action.ARCHIVE -> {
//				bucketItemDbEntry!!.isArchived = !bucketItemDbEntry!!.isArchived
				viewModel.updateItem()
			}
			Action.LOCK -> {
//				bucketItemDbEntry!!.isLocked = !bucketItemDbEntry!!.isLocked
				viewModel.updateItem()
			}
			Action.DELETE -> {
				if (bucketItemDbEntry != null) {
					Intent().apply {
						putExtra(Konstant.Companion.Konstant.DO_DELETE.name, true)
						putExtra(
							Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name,
							bucketItemDbEntry!!.key
						)
						setResult(Activity.RESULT_OK, this)
						finish()
					}
				}
			}
			Action.EXPORT -> null
			Action.SHARE -> null
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class,
		ExperimentalMaterial3Api::class,
		androidx.compose.animation.ExperimentalAnimationApi::class
	)
	@Composable
	private fun Screen() {
		val status by viewModel.status

		Crossfade(targetState = status) {
			when (it) {
				Status.INIT -> LoadingView()
				Status.LOADING -> LoadingView()
				Status.LOADED -> Content()
				else -> null
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Composable
	private fun Content() {
		val bucketItemDbEntry by viewModel.bucketItemDbEntryFlow.collectAsState()
		val bookData by viewModel.bookData
		val tvData by viewModel.tvData
		val movieData by viewModel.movieData
		val thoughtList = viewModel.thoughtList

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				MenuBottomSheet(
					createdTimestamp = bucketItemDbEntry?.createdTimestamp ?: -1,
					modifiedTimestamp = bucketItemDbEntry?.modifiedTimestamp ?: -1,
				)
			},
		) {
			Scaffold(
				containerColor = MaterialTheme.colorScheme.background,
				topBar = {
					TopBar(
						isNew = viewModel.bucketItemKey.value == null,
					) { action, data -> onPerformAction(action, data) }
				}
			) {
				when (viewModel.bucketItemType) {
					BucketItemType.TODO -> null
					BucketItemType.BOOKS -> Crossfade(targetState = bookData != null) {
						if (it) {
							BookItemScreen(
								bookData = bookData!!,
								thumbnail = viewModel.thumbnail.value,
								thoughtList = thoughtList,
								currentState = bucketItemDbEntry!!.state.ordinal,
							) {action, data -> onPerformAction(action, data) }
						} else {
							LoadingView()
						}
					}
					BucketItemType.SHOWS -> {
						Crossfade(targetState = tvData != null || movieData != null) {
							if (it) {
								when (viewModel.showData.value!!.showType) {
									ShowType.TV -> ShowTvItemScreen(
										tvData = tvData!!,
										thumbnail = viewModel.thumbnail.value,
										thoughtList = thoughtList,
										currentState = bucketItemDbEntry!!.state.ordinal,
									) { action, data -> onPerformAction(action, data) }
									ShowType.MOVIE -> ShowMovieItemScreen(
										movieData = movieData!!,
										thumbnail = viewModel.thumbnail.value,
										currentState = bucketItemDbEntry!!.state.ordinal,
										thoughtList = thoughtList,
									) { action, data -> onPerformAction(action, data) }
								}
							} else {
								LoadingView()
							}
						}
					}
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

	enum class Action {
		TOP_BAR_PRIMARY,
		MENU,
		STATE,
		ADD_THOUGHT,
		OPEN_LINK,
		FAVOURITE,
		ARCHIVE,
		LOCK,
		DELETE,
		EXPORT,
		SHARE
	}
}
