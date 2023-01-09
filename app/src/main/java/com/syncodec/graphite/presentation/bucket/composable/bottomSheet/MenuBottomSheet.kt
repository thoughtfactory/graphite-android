package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnShare
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyValueCard
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.timeStampToPrettyFull


@Preview
@Composable
fun MenuBottomSheet() {

	val isVaultOpened = LocalVaultIsOpened.current

	val bucketObject = LocalCompositionBucketObject.current

	val openDialog = LocalCompositionOpenDialog.current
	val closeSheet = LocalCompositionCloseBottomSheet.current

	val onShare = LocalCompositionOnShare.current

	GenericBottomSheet(
		title = "Menu",
		icon = R.drawable.ic_menu,
	) {

		BottomSheetButtonGrid(
			buttonList = listOf(
				{ BottomSheetButton(title = "Edit", icon = R.drawable.ic_pencil) { openDialog(DialogType.EDIT) } },
				{ BottomSheetButton(title = "Share", icon = R.drawable.ic_share) { onShare(true); closeSheet() } },
				{
					BottomSheetButton(
						title = "Delete",
						icon = R.drawable.ic_delete,
						containerColor = Color.Companion.DeleteContainer,
						contentColor = Color.Companion.DeleteContent
					) { closeSheet(); openDialog(DialogType.DELETE) }
				},
			)
		)

		Spacer(modifier = Modifier.height(6.dp))

		Spacer(
			modifier = Modifier
				.fillMaxWidth(0.71f)
				.height(1.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
		)

		Spacer(modifier = Modifier.height(6.dp))

		BottomSheetKeyValueCard(
			key = "ID",
			value = bucketObject?.id?.toString() ?: "",
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Description",
			value = bucketObject?.description,
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Created On",
			value = bucketObject?.createdTimestamp?.timeStampToPrettyFull(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Modified On",
			value = bucketObject?.modifiedTimestamp?.timeStampToPrettyFull(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		DataView(
			allCount = bucketObject?.bucketItemList?.count { if (it.isLocked) isVaultOpened else true } ?: 0,
			alphaCount = bucketObject?.bucketItemList?.count { (it.state == BucketItemState.ALPHA.name) && if (it.isLocked) isVaultOpened else true } ?: 0,
			betaCount = bucketObject?.bucketItemList?.count { (it.state == BucketItemState.BETA.name) && if (it.isLocked) isVaultOpened else true } ?: 0,
			gammaCount = bucketObject?.bucketItemList?.count { (it.state == BucketItemState.GAMMA.name) && if (it.isLocked) isVaultOpened else true } ?: 0,
			bucketType = bucketObject?.bucketType.let {
				try {
					BucketType.valueOf(it ?: BucketType.UNKNOWN.name)
				} catch (e : Exception) {
					BucketType.UNKNOWN
				}
			}
		)
	}
}

@Composable
private fun ColumnScope.DataView(
	allCount : Int,
	alphaCount : Int,
	betaCount : Int,
	gammaCount : Int,
	bucketType : BucketType
) {
	this.apply {
		Row(
			modifier = Modifier.fillMaxWidth()
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
					BucketType.TODO -> "Todo count"
					BucketType.BOOK -> "To read count"
					BucketType.SHOW -> "To watch count"
					BucketType.LINK -> "Todo count"
					else -> "ERROR"
				},
				icon = R.drawable.ic_clock,
				modifier = Modifier.weight(1f)
			)
		}

		Spacer(modifier = Modifier.height(4.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			DataItemView(
				text = betaCount.toString(),
				contentDescription = when (bucketType) {
					BucketType.TODO -> "Doing count"
					BucketType.BOOK -> "Reading count"
					BucketType.SHOW -> "Watching count"
					BucketType.LINK -> "Doing count"
					else -> "ERROR"
				},
				icon = when (bucketType) {
					BucketType.TODO -> R.drawable.ic_todo
					BucketType.BOOK -> R.drawable.ic_book
					BucketType.SHOW -> R.drawable.ic_show
					BucketType.LINK -> R.drawable.ic_link
					else -> R.drawable.ic_warning
				},
				modifier = Modifier.weight(1f)
			)

			Spacer(modifier = Modifier.width(4.dp))

			DataItemView(
				text = gammaCount.toString(),
				contentDescription = when (bucketType) {
					BucketType.TODO -> "Done count"
					BucketType.BOOK -> "Read count"
					BucketType.SHOW -> "Watched count"
					BucketType.LINK -> "Done count"
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
	modifier : Modifier = Modifier,
	text : String,
	icon : Int,
	contentDescription : String,
) {
	Card(
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			contentColor = MaterialTheme.colorScheme.onSurface
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
				contentDescription = contentDescription,
				modifier = Modifier.requiredSize(20.dp)
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
