package com.syncodec.momento.bucketComponent


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.miscellaneous.BucketItemTopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.AddBookSheet
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.bucketComponent.modalBottomSheet.MovieData
import com.syncodec.momento.bucketComponent.screen.BooksItemScreen
import com.syncodec.momento.bucketComponent.screen.MovieCharacterData
import com.syncodec.momento.bucketComponent.screen.MoviesItemScreen
import com.syncodec.momento.bucketComponent.screen.TodoScreen
import com.syncodec.momento.custom.DotsPulsing
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import org.json.JSONObject

class BucketItemActivity : ComponentActivity() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val viewModel by viewModels<BucketItemViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.bucketItemType = BucketItemType.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.TODO.ordinal)]
		viewModel.bucketKey = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_KEY.name)!!
		viewModel.bucketItemKey = intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name)
		viewModel.bucketItemDataJson = JSONObject(intent.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name)!!)

		if (viewModel.bucketItemKey == null) {
			viewModel.generateNewBucketItem()
		} else {
			viewModel.readBucketItem()
		}

		setContent {
			viewModel.bucketItemActivityState = rememberBucketItemActivityState()

			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)

				EditScreen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Preview
	@Composable
	private fun EditScreen() {

		val status: Int by viewModel.status

		when(status) {
			1 -> {
				when (viewModel.bucketItemType) {
					BucketItemType.TODO -> {}
					BucketItemType.BOOKS -> {}
					BucketItemType.MOVIES -> { }
					BucketItemType.TVSHOWS -> {}
					BucketItemType.MEDIA -> {}
					BucketItemType.LINKS -> {}
				}
			}
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.bucketItemActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				AddBookSheet()
			},
		) {
			Scaffold(
				topBar = { BucketItemTopBar() }
			) {
				if (viewModel.bucketItemKey == null) {
					when (viewModel.bucketItemType) {
						BucketItemType.TODO -> TodoScreen()
						BucketItemType.BOOKS -> BooksItemScreen()
						BucketItemType.MOVIES -> {
							MoviesItemScreen()
						}
						BucketItemType.TVSHOWS -> {}
						BucketItemType.MEDIA -> {}
						BucketItemType.LINKS -> {}
					}
				} else {
					when (status) {
						1 -> {
							when (viewModel.bucketItemType) {
								BucketItemType.TODO -> TodoScreen()
								BucketItemType.BOOKS -> BooksItemScreen()
								BucketItemType.MOVIES -> {
									MoviesItemScreen()
								}
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
	}

	@OptIn(ExperimentalMaterialApi::class)
	class BucketItemActivityState(
		val bottomSheetState: ModalBottomSheetState,
		val collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AddBookSheet)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberBucketItemActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		collapsingToolbarScaffoldState: CollapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
	) = remember {
		BucketItemActivityState(bottomSheetState, collapsingToolbarScaffoldState)
	}
}
