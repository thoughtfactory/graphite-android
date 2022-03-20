package com.syncodec.momento.bucketItemComponent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.bucketItemComponent.miscellaneous.TopBar
import com.syncodec.momento.bucketItemComponent.modalBottonSheet.MenuBottomSheet
import com.syncodec.momento.bucketItemComponent.screen.ShowMovieItemScreen
import com.syncodec.momento.bucketItemComponent.screen.ShowTvItemScreen
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.database.bucket.BucketItemState
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

		intent.hasExtra(Konstant.Companion.Konstant.IS_NEW.name).also {
			if (it) {
				viewModel.isNew = intent.getBooleanExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
				viewModel.bucketKey = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_KEY.name)!!
				viewModel.bucketItemKey.value = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name)
				intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, -1).also {
					if (it == -1) {
						finish()
					} else {
						viewModel.bucketItemType = BucketItemType.Type.values()[it]
					}
				}

			} else {
				finish()
			}
		}

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
	private fun onClick(click: Click, data: Any? = null) {
		val bucketItemDbEntry by viewModel.bucketItemDbEntry

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
			Click.FAVOURITE -> {
				bucketItemDbEntry!!.isFavourite = !bucketItemDbEntry!!.isFavourite
				viewModel.updateItem()
			}
			Click.ARCHIVE -> {
				bucketItemDbEntry!!.isArchived = !bucketItemDbEntry!!.isArchived
				viewModel.updateItem()
			}
			Click.LOCK -> {
				bucketItemDbEntry!!.isLocked = !bucketItemDbEntry!!.isLocked
				viewModel.updateItem()
			}
			Click.DELETE -> {
				if (bucketItemDbEntry != null) {
					Intent().apply {
						putExtra(Konstant.Companion.Konstant.DO_DELETE.name, true)
						putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, bucketItemDbEntry!!.key)
						setResult(Activity.RESULT_OK, this)
						finish()
					}
				}
			}
			Click.EXPORT -> viewModel.updateThought()
			Click.SHARE -> viewModel.updateThought()
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
	@Composable
	private fun Screen() {
		val status by viewModel.status

		Crossfade(targetState = status) {
			when (it) {
				Status.INIT -> LoadingView()
				Status.LOADING -> LoadingView()
				Status.LOADED -> Content()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Composable
	private fun Content() {
		val bucketItemDbEntry by viewModel.bucketItemDbEntry
		val tvData by viewModel.tvData
		val movieData by viewModel.movieData
		val thoughtList = viewModel.thoughtList

		val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData?> = if (bucketItemDbEntry == null) listOf()
		else listOf(
			MenuBottomSheetButtonData(
				title = "Favourite",
				resourceId = if (bucketItemDbEntry!!.isFavourite) R.drawable.ic_heart_filled else R.drawable.ic_heart,
				highlight = bucketItemDbEntry!!.isFavourite
			) { onClick(Click.FAVOURITE) },
			MenuBottomSheetButtonData(
				title = "Archive",
				resourceId = R.drawable.ic_box,
				highlight = bucketItemDbEntry?.isArchived == true
			) { onClick(Click.ARCHIVE) },
			MenuBottomSheetButtonData(
				title = "Lock",
				resourceId = R.drawable.ic_locked,
				highlight = bucketItemDbEntry?.isLocked == true
			) { onClick(Click.LOCK) },
			MenuBottomSheetButtonData(title = "Delete", resourceId = R.drawable.ic_trash, highlight = false) { onClick(Click.DELETE) },

			MenuBottomSheetButtonData(title = "Export", resourceId = R.drawable.ic_export, highlight = false) { onClick(Click.EXPORT) },
			MenuBottomSheetButtonData(title = "Share", resourceId = R.drawable.ic_share, highlight = false) { onClick(Click.SHARE) },
			null,
			null
		)


		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				MenuBottomSheet(
					createdTimestamp = bucketItemDbEntry?.createdTimestamp ?: -1,
					modifiedTimestamp = bucketItemDbEntry?.modifiedTimestamp ?: -1,
					menuBottomSheetButtonDataList = menuBottomSheetButtonDataList
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
		ADD_THOUGHT,
		FAVOURITE,
		ARCHIVE,
		LOCK,
		DELETE,
		EXPORT,
		SHARE
	}
}
