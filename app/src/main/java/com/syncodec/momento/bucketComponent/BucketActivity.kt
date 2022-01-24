package com.syncodec.momento.bucketComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.miscellaneous.BucketTopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.AddBookSheet
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.bucketComponent.screen.BooksScreen
import com.syncodec.momento.bucketComponent.screen.TodoScreen
import com.syncodec.momento.custom.DotsPulsing
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.ui.theme.MomentoTheme
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

class BucketActivity : ComponentActivity() {

	private val viewModel by viewModels<BucketViewModel>()

	private lateinit var primaryKey: String
	private lateinit var bucketItemType: BucketItemType

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		primaryKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		bucketItemType = BucketItemType.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.TODO.ordinal)]

		viewModel.openBucket(primaryKey)

		setContent {
			viewModel.bucketActivityState = rememberBucketActivityState()

			MomentoTheme {
				BucketScreen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun BucketScreen() {

		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(Color.White)

		ModalBottomSheetLayout(
			sheetState = viewModel.bucketActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				AddBookSheet()
			},
		) {
			CollapsingToolbarScaffold(
				state = viewModel.bucketActivityState.collapsingToolbarScaffoldState,
				scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
				modifier = Modifier,
				toolbar = {
					BucketTopBar()
					Image(
						painter = painterResource(id = R.drawable.home_background2),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp)
							.blur(4.dp),
					)
				}
			) {
//				when (bucketItemType) {
//					BucketItemType.TODO -> TodoScreen()
//					BucketItemType.BOOKS -> BooksScreen()
//					BucketItemType.MOVIES -> {}
//					BucketItemType.TVSHOWS -> {}
//					BucketItemType.MEDIA -> {}
//					BucketItemType.LINKS -> {}
//				}
				when (viewModel.status.value) {
					1 -> {
						when (bucketItemType) {
							BucketItemType.TODO -> TodoScreen()
							BucketItemType.BOOKS -> BooksScreen()
							BucketItemType.MOVIES -> {}
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
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class BucketActivityState(
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberBucketActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
	) = remember {
		BucketActivityState(bottomSheetState, collapsingToolbarScaffoldState)
	}
}
