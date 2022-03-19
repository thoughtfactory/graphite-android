package com.syncodec.momento.bucketComponent

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.miscellaneous.AddNewBucketItemButton
import com.syncodec.momento.bucketComponent.miscellaneous.DataTypeSelectDropdownDemo
import com.syncodec.momento.bucketComponent.miscellaneous.EmptyBucketView
import com.syncodec.momento.bucketComponent.miscellaneous.TopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.*
import com.syncodec.momento.bucketComponent.screen.GridItemScreen
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import com.syncodec.momento.custom.DeleteDialog
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState


class BucketActivity : ComponentActivity() {

	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	val viewModel by viewModels<BucketViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.bucketKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		viewModel.getBucket()
		intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, -1).also {
			if (it != -1) {
				viewModel.bucketItemType = BucketItemType.Type.values()[it]
			} else {
				finish()
			}
		}

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
				viewModel.activityState = rememberBucketActivityState()
				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	override fun onBackPressed() {
		when {
			viewModel.activityState.bottomSheetState.isVisible -> CoroutineScope(Dispatchers.IO).launch { viewModel.activityState.bottomSheetState.hide() }
			viewModel.activityState.isSelected.value -> {
				viewModel.activityState.selectedItemList.removeIf { true }
				viewModel.activityState.isSelected.value = false
			}
			else -> super.onBackPressed()
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	private fun onClick(click: Click, data: Any? = null) {
		val scope = viewModel.activityState.coroutineScope
		when (click) {
			Click.BACK -> finish()
			Click.SEARCH -> {}
			Click.MENU -> {}
			Click.STATE_ALPHA -> scope.launch { viewModel.activityState.pagerState.animateScrollToPage(0, 0f) }
			Click.STATE_BETA -> scope.launch { viewModel.activityState.pagerState.animateScrollToPage(1, 0f) }
			Click.STATE_GAMMA -> scope.launch { viewModel.activityState.pagerState.animateScrollToPage(2, 0f) }
			Click.STATE_DELTA -> scope.launch { viewModel.activityState.pagerState.animateScrollToPage(3, 0f) }
			Click.CLICK_ITEM -> {
				val isSelected by viewModel.activityState.isSelected
				val selectedItemList = viewModel.activityState.selectedItemList
				data as String
				if (isSelected) {
					if (data in selectedItemList) {
						selectedItemList.remove(data)
					} else {
						selectedItemList.add(data)
					}
				} else {
					Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
						putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
						putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, data)
						putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, viewModel.bucketItemType.ordinal)

						startActivity(this)
					}
				}
			}
			Click.LONG_CLICK_ITEM -> {
				val selectedBucketItemList = viewModel.activityState.selectedItemList
				data as String
				viewModel.activityState.isSelected.value = true
				if (data in selectedBucketItemList) {
					selectedBucketItemList.remove(data)
				} else {
					selectedBucketItemList.add(data)
				}
			}
			Click.OPEN_ADD_SHEET -> {
				viewModel.activityState.bottomSheetType.value = when (viewModel.bucketItemType) {
					BucketItemType.Type.TODO -> BottomSheetType.AddBookSheet
					BucketItemType.Type.BOOKS -> BottomSheetType.AddBookSheet
					BucketItemType.Type.SHOWS -> BottomSheetType.AddMovieSheet
					BucketItemType.Type.MEDIA -> BottomSheetType.AddBookSheet
					BucketItemType.Type.LINKS -> BottomSheetType.AddBookSheet
				}

				viewModel.activityState.coroutineScope.launch {
					viewModel.activityState.bottomSheetState.show()
				}
			}
			Click.ADD_BOOK -> {
				viewModel.activityState.coroutineScope.launch { viewModel.activityState.bottomSheetState.hide() }
				Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.BOOKS.ordinal)
					putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, objectMapper.writeValueAsString(data as BookData))
					startActivity(this)
				}
			}
			Click.ADD_SHOW -> {
				viewModel.activityState.coroutineScope.launch { viewModel.activityState.bottomSheetState.hide() }
				Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.SHOWS.ordinal)
					putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, objectMapper.writeValueAsString(data as ShowData))
					startActivity(this)
				}
			}
			Click.DATA_TYPE_SELECT -> {
				viewModel.activityState.isSelectionCardVisible.value = true
			}
			Click.DATA_TYPE -> {
				data as DataType?
				viewModel.activityState.dataType.value = when (data) {
					DataType.TV -> DataType.TV
					DataType.MOVIE -> DataType.MOVIE
					else -> viewModel.activityState.dataType.value
				}
				viewModel.activityState.isSelectionCardVisible.value = false
			}
			Click.DELETE_ITEM -> {
				if (viewModel.activityState.selectedItemList.isEmpty()) {
					Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
				} else {
					viewModel.activityState.showDeleteDialog.value = true
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
	@Composable
	private fun Screen() {
		val status by viewModel.status
		val bucketItemList = viewModel.bucketItemList
		val isSelected by viewModel.activityState.isSelected

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetContent = {
				when (viewModel.activityState.bottomSheetType.value) {
					BottomSheetType.AddBookSheet -> AddBookSheet { onClick(Click.ADD_BOOK, it) }
					BottomSheetType.AddMovieSheet -> AddShowSheet(dataType = viewModel.activityState.dataType.value) { click, data -> onClick(click, data) }
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
						if (bucketItemList.isNullOrEmpty()) {
							EmptyBucketView(
								bucketTitle = viewModel.bucketDbEntry.title,
								bucketItemType = viewModel.bucketItemType
							) { onClick(it) }
						} else {
							CollapsingToolbarScaffold(
								modifier = Modifier,
								state = viewModel.activityState.collapsingToolbarScaffoldState,
								scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
								toolbar = {
									TopBar(
										bucketTitle = viewModel.bucketDbEntry.title,
										bucketItemType = viewModel.bucketItemType,
										showStateSelector = !isSelected,
									) { onClick(it) }
									Column(
										modifier = Modifier
											.fillMaxWidth()
											.parallax(0.2f)
									) {
										Spacer(modifier = Modifier.height(128.dp))
										Image(
											painter = painterResource(id = R.drawable.il_reading),
											contentDescription = null,
											contentScale = ContentScale.Fit,
											modifier = Modifier
												.fillMaxWidth()
												.height(192.dp)
												.graphicsLayer {
													this.alpha = viewModel.activityState.collapsingToolbarScaffoldState.toolbarState.progress
												}
										)
									}
								}
							) {
								GridItemScreen(
									bucketItemList = bucketItemList!!,
									selectedBucketItemList = viewModel.activityState.selectedItemList,
									pagerState = viewModel.activityState.pagerState,
									getThumbnailPath = { viewModel.getThumbnail(bucketItemKey = it) }
								) { click, key ->
									onClick(click, key)
								}
							}

							DeleteDialog(
								showDeleteDialog = viewModel.activityState.showDeleteDialog.value,
								selectedItemSize = viewModel.activityState.selectedItemList.size,
								onDismiss = { viewModel.activityState.showDeleteDialog.value = false },
								onDelete = {
									val selectedItemList = viewModel.activityState.selectedItemList

									viewModel.deleteBucketItem(selectedItemList.toList())
									val selectedItemSize: Int = selectedItemList.size

									selectedItemList.removeAll { true }
									viewModel.activityState.isSelected.value = false
									viewModel.activityState.showDeleteDialog.value = false

									Toast.makeText(
										this@BucketActivity,
										"${if (selectedItemSize == 1) "1 entry" else "$selectedItemSize entries"} deleted",
										Toast.LENGTH_SHORT
									).show()
								},
							)
						}
					}
					Status.SAVING -> null
					Status.SAVED -> null
					Status.SUCCESS -> null
					Status.ERROR -> null
				}
			}
			DataTypeSelectDropdownDemo(isVisible = viewModel.activityState.isSelectionCardVisible.value) { onClick(Click.DATA_TYPE, it) }

			AddNewBucketItemButton(
				bucketItemType = viewModel.bucketItemType,
				isSelected = isSelected,
				selectedBucketItemList = viewModel.activityState.selectedItemList
			) { onClick(if (isSelected) Click.DELETE_ITEM else Click.OPEN_ADD_SHEET) }
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class ActivityState @OptIn(ExperimentalPagerApi::class) constructor(
		val coroutineScope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
		val pagerState: PagerState
	) {
		var isSelectionCardVisible: MutableState<Boolean> = mutableStateOf(false)
		var dataType: MutableState<DataType> = mutableStateOf(DataType.MOVIE)
		var isSelected: MutableState<Boolean> = mutableStateOf(false)
		val selectedItemList: SnapshotStateList<String> = mutableStateListOf()
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	@Composable
	fun rememberBucketActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
		pagerState: PagerState = rememberPagerState()
	) = remember { ActivityState(coroutineScope, bottomSheetState, collapsingToolbarScaffoldState, pagerState) }

	enum class DataType {
		TV,
		MOVIE,
	}

	enum class Click {
		BACK,
		SEARCH,
		MENU,
		STATE_ALPHA,
		STATE_BETA,
		STATE_GAMMA,
		STATE_DELTA,
		CLICK_ITEM,
		LONG_CLICK_ITEM,
		OPEN_ADD_SHEET,
		ADD_BOOK,
		ADD_SHOW,
		DATA_TYPE_SELECT,
		DATA_TYPE,
		DELETE_ITEM
	}
}
