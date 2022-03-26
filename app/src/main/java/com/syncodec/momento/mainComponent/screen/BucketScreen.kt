package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.ResourceMap
import com.syncodec.momento.mainComponent.miscellaneous.TopBar

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BucketScreen(
	bucketMap: Map<String, Pair<BucketDbEntry, Int>>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {

	val chipDataList: MutableList<ChipData> = mutableListOf()
	val isChipSelected: MutableMap<BucketItemType, Boolean> = mutableMapOf()

	BucketItemType.values().forEach {
		var _isSelected by remember { mutableStateOf(true) }
		ChipData(
			title = ResourceMap.BucketItemNameMap[it]!!,
			imageVector = ResourceMap.bucketTypeToIcon[it]!!,
			isSelected = _isSelected
		) { _isSelected = !_isSelected }.apply { chipDataList.add(this) }
		isChipSelected[it] = _isSelected
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
	) {
		TopBar(
			isSelected = isSelected,
			selectedItemSize = selectedItemList.size
		) { click, data -> onClick(click, data) }

		if (bucketMap.isNotEmpty()) {
			Spacer(modifier = Modifier.height(8.dp))
			ChipView(chipDataList = chipDataList)
			LazyVerticalGrid(
				columns = GridCells.Adaptive(minSize = 144.dp),
				modifier = Modifier.padding(4.dp),
			) {
				bucketMap.forEach { (_, data) ->
					if (isChipSelected[BucketItemType.values()[data.first.bucketItemType]]!!) {
						item {
							BucketCard(
								bucket = data.first,
								bucketSize = data.second
							)
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
	Box(
		modifier = Modifier
			.fillMaxSize()
			.graphicsLayer {
//				this.scaleX = scaffoldScale
//				this.scaleY = scaffoldScale
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
	bucket: BucketDbEntry,
	bucketSize: Int?
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
					putExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name, bucket.key)
					putExtra(Konstant.Companion.Konstant.BUCKET_TYPE.name, bucket.bucketItemType)
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
					imageVector = ResourceMap.bucketTypeToIcon[BucketItemType.values()[bucket.bucketItemType]]!!,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.size(24.dp)
				)
				Text(
					text = "$bucketSize",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
			Text(
				text = bucket.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				fontWeight = FontWeight.ExtraBold
			)
		}
	}
}
