package com.syncodec.momento.bucketComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.miscellaneous.BucketTopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.bucketComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.bucketComponent.screen.BooksScreen
import com.syncodec.momento.bucketComponent.screen.MoviesScreen
import com.syncodec.momento.bucketComponent.screen.TodoScreen
import com.syncodec.momento.custom.DotsPulsing
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

class BucketActivity : ComponentActivity() {

	private val viewModel by viewModels<BucketViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.bucketKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		viewModel.bucketItemType = BucketItemType.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.TODO.ordinal)]
		viewModel.openBucket()

		setContent {
			viewModel.bucketActivityState = rememberBucketActivityState()
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)
				BucketScreen()
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.bucketActivityState.isSelectedToDelete.value) {
			viewModel.bucketActivityState.selectedToDeleteList.removeAll { true }
			viewModel.bucketActivityState.isSelectedToDelete.value = false
		} else {
			finish()
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun BucketScreen() {
		val configuration = LocalConfiguration.current
		val screenWidth = configuration.screenWidthDp.dp
		val screenHeight = configuration.screenHeightDp.dp

		val scope = rememberCoroutineScope()

		val collapsingToolbarScaffoldState = viewModel.bucketActivityState.collapsingToolbarScaffoldState

		val status: Int by viewModel.status

		ModalBottomSheetLayout(
			sheetState = viewModel.bucketActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout()
			},
		) {
			Box(
				modifier = Modifier,
				contentAlignment = Alignment.BottomCenter
			) {
				CollapsingToolbarScaffold(
					state = collapsingToolbarScaffoldState,
					scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
					modifier = Modifier
						.fillMaxSize(),
					toolbar = {
						BucketTopBar()
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(256.dp)
								.padding(16.dp, 64.dp, 16.dp, 16.dp)
								.road(Alignment.CenterStart, Alignment.BottomEnd)
						) {
							Image(
								painter = when (viewModel.bucketItemType) {
									BucketItemType.TODO -> painterResource(id = R.drawable.il_book_screen_header)
									BucketItemType.BOOKS -> painterResource(id = R.drawable.il_book_screen_header)
									BucketItemType.MOVIES -> painterResource(id = R.drawable.il_movie_screen_header)
									BucketItemType.TVSHOWS -> painterResource(id = R.drawable.il_book_screen_header)
									BucketItemType.MEDIA -> painterResource(id = R.drawable.il_book_screen_header)
									BucketItemType.LINKS -> painterResource(id = R.drawable.il_book_screen_header)
								},
								contentDescription = null,
								contentScale = ContentScale.Fit,
								modifier = Modifier
									.fillMaxWidth()
									.height(256.dp)
									.graphicsLayer {
										this.alpha = collapsingToolbarScaffoldState.toolbarState.progress
									},
							)
						}
					}
				) {
					when (status) {
						1 -> {
							when (viewModel.bucketItemType) {
								BucketItemType.TODO -> TodoScreen()
								BucketItemType.BOOKS -> BooksScreen()
								BucketItemType.MOVIES -> MoviesScreen()
								BucketItemType.TVSHOWS -> {}
								BucketItemType.MEDIA -> {}
								BucketItemType.LINKS -> {}
							}
						}
						0 -> {
							Box(
								contentAlignment = Alignment.BottomCenter,
								modifier = Modifier
									.fillMaxSize(),
							) {
								DotsPulsing()
							}
						}
						-2 -> {}
						-1 -> {}
						else -> {}
					}
				}

				if (viewModel.bucketItemList.isNotEmpty()) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
					) {
						LargeButton(
							text = if (viewModel.bucketActivityState.isSelectedToDelete.value)
								"Delete"
							else when (viewModel.bucketItemType) {
								BucketItemType.TODO -> "Add New Task"
								BucketItemType.BOOKS -> "What did you read?"
								BucketItemType.MOVIES -> "A new movie?"
								BucketItemType.TVSHOWS -> "What did you watch?"
								BucketItemType.MEDIA -> "Add media"
								BucketItemType.LINKS -> "Add link"
							},
							backgroundColor = MaterialTheme.colorScheme.primaryContainer,
							textColor = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier
								.fillMaxWidth()
								.height(48.dp)
								.padding(24.dp, 0.dp)
								.focusable()
						) {
							if (viewModel.bucketActivityState.isSelectedToDelete.value) {
								viewModel.deleteBucketItem(viewModel.bucketActivityState.selectedToDeleteList!!)
							}
							else {
								viewModel.bucketActivityState.bottomSheetType.value = when (viewModel.bucketItemType) {
									BucketItemType.TODO -> BottomSheetType.AddMovieSheet
									BucketItemType.BOOKS -> BottomSheetType.AddBookSheet
									BucketItemType.MOVIES -> BottomSheetType.AddMovieSheet
									BucketItemType.TVSHOWS -> BottomSheetType.AddMovieSheet
									BucketItemType.MEDIA -> BottomSheetType.AddMovieSheet
									BucketItemType.LINKS -> BottomSheetType.AddMovieSheet
								}

								scope.launch {
									viewModel.bucketActivityState.bottomSheetState.show()
								}
							}
						}

						Spacer(modifier = Modifier.height(24.dp))
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class BucketActivityState(
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState,
		var isSelectedToDelete: MutableState<Boolean>,
		val selectedToDeleteList: SnapshotStateList<String>
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberBucketActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState(),
		isSelectedToDelete: MutableState<Boolean> = remember { mutableStateOf(false) },
		selectedToDeleteList: SnapshotStateList<String> = remember { mutableStateListOf() }
	) = remember {
		BucketActivityState(bottomSheetState, collapsingToolbarScaffoldState, isSelectedToDelete, selectedToDeleteList)
	}
}
