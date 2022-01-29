package com.syncodec.momento.bucketComponent.screen

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketViewModel
import com.syncodec.momento.bucketComponent.BucketItemActivity
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import compose.icons.TablerIcons
import compose.icons.tablericons.CircleDotted
import compose.icons.tablericons.Plus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BooksScreen() {

	val viewModel: BucketViewModel = viewModel()
	val scope = rememberCoroutineScope()

	AnimatedVisibility(
		visible = viewModel.bucketItemList.isEmpty(),
		enter = fadeIn(),
		exit = fadeOut()
	) {
		NoBookCard {
			viewModel.bucketActivityState.bottomSheetType.value = BottomSheetType.AddBookSheet
			scope.launch {
				viewModel.bucketActivityState.bottomSheetState.show()
			}
		}
	}

	AnimatedVisibility(
		visible = viewModel.bucketItemList.isNotEmpty(),
		enter = fadeIn(),
		exit = fadeOut()
	) {
		LazyVerticalGrid(
			cells = GridCells.Adaptive(96.dp),
			modifier = Modifier
				.padding(8.dp)
		) {
			item {
				AddBookItem(
					modifier = Modifier
						.aspectRatio(0.75f)
						.clickable { }
				)
			}
			viewModel.bucketItemList.forEach {
				item {
					BookItem(
						modifier = Modifier
							.aspectRatio(0.75f)
					)
				}
			}
		}
	}
}

@Composable
private fun NoBookCard(
	modifier: Modifier = Modifier,
	openSheet: (BottomSheetType) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.47f),
		modifier = modifier
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.height(screenHeight - 256.dp)
				.padding(12.dp),
		) {
			Image(
				painter = painterResource(id = R.drawable.il_reading),
				contentDescription = null,
				modifier = Modifier
					.requiredSize(192.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			OutlinedButton(
				onClick = { openSheet(BottomSheetType.AddBookSheet) },
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				),
				modifier = Modifier
					.fillMaxWidth(0.8f)
			) {
				Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add some books",
					tint = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "You must have read something",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.Bold
				)
			}
		}
	}

}


@Composable
private fun AddBookItem(
	modifier: Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			modifier = modifier,
		) {
			Icon(
				imageVector = TablerIcons.CircleDotted,
				contentDescription = null,
				tint = Color.LightGray,
				modifier = Modifier
					.requiredSize(40.dp)
			)
			Icon(
				imageVector = TablerIcons.Plus,
				contentDescription = null,
				tint = Color.LightGray,
				modifier = Modifier
					.requiredSize(20.dp)
			)
		}

		Text(
			text = "A new book?",
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookItem(
	modifier: Modifier
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			onClick = {
				Intent(context, BucketItemActivity::class.java).apply {
					val bookDataString =
						"{\"key\":\"/works/OL21667536W\",\"title\":\"How to Avoid a Climate Disaster\",\"cover_i\":10656063,\"author_name\":[\"Bill Gates\"]}"
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.BOOKS)
//					putExtra(Konstant.Companion.Konstant.BOOK_DATA.name, bookDataString)
					context.startActivity(this)
				}
			}
		) {
			Image(
				painter = painterResource(id = R.drawable.home_background),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = modifier,
			)
		}

		Text(
			text = "Book",
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
