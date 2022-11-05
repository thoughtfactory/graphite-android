package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@Composable
fun MenuBottomSheet() {
	val buttonList: List<BottomSheetButtonData> = remember {
		listOf(
			BottomSheetButtonData(
				title = "Edit",
				icon = R.drawable.ic_pencil,
				onClick = {}
			),
			BottomSheetButtonData(
				title = "Pin",
				icon = R.drawable.ic_pencil,
				onClick = {}
			),
			BottomSheetButtonData(
				title = "Delete",
				icon = R.drawable.ic_delete,
				containerColor = Color.DeleteContainer,
				contentColor = Color.DeleteContent,
				onClick = {}
			),
		)
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu,
		)

		Spacer(modifier = Modifier.height(8.dp))

		BottomSheetButtonGrid(buttonList = buttonList)

		Spacer(modifier = Modifier.height(6.dp))

		DataView(
			allCount = 0,
			alphaCount = 0,
			betaCount = 0,
			gammaCount = 0,
			bucketType = BucketType.BOOK.name
		)

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun ColumnScope.DataView(
	allCount: Int,
	alphaCount: Int,
	betaCount: Int,
	gammaCount: Int,
	bucketType: String
) {
	this.apply {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
		) {
			DataItemView(
				text = allCount.toString(),
				contentDescription = "All count",
				icon = R.drawable.ic_state,
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(4.dp))
			DataItemView(
				text = alphaCount.toString(),
				contentDescription = when (bucketType) {
					BucketType.TODO.name -> "Todo count"
					BucketType.BOOK.name -> "To read count"
					BucketType.SHOW.name -> "To watch count"
					BucketType.LINK.name -> "Todo count"
					else -> "ERROR"
				},
				icon = R.drawable.ic_clock,
				modifier = Modifier.weight(1f)
			)
		}

		Spacer(modifier = Modifier.height(6.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
		) {
			DataItemView(
				text = betaCount.toString(),
				contentDescription = when (bucketType) {
					BucketType.TODO.name -> "Doing count"
					BucketType.BOOK.name -> "Reading count"
					BucketType.SHOW.name -> "Watching count"
					BucketType.LINK.name -> "Doing count"
					else -> "ERROR"
				},
				icon = when (bucketType) {
					BucketType.TODO.name -> R.drawable.ic_todo
					BucketType.BOOK.name -> R.drawable.ic_book
					BucketType.SHOW.name -> R.drawable.ic_show
					BucketType.LINK.name -> R.drawable.ic_link
					else -> R.drawable.ic_warning
				},
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(4.dp))
			DataItemView(
				text = gammaCount.toString(),
				contentDescription = when (bucketType) {
					BucketType.TODO.name -> "Done count"
					BucketType.BOOK.name -> "Read count"
					BucketType.SHOW.name -> "Watched count"
					BucketType.LINK.name -> "Done count"
					else -> "ERROR"
				},
				icon = R.drawable.ic_check,
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun DataItemView(
	modifier: Modifier = Modifier,
	text: String,
	icon: Int,
	contentDescription: String,
) {
	Card(
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.outlinedCardColors(
			containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		modifier = modifier.height(40.dp)
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxSize(),
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = contentDescription
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
			)
		}
	}
}
