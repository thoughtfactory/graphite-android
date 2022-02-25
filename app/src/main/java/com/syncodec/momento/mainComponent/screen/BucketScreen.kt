package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.ResourceMap
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.TopBar

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BucketScreen() {

	val viewModel: MainViewModel = viewModel()

	val bucketList by viewModel.getBucketList().observeAsState()

	val chipDataList: MutableList<ChipData> = mutableListOf()
	val isChipSelected: MutableMap<BucketItemType.Type, Boolean> = mutableMapOf()

	BucketItemType.Type.values().forEach {
		var isSelected by remember { mutableStateOf(true) }
		ChipData(
			title = ResourceMap.BucketItemNameMap[it]!!,
			imageVector = ResourceMap.bucketTypeToIcon[it]!!,
			isSelected = isSelected
		) { isSelected = !isSelected }.apply { chipDataList.add(this) }
		isChipSelected[it] = isSelected
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
	) {
		TopBar()
		if (bucketList?.isNotEmpty() == true) {
			Spacer(modifier = Modifier.height(8.dp))
			ChipView(chipDataList = chipDataList)
			LazyVerticalGrid(
				cells = GridCells
					.Adaptive(minSize = 144.dp),
				modifier = Modifier
					.padding(4.dp)
			) {
				bucketList?.forEach { bucket ->
					if (isChipSelected[BucketItemType.Type.values()[bucket.bucketType]]!!) {
						item {
							BucketCard(bucket)
						}
					}
				}
			}
		} else {
			NoBucketCard()
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NoBucketCard() {
	val viewModel: MainViewModel = viewModel()
	val scaffoldScale by animateFloatAsState(
		targetValue = if (viewModel.activityState.bottomSheetState.progress.to == ModalBottomSheetValue.Hidden) 1f else 0.95f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioHighBouncy,
			stiffness = Spring.StiffnessMediumLow
		),
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.graphicsLayer {
				this.scaleX = scaffoldScale
				this.scaleY = scaffoldScale
			},
		contentAlignment = Alignment.Center
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BucketCard(
	bucket: BucketDbEntry
) {
	val context = LocalContext.current

	Box(
		modifier = Modifier
			.padding(4.dp)
			.height(96.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.secondaryContainer)
			.clickable {
				Intent(context, BucketActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name, bucket.primaryKey)
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, bucket.bucketType)
					context.startActivity(this)
				}
			},
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
					imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.Type.values()[bucket.bucketType]]!!,
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
