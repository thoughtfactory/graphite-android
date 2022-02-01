package com.syncodec.momento.bucketComponent.screen

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketItemActivity
import com.syncodec.momento.bucketComponent.BucketViewModel
import com.syncodec.momento.bucketComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MoviesScreen() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	val viewModel: BucketViewModel = viewModel()
	val scope = rememberCoroutineScope()

	var isSelectedToDelete by viewModel.bucketActivityState.isSelectedToDelete
	val selectedToDeleteList: MutableList<String> = viewModel.bucketActivityState.selectedToDeleteList

	val activity = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) {
		viewModel.openBucket()
	}

	AnimatedVisibility(
		visible = viewModel.bucketItemList.isEmpty(),
		enter = fadeIn(),
		exit = fadeOut()
	) {
		NoMoviesCard(
			modifier = Modifier
				.fillMaxWidth()
		) {
			viewModel.bucketActivityState.bottomSheetType.value = BottomSheetType.AddMovieSheet
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
				.fillMaxSize()
				.padding(8.dp, 0.dp)
		) {
			viewModel.bucketItemList.forEach { bucketItem ->
				item {
					MovieItem(
						bucketItem = bucketItem,
						thumbnail = bucketItem.thumbnail,
						highlight = isSelectedToDelete and (bucketItem.primaryKey in selectedToDeleteList),
						modifier = Modifier
							.aspectRatio(0.75f)
							.combinedClickable(
								enabled = true,
								onClick = {
									if (isSelectedToDelete) {
										if (bucketItem.primaryKey in selectedToDeleteList) {
											selectedToDeleteList.remove(bucketItem.primaryKey)
										} else {
											selectedToDeleteList.add(bucketItem.primaryKey)
										}
									} else {
										Intent(context, BucketItemActivity::class.java).apply {
											putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, BucketItemType.MOVIES.ordinal)
											putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, bucketItem.bucketKey)
											putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_KEY.name, bucketItem.primaryKey)
											putExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name, bucketItem.innerContent)
											activity.launch(this)
										}
									}
								},
								onLongClick = {
									if (!isSelectedToDelete) {
										isSelectedToDelete = true
									}
									if (bucketItem.primaryKey in selectedToDeleteList) {
										selectedToDeleteList.remove(bucketItem.primaryKey)
									} else {
										selectedToDeleteList.add(bucketItem.primaryKey)
									}
								}
							)
					)
				}
			}
			when (viewModel.bucketItemList.size % 3) {
				0 -> {
					for (i in 0 until 3) {
						item {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.height(screenHeight - 64.dp - 64.dp - (screenWidth / 3) - 16.dp)
							)
						}
					}
				}
				1 -> {
					for (i in 0 until 2) {
						item {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.height(screenHeight - 64.dp - 64.dp)
							)
						}
					}
				}
				2 -> {
					for (i in 0 until 1) {
						item {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.height(screenHeight - 64.dp - 64.dp)
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun NoMoviesCard(
	modifier: Modifier = Modifier,
	openSheet: (BottomSheetType) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth(0.8f)
				.height(screenHeight - 256.dp)
				.padding(12.dp),
		) {
			Image(
				painter = painterResource(id = R.drawable.il_watching_movie),
				contentDescription = null,
				modifier = Modifier
					.requiredSize(192.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			OutlinedButton(
				onClick = { openSheet(BottomSheetType.AddMovieSheet) },
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				),
				modifier = Modifier
					.fillMaxWidth()
			) {
				Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add some movies",
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
				)

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "Which one were your favourites",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.Bold
				)
			}
		}
	}

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MovieItem(
	modifier: Modifier,
	bucketItem: BucketItem,
	thumbnail: ByteArray?,
	highlight: Boolean
) {

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 12.dp,
			border = BorderStroke(4.dp, if (highlight) MaterialTheme.colorScheme.primary else Color.Transparent),
			modifier = modifier,
		) {
			if (thumbnail != null) {
				Image(
					painter = rememberImagePainter(
						data = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size),
						builder = {
							crossfade(false)
						}
					),
					contentDescription = null,
					contentScale = ContentScale.Crop,
				)
			}
		}

		Text(
			text = bucketItem.title ?: "",
			style = MaterialTheme.typography.bodyMedium,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
