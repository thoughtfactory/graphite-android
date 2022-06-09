package com.syncodec.graphite.bucketComponent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.miscellaneous.AddNewBucketItemButton
import com.syncodec.graphite.bucketComponent.miscellaneous.DataTypeSelectDropdownDemo
import com.syncodec.graphite.bucketComponent.miscellaneous.EmptyBucketView
import com.syncodec.graphite.bucketComponent.miscellaneous.TopBar
import com.syncodec.graphite.bucketComponent.modalBottomSheet.BookData
import com.syncodec.graphite.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.graphite.bucketComponent.modalBottomSheet.SheetLayout
import com.syncodec.graphite.bucketComponent.modalBottomSheet.ShowData
import com.syncodec.graphite.bucketComponent.screen.GridItemScreen
import com.syncodec.graphite.bucketComponent.screen.TodoScreen
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity
import com.syncodec.graphite.custom.DeleteDialog
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.bucketItem.BucketItemState
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.ui.theme.GraphiteBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState


class BucketActivity : ComponentActivity() {
	val viewModel by viewModels<BucketViewModel>()

	@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_KEY.name).also {
			if (it == null) {
				finish()
			} else {
				viewModel.bucketKey = it
				viewModel.getBucket()
			}
		}

		setContent {
			GraphiteBase {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
				viewModel.activityState = rememberBucketActivityState()

				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	override fun onBackPressed() {
		when {
			viewModel.activityState.bottomSheetState.isVisible -> viewModel.activityState.scope.launch { viewModel.activityState.bottomSheetState.hide() }
			viewModel.activityState.isSelected.value -> {
				viewModel.activityState.selectedItemList.removeIf { true }
				viewModel.activityState.isSelected.value = false
			}
			else -> super.onBackPressed()
		}
	}

	private val startForResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
			CoroutineScope(Dispatchers.IO).launch {
				if (result.resultCode == Activity.RESULT_OK) {
					val intent = result.data
					intent?.getBooleanExtra(Konstant.Companion.Konstant.DO_DELETE.name, false)
						?.also {
							if (it) {
								delay(2000)
								intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name)
									?.also { viewModel.deleteBucketItem(listOf(it)) }
							}
						}
				}
			}
		}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	private fun onPerformAction(action: Action, data: Any? = null) {
		val scope = viewModel.activityState.scope
		val activityState = viewModel.activityState
		val bucketDbEntry by viewModel.bucketDbEntry

		when (action) {
			Action.BACK -> finish()
			Action.MENU -> {
				scope.launch {
					activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
					activityState.bottomSheetState.show()
				}
			}
			Action.CHANGE_STATE -> scope.launch {
				activityState.pagerState.animateScrollToPage(data as Int, 0f)
			}
			Action.CLICK_ITEM -> {
				val isSelected by activityState.isSelected
				val selectedItemList = activityState.selectedItemList
				data as String
				if (isSelected) {
					if (data in selectedItemList) {
						selectedItemList.remove(data)
					} else {
						selectedItemList.add(data)
					}
				} else {
					if (bucketDbEntry != null) {
						viewModel.bucketItemDbEntry.value =
							viewModel.bucketItemList.find { it.key == data }
						when (bucketDbEntry?.bucketItemType) {
							BucketItemType.TODO -> {
								scope.launch {
									activityState.bottomSheetType.value =
										BottomSheetType.AddTodoSheet
									activityState.bottomSheetState.show()
								}
							}
							BucketItemType.BOOKS -> Intent(
								this@BucketActivity,
								BucketItemActivity::class.java
							).apply {
								putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
								putExtra(
									Konstant.Companion.Konstant.BUCKET_KEY.name,
									viewModel.bucketKey
								)
								putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, data)
								putExtra(
									Konstant.Companion.Konstant.BUCKET_TYPE.name,
									bucketDbEntry!!.bucketItemType.ordinal
								)

								startForResult.launch(this)
							}
							BucketItemType.SHOWS -> Intent(
								this@BucketActivity,
								BucketItemActivity::class.java
							).apply {
								putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
								putExtra(
									Konstant.Companion.Konstant.BUCKET_KEY.name,
									viewModel.bucketKey
								)
								putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, data)
								putExtra(
									Konstant.Companion.Konstant.BUCKET_TYPE.name,
									bucketDbEntry!!.bucketItemType.ordinal
								)

								startForResult.launch(this)
							}
						}
					}
				}
			}
			Action.LONG_CLICK_ITEM -> {
				val selectedBucketItemList = activityState.selectedItemList
				data as String
				activityState.isSelected.value = true
				if (data in selectedBucketItemList) {
					selectedBucketItemList.remove(data)
				} else {
					selectedBucketItemList.add(data)
				}
			}
			Action.OPEN_ADD_SHEET -> {
				activityState.bottomSheetType.value =
					when (bucketDbEntry?.bucketItemType) {
						BucketItemType.TODO -> BottomSheetType.AddTodoSheet
						BucketItemType.BOOKS -> BottomSheetType.AddBookSheet
						BucketItemType.SHOWS -> BottomSheetType.AddMovieSheet
						else -> BottomSheetType.MenuBottomSheet
					}

				viewModel.bucketItemDbEntry.value = null

				activityState.scope.launch {
					activityState.bottomSheetState.show()
				}
			}
			Action.ADD_BOOK -> {
				activityState.scope.launch { viewModel.activityState.bottomSheetState.hide() }
				Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.IS_NEW.name, true)
					putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
					putExtra(
						Konstant.Companion.Konstant.BUCKET_TYPE.name,
						BucketItemType.BOOKS.ordinal
					)
					putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, data as BookData)
					startActivity(this)
				}
			}
			Action.ADD_SHOW -> {
				activityState.scope.launch { activityState.bottomSheetState.hide() }
				Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.IS_NEW.name, true)
					putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
					putExtra(
						Konstant.Companion.Konstant.BUCKET_TYPE.name,
						BucketItemType.SHOWS.ordinal
					)
					putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, data as ShowData)
					startActivity(this)
				}
			}
			Action.ADD_TODO -> {
				data as Triple<*, *, *>
				viewModel.addTodo(
					key = data.first as String?,
					title = data.second as String,
					state = data.third as Int
				)
				scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.DATA_TYPE_SELECT -> {
				activityState.isSelectionCardVisible.value = true
			}
			Action.DATA_TYPE -> {
				data as DataType?
				activityState.dataType.value = when (data) {
					DataType.TV -> DataType.TV
					DataType.MOVIE -> DataType.MOVIE
					else -> viewModel.activityState.dataType.value
				}
				activityState.isSelectionCardVisible.value = false
			}
			Action.DELETE_ITEM -> {
				if (activityState.selectedItemList.isEmpty()) {
					Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
				} else {
					activityState.showDeleteDialog.value = true
				}
			}
			Action.ON_DELETE_ITEM -> {
				val selectedItemList = activityState.selectedItemList

				viewModel.deleteBucketItem(selectedItemList.toList())
				val selectedItemSize: Int = selectedItemList.size

				selectedItemList.clear()
				activityState.isSelected.value = false
				activityState.showDeleteDialog.value = false

				Toast.makeText(
					this@BucketActivity,
					"${if (selectedItemSize == 1) "1 entry" else "$selectedItemSize entries"} deleted",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.ON_DISMISS_DELETE -> activityState.showDeleteDialog.value = false
			Action.FAVOURITE -> if (bucketDbEntry != null) {
				bucketDbEntry!!.isFavourite = !bucketDbEntry!!.isFavourite
				viewModel.updateItem()
			}
			Action.ARCHIVE -> if (bucketDbEntry != null) {
				bucketDbEntry!!.isArchived = !bucketDbEntry!!.isArchived
				viewModel.updateItem()
			}
			Action.LOCK -> if (bucketDbEntry != null) {
				bucketDbEntry!!.isLocked = !bucketDbEntry!!.isLocked
				viewModel.updateItem()
			}
			Action.UPDATE_BUCKET -> {
				viewModel.updateBucket(bucketTitle = data as String)
			}
			Action.DELETE -> null
			Action.EXPORT -> null
			Action.SHARE -> null
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class,
		androidx.compose.animation.ExperimentalAnimationApi::class,
		ExperimentalPagerApi::class
	)
	@Composable
	private fun Screen() {
		val configuration = LocalConfiguration.current
		val screenHeight = configuration.screenHeightDp.dp

		val status by viewModel.status
		val activityState = viewModel.activityState
		val bucketDbEntry by viewModel.bucketDbEntry
		val bucketItemDbEntry by viewModel.bucketItemDbEntry
		val bucketItemList = viewModel.bucketItemList
		val isSelected by activityState.isSelected
		val bottomSheetType by activityState.bottomSheetType
		val dataType by activityState.dataType

		var alphaCount by remember { mutableStateOf(0) }
		var betaCount by remember { mutableStateOf(0) }
		var gammaCount by remember { mutableStateOf(0) }
		var totalCount by remember { mutableStateOf(0) }

		SideEffect {
			alphaCount = bucketItemList.filter { it.state == BucketItemState.ALPHA }.size
			betaCount = bucketItemList.filter { it.state == BucketItemState.BETA }.size
			gammaCount = bucketItemList.filter { it.state == BucketItemState.GAMMA }.size
			totalCount = alphaCount + betaCount + gammaCount
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout(
					bucketDbEntry = bucketDbEntry,
					bucketItemDbEntry = bucketItemDbEntry,
					bottomSheetType = bottomSheetType,
					dataType = dataType,
					alphaCount = alphaCount,
					betaCount = betaCount,
					gammaCount = gammaCount,
					totalCount = totalCount,
				) { action, data -> onPerformAction(action, data) }
			}
		) {
			if (bucketItemList.isEmpty()) {
				EmptyBucketView(
					bucketTitle = bucketDbEntry?.title ?: "",
					bucketItemType = bucketDbEntry!!.bucketItemType
				) { action, data -> onPerformAction(action, data) }
			} else {
				CollapsingToolbarScaffold(
					modifier = Modifier.background(MaterialTheme.colorScheme.background),
					state = viewModel.activityState.collapsingToolbarScaffoldState,
					scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
					toolbar = {
						TopBar(
							title = bucketDbEntry?.title ?: "",
							bucketItemType = bucketDbEntry!!.bucketItemType,
							showStateSelector = !isSelected,
							currentState = activityState.pagerState.currentPage
						) { action, data -> onPerformAction(action, data) }
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.parallax(0.2f)
						) {
							Spacer(modifier = Modifier.height(128.dp))
							Image(
								painter = painterResource(
									id = when (bucketDbEntry?.bucketItemType) {
										BucketItemType.TODO -> R.drawable.il_todo_header
										BucketItemType.BOOKS -> R.drawable.il_book_header
										BucketItemType.SHOWS -> R.drawable.il_show_header
										else -> R.drawable.il_reading
									}
								),
								contentDescription = null,
								contentScale = ContentScale.Fit,
								modifier = Modifier
									.fillMaxWidth()
									.height(screenHeight / 3)
									.graphicsLayer {
										this.alpha =
											viewModel.activityState.collapsingToolbarScaffoldState.toolbarState.progress
									}
							)
						}
					}
				) {
					AnimatedContent(
						targetState = status,
						modifier = Modifier
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.background)
					) {
						when (it) {
							Status.INIT -> LoadingView()
							Status.LOADING -> LoadingView()
							Status.LOADED -> {
								when (bucketDbEntry?.bucketItemType) {
									BucketItemType.TODO -> TodoScreen(
										bucketItemList = bucketItemList,
										selectedBucketItemList = activityState.selectedItemList,
										pagerState = activityState.pagerState,
									) { action, key -> onPerformAction(action, key) }
									BucketItemType.BOOKS -> GridItemScreen(
										bucketItemList = bucketItemList,
										selectedBucketItemList = activityState.selectedItemList,
										pagerState = activityState.pagerState,
									) { action, key -> onPerformAction(action, key) }
									BucketItemType.SHOWS -> GridItemScreen(
										bucketItemList = bucketItemList,
										selectedBucketItemList = activityState.selectedItemList,
										pagerState = activityState.pagerState,
									) { action, key -> onPerformAction(action, key) }
									else -> LoadingView()
								}
							}
							Status.ERROR -> {
							}
						}
					}
				}

				DeleteDialog(
					showDeleteDialog = viewModel.activityState.showDeleteDialog.value,
					selectedItemSize = viewModel.activityState.selectedItemList.size,
					onDismiss = { onPerformAction(Action.ON_DISMISS_DELETE) },
					onDelete = { onPerformAction(Action.ON_DELETE_ITEM) },
				)
			}

			DataTypeSelectDropdownDemo(isVisible = viewModel.activityState.isSelectionCardVisible.value) {
				onPerformAction(
					Action.DATA_TYPE,
					it
				)
			}

			AddNewBucketItemButton(
				bucketItemType = bucketDbEntry!!.bucketItemType,
				isSelected = isSelected,
				selectedBucketItemList = viewModel.activityState.selectedItemList
			) { onPerformAction(it) }
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class ActivityState @OptIn(ExperimentalPagerApi::class) constructor(
		val scope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
		val pagerState: PagerState
	) {
		var isSelectionCardVisible: MutableState<Boolean> = mutableStateOf(false)
		var dataType: MutableState<DataType> = mutableStateOf(DataType.MOVIE)
		var isSelected: MutableState<Boolean> = mutableStateOf(false)
		val selectedItemList: SnapshotStateList<String> = mutableStateListOf()
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)
		var bottomSheetType: MutableState<BottomSheetType> =
			mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	@Composable
	fun rememberBucketActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
		pagerState: PagerState = rememberPagerState()
	) = remember {
		ActivityState(
			coroutineScope,
			bottomSheetState,
			collapsingToolbarScaffoldState,
			pagerState
		)
	}

	enum class DataType {
		TV,
		MOVIE,
	}

	enum class Action {
		BACK,
		MENU,
		CHANGE_STATE,
		CLICK_ITEM,
		LONG_CLICK_ITEM,
		OPEN_ADD_SHEET,
		ADD_BOOK,
		ADD_SHOW,
		ADD_TODO,
		DATA_TYPE_SELECT,
		DATA_TYPE,
		SHOW_DELETE,
		DELETE_ITEM,
		ON_DELETE_ITEM,
		ON_DISMISS_DELETE,
		FAVOURITE,
		ARCHIVE,
		LOCK,
		UPDATE_BUCKET,
		DELETE,
		EXPORT,
		SHARE,
	}
}
