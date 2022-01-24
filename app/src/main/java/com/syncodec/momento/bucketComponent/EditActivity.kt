package com.syncodec.momento.bucketComponent


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.bucketComponent.miscellaneous.BucketTopBar
import com.syncodec.momento.bucketComponent.modalBottomSheet.AddBookSheet
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.ui.theme.MomentoTheme

class EditActivity : ComponentActivity() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private lateinit var bucketItemType: BucketItemType
	private lateinit var bookData: BookData

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		bucketItemType = BucketItemType.values()[intent.getIntExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.TODO.ordinal)]
		bookData = objectMapper.readValue(intent.getStringExtra(Konstant.Companion.Konstant.BOOK_DATA.name)!!)

		setContent {
			MomentoTheme {
				EditScreen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Preview
	@Composable
	private fun EditScreen() {
		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(Color.White)

		val configuration = LocalConfiguration.current

		val screenHeight = configuration.screenHeightDp.dp
		val screenWidth = configuration.screenWidthDp.dp

		val scope = rememberCoroutineScope()
		val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

		val scrollState = rememberScrollState()

		ModalBottomSheetLayout(
			sheetState = bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				AddBookSheet()
			},
		) {
			Scaffold(
				topBar = { BucketTopBar() }
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.fillMaxHeight()
						.padding(16.dp, 0.dp)
						.verticalScroll(scrollState),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Card(
						shape = RoundedCornerShape(12.dp),
						elevation = 16.dp,
						modifier = Modifier
							.requiredWidth(192.dp)
							.aspectRatio(0.75f)
							.padding(16.dp),
						onClick = {
						}
					) {
						Image(
							painter = rememberImagePainter(
								data = "https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg",
							),
							contentDescription = null,
							modifier = Modifier.fillMaxSize(),
							contentScale = ContentScale.Crop
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					androidx.compose.material3.Text(
						text = bookData.title,
						style = androidx.compose.material3.MaterialTheme.typography.titleLarge
					)

				}
			}
		}
	}
}
