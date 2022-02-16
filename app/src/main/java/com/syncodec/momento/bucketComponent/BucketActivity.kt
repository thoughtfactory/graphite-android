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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
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
import com.syncodec.momento.konstant.Status
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
		viewModel.bucketItemType =
			BucketItemType.Type.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.Type.TODO.ordinal)]
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
			super.onBackPressed()
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
	@Composable
	private fun BucketScreen() {

		val collapsingToolbarScaffoldState = viewModel.bucketActivityState.collapsingToolbarScaffoldState

		val status by viewModel.status

		ModalBottomSheetLayout(
			sheetState = viewModel.bucketActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout()
			},
		) {
			when (status) {
				Status.INIT -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxSize(),
					) {
						val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

						LottieAnimation(
							composition = lottieComposition,
							iterations = LottieConstants.IterateForever,
							modifier = Modifier
								.requiredSize(128.dp)
						)
					}
				}
				Status.LOADING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxSize(),
					) {
						val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

						LottieAnimation(
							composition = lottieComposition,
							iterations = LottieConstants.IterateForever,
							modifier = Modifier
								.requiredSize(128.dp)
						)
					}
				}
				Status.LOADED -> {
					if (viewModel.bucketItemList.isEmpty()) {
						EmptyBucketView()
					} else {
						CollapsingToolbarScaffold(
							modifier = Modifier.fillMaxSize(),
							state = collapsingToolbarScaffoldState,
							scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
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
											BucketItemType.Type.TODO -> painterResource(id = R.drawable.il_book_screen_header)
											BucketItemType.Type.BOOKS -> painterResource(id = R.drawable.il_book_screen_header)
											BucketItemType.Type.MOVIES -> painterResource(id = R.drawable.il_movie_screen_header)
											BucketItemType.Type.TVSHOWS -> painterResource(id = R.drawable.il_book_screen_header)
											BucketItemType.Type.MEDIA -> painterResource(id = R.drawable.il_book_screen_header)
											BucketItemType.Type.LINKS -> painterResource(id = R.drawable.il_book_screen_header)
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
							when (viewModel.bucketItemType) {
								BucketItemType.Type.TODO -> TodoScreen()
								BucketItemType.Type.BOOKS -> BooksScreen()
								BucketItemType.Type.MOVIES -> MoviesScreen()
								BucketItemType.Type.TVSHOWS -> {
								}
								BucketItemType.Type.MEDIA -> {
								}
								BucketItemType.Type.LINKS -> {
								}
							}
						}

						AddNewBucketItemButton()
					}
				}
			}

		}
	}

	@Composable
	private fun EmptyBucketView() {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			BucketTopBar()
			Box(
				modifier = Modifier
					.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Spacer(modifier = Modifier.height(24.dp))
					Image(
						painter = painterResource(id = R.drawable.il_reading),
						contentDescription = "No diary entries",
						modifier = Modifier
							.fillMaxWidth(0.5f)
					)

					Spacer(modifier = Modifier.height(24.dp))

					Text(
						text = "The town was paper, but the memories were not.",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.fillMaxWidth(0.71f)
					)

					Spacer(modifier = Modifier.height(16.dp))

					Text(
						text = "~ John Green, Paper Towns",
						style = MaterialTheme.typography.bodySmall,
						fontStyle = FontStyle.Italic,
						textAlign = TextAlign.End,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.fillMaxWidth(0.71f)
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun AddNewBucketItemButton() {
		val scope = rememberCoroutineScope()

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(0.dp, 0.dp, 0.dp, 24.dp),
			contentAlignment = Alignment.BottomCenter
		) {
			LargeButton(
				text = if (viewModel.bucketActivityState.isSelectedToDelete.value)
					"Delete"
				else when (viewModel.bucketItemType) {
					BucketItemType.Type.TODO -> "Add New Task"
					BucketItemType.Type.BOOKS -> "What did you read?"
					BucketItemType.Type.MOVIES -> "A new movie?"
					BucketItemType.Type.TVSHOWS -> "What did you watch?"
					BucketItemType.Type.MEDIA -> "Add media"
					BucketItemType.Type.LINKS -> "Add link"
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
					viewModel.deleteBucketItem(viewModel.bucketActivityState.selectedToDeleteList)
				} else {
					viewModel.bucketActivityState.bottomSheetType.value = when (viewModel.bucketItemType) {
						BucketItemType.Type.TODO -> BottomSheetType.AddMovieSheet
						BucketItemType.Type.BOOKS -> BottomSheetType.AddBookSheet
						BucketItemType.Type.MOVIES -> BottomSheetType.AddMovieSheet
						BucketItemType.Type.TVSHOWS -> BottomSheetType.AddMovieSheet
						BucketItemType.Type.MEDIA -> BottomSheetType.AddMovieSheet
						BucketItemType.Type.LINKS -> BottomSheetType.AddMovieSheet
					}

					scope.launch {
						viewModel.bucketActivityState.bottomSheetState.show()
					}
				}
			}
		}
	}

//	@OptIn(ExperimentalMaterialApi::class)
//	@Composable
//	private fun BucketScreen() {
//		val scope = rememberCoroutineScope()
//
//		val collapsingToolbarScaffoldState = viewModel.bucketActivityState.collapsingToolbarScaffoldState
//
//		val status: Int by viewModel.status
//
//		ModalBottomSheetLayout(
//			sheetState = viewModel.bucketActivityState.bottomSheetState,
//			sheetElevation = 0.dp,
//			sheetBackgroundColor = Color.Transparent,
//			sheetContent = {
//				SheetLayout()
//			},
//		) {
//			Box(
//				modifier = Modifier,
//				contentAlignment = Alignment.BottomCenter
//			) {
//				CollapsingToolbarScaffold(
//					state = collapsingToolbarScaffoldState,
//					scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
//					modifier = Modifier
//						.fillMaxSize(),
//					toolbar = {
//						BucketTopBar()
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(256.dp)
//								.padding(16.dp, 64.dp, 16.dp, 16.dp)
//								.road(Alignment.CenterStart, Alignment.BottomEnd)
//						) {
//							Image(
//								painter = when (viewModel.bucketItemType) {
//									BucketItemType.Type.TODO -> painterResource(id = R.drawable.il_book_screen_header)
//									BucketItemType.Type.BOOKS -> painterResource(id = R.drawable.il_book_screen_header)
//									BucketItemType.Type.MOVIES -> painterResource(id = R.drawable.il_movie_screen_header)
//									BucketItemType.Type.TVSHOWS -> painterResource(id = R.drawable.il_book_screen_header)
//									BucketItemType.Type.MEDIA -> painterResource(id = R.drawable.il_book_screen_header)
//									BucketItemType.Type.LINKS -> painterResource(id = R.drawable.il_book_screen_header)
//								},
//								contentDescription = null,
//								contentScale = ContentScale.Fit,
//								modifier = Modifier
//									.fillMaxWidth()
//									.height(256.dp)
//									.graphicsLayer {
//										this.alpha = collapsingToolbarScaffoldState.toolbarState.progress
//									},
//							)
//						}
//					}
//				) {
//					when (status) {
//						1 -> {
//							when (viewModel.bucketItemType) {
//								BucketItemType.Type.TODO -> TodoScreen()
//								BucketItemType.Type.BOOKS -> BooksScreen()
//								BucketItemType.Type.MOVIES -> MoviesScreen()
//								BucketItemType.Type.TVSHOWS -> {}
//								BucketItemType.Type.MEDIA -> {}
//								BucketItemType.Type.LINKS -> {}
//							}
//						}
//						0 -> {
//							Box(
//								contentAlignment = Alignment.BottomCenter,
//								modifier = Modifier
//									.fillMaxSize(),
//							) {
//								DotsPulsing()
//							}
//						}
//						-2 -> {}
//						-1 -> {}
//						else -> {}
//					}
//				}
//
//				if (viewModel.bucketItemList.isNotEmpty()) {
//					Column(
//						modifier = Modifier
//							.fillMaxWidth()
//					) {
//						LargeButton(
//							text = if (viewModel.bucketActivityState.isSelectedToDelete.value)
//								"Delete"
//							else when (viewModel.bucketItemType) {
//								BucketItemType.Type.TODO -> "Add New Task"
//								BucketItemType.Type.BOOKS -> "What did you read?"
//								BucketItemType.Type.MOVIES -> "A new movie?"
//								BucketItemType.Type.TVSHOWS -> "What did you watch?"
//								BucketItemType.Type.MEDIA -> "Add media"
//								BucketItemType.Type.LINKS -> "Add link"
//							},
//							backgroundColor = MaterialTheme.colorScheme.primaryContainer,
//							textColor = MaterialTheme.colorScheme.onPrimaryContainer,
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(48.dp)
//								.padding(24.dp, 0.dp)
//								.focusable()
//						) {
//							if (viewModel.bucketActivityState.isSelectedToDelete.value) {
//								viewModel.deleteBucketItem(viewModel.bucketActivityState.selectedToDeleteList!!)
//							}
//							else {
//								viewModel.bucketActivityState.bottomSheetType.value = when (viewModel.bucketItemType) {
//									BucketItemType.Type.TODO -> BottomSheetType.AddMovieSheet
//									BucketItemType.Type.BOOKS -> BottomSheetType.AddBookSheet
//									BucketItemType.Type.MOVIES -> BottomSheetType.AddMovieSheet
//									BucketItemType.Type.TVSHOWS -> BottomSheetType.AddMovieSheet
//									BucketItemType.Type.MEDIA -> BottomSheetType.AddMovieSheet
//									BucketItemType.Type.LINKS -> BottomSheetType.AddMovieSheet
//								}
//
//								scope.launch {
//									viewModel.bucketActivityState.bottomSheetState.show()
//								}
//							}
//						}
//
//						Spacer(modifier = Modifier.height(24.dp))
//					}
//				}
//			}
//		}
//	}

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
