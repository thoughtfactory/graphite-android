package com.syncodec.momento.bucketComponent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.bucketComponent.screen.BooksScreen
import com.syncodec.momento.bucketComponent.miscellaneous.AddNewBucketItemButton
import com.syncodec.momento.bucketComponent.miscellaneous.EmptyBucketView
import com.syncodec.momento.bucketComponent.miscellaneous.TopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.AddBookSheet
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.CoroutineScope
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
		viewModel.bucketItemType =
			BucketItemType.Type.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.TODO.ordinal)]

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)
				viewModel.activityState = rememberBucketActivityState()
				Screen()
			}
		}
	}

	override fun onBackPressed() {
		super.onBackPressed()
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun Screen() {
		val scope = rememberCoroutineScope()

		val isSelected by viewModel.activityState.isSelected

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetContent = {
//				SheetLayout(bottomSheetType = viewModel.activityState.bottomSheetType.value)
				when (viewModel.activityState.bottomSheetType.value) {
					BottomSheetType.AddBookSheet -> AddBookSheet {
						scope.launch { viewModel.activityState.bottomSheetState.hide() }
						Intent(this@BucketActivity, BucketItemActivity::class.java).apply {
							putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, viewModel.bucketKey)
							putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.BOOKS.ordinal)
							putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, objectMapper.writeValueAsString(it))

							startActivity(this)
						}
					}
				}
			}
		) {

			val bucketItemList by viewModel.getBucketItemList().observeAsState()

			Crossfade(
				targetState = bucketItemList,
				animationSpec = tween(durationMillis = 400)
			) {
				when {
					it == null -> LoadingView()
					it.isEmpty() -> EmptyBucketView()
					else -> {
						CollapsingToolbarScaffold(
							modifier = Modifier,
							state = rememberCollapsingToolbarScaffoldState(),
							scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
							toolbar = {
								TopBar()
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.height(192.dp)
										.background(MaterialTheme.colorScheme.secondaryContainer)
								) {

								}
							}
						) {
							when (viewModel.bucketItemType) {
								BucketItemType.Type.TODO -> null
								BucketItemType.Type.BOOKS -> BooksScreen(
									bucketItemList = bucketItemList!!,
									isSelected = isSelected,
									selectedBucketItemList = viewModel.activityState.selectedBucketItemList
								) {
									return@BooksScreen viewModel.getThumbnail(bucketItemKey = it)
								}
								BucketItemType.Type.MOVIES -> null
								BucketItemType.Type.TVSHOWS -> null
								BucketItemType.Type.MEDIA -> null
								BucketItemType.Type.LINKS -> null
							}
						}
					}
				}
			}

			AddNewBucketItemButton(
				bottomSheetType = viewModel.activityState.bottomSheetType.value,
				bucketItemType = viewModel.bucketItemType,
				isSelected = isSelected,
				selectedBucketItemList = viewModel.activityState.selectedBucketItemList
			) {
				if (isSelected) {
					viewModel.activityState.showDeleteDialog.value = true
				} else {
					viewModel.activityState.bottomSheetType.value = when (viewModel.bucketItemType) {
						BucketItemType.Type.TODO -> BottomSheetType.AddBookSheet
						BucketItemType.Type.BOOKS -> BottomSheetType.AddBookSheet
						BucketItemType.Type.MOVIES -> BottomSheetType.AddBookSheet
						BucketItemType.Type.TVSHOWS -> BottomSheetType.AddBookSheet
						BucketItemType.Type.MEDIA -> BottomSheetType.AddBookSheet
						BucketItemType.Type.LINKS -> BottomSheetType.AddBookSheet
					}

					scope.launch {
						viewModel.activityState.bottomSheetState.show()
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class ActivityState(
		val coroutineScope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
		var isSelected: MutableState<Boolean>,
		val selectedBucketItemList: SnapshotStateList<String>,
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false),
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberBucketActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
		isSelectedToDelete: MutableState<Boolean> = remember { mutableStateOf(false) },
		selectedToDeleteList: SnapshotStateList<String> = remember { mutableStateListOf() }
	) = remember {
		ActivityState(coroutineScope, bottomSheetState, collapsingToolbarScaffoldState, isSelectedToDelete, selectedToDeleteList)
	}
}
