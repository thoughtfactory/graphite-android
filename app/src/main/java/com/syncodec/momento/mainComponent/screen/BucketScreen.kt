package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.ResourceMap
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import compose.icons.TablerIcons
import compose.icons.tablericons.Book
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BucketScreen() {

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()

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
		backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
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
					tint = MaterialTheme.colorScheme.onTertiaryContainer,
					modifier = Modifier
						.size(24.dp)
				)
				Text(
					text = "${bucket.containerSize}",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onTertiaryContainer,
				)
			}
			Text(
				text = bucket.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.tertiary,
				fontWeight = FontWeight.ExtraBold
			)
		}
	}
}
