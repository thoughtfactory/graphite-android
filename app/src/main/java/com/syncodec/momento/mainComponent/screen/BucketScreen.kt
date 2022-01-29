package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.ResourceMap
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BucketScreen() {

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}


	val bucketList by viewModel.bucketRepository.bucketDbEntryListLiveData.observeAsState()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.fillMaxHeight()
			.background(MaterialTheme.colorScheme.background)
	) {
		MainTopBar(
			showBackground = true,
			openSheet = { bottomSheetType ->
				viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
				scope.launch {
					viewModel.mainActivityState.bottomSheetState.show()
				}
			}
		)

		if (bucketList?.isNotEmpty() == true) {
			LazyVerticalGrid(
				cells = GridCells
					.Adaptive(minSize = 144.dp),
				modifier = Modifier
					.padding(4.dp)
			) {
				bucketList?.forEach { bucket ->
					item {
						BucketCard(
							bucket
						)
					}
				}
			}
		} else {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.padding(16.dp),
				contentAlignment = Alignment.Center
			) {
				Column {
					NoBucketCard(
						modifier = Modifier
							.fillMaxWidth()
							.height(320.dp)
					) { bottomSheetType ->
						openSheet(bottomSheetType)
					}
					Spacer(modifier = Modifier.height(88.dp))
				}
			}
		}
	}
}

@Composable
private fun NoBucketCard(
	modifier: Modifier = Modifier,
	openSheet: (BottomSheetType) -> Unit
) {
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
				.fillMaxHeight()
				.padding(12.dp),
		) {
			Image(
				painter = painterResource(id = R.drawable.il_bucket),
				contentDescription = null,
				modifier = Modifier
					.requiredSize(192.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			OutlinedButton(
				onClick = { openSheet(BottomSheetType.BucketBottomSheet) },
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				)
			) {
				androidx.compose.material.Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add new bucket",
					tint = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "Add your first bucket",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BucketCard(
	bucket: BucketDbEntry
) {
	val context = LocalContext.current

	Card(
		modifier = Modifier
			.padding(6.dp)
			.height(96.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
		onClick = {
			Intent(context, BucketActivity::class.java).apply {
				putExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name, bucket.primaryKey)
				putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, bucket.bucketType)
				context.startActivity(this)
			}
		}
	) {
		Column(
			modifier = Modifier
				.padding(12.dp)
				.fillMaxWidth()
				.fillMaxHeight(),
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.Start
		) {
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.values()[bucket.bucketType]]!!,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.size(24.dp)
				)
				Text(
					text = "${bucket.containerSize}",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
			Text(
				text = bucket.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.secondary,
				fontWeight = FontWeight.ExtraBold
			)
		}
	}
}
