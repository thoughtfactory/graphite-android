package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyValueCard
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun MenuBottomSheet(
	id : RealmUUID? = null,
	description : String? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	onClickEdit : () -> Unit = {},
	onClickShare : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	closeSheet : () -> Unit = {},
) {
	GenericBottomSheet(
		title = "Menu",
		icon = R.drawable.ic_menu,
	) {

		BottomSheetButtonGrid(
			buttonList = listOf(
				{ BottomSheetButton(title = "Edit", icon = R.drawable.ic_pencil, onClick = onClickEdit)  },
				{ BottomSheetButton(title = "Share All", icon = R.drawable.ic_share, onClick = onClickShare) },
				{
					BottomSheetButton(
						title = "Delete",
						icon = R.drawable.ic_delete,
						containerColor = MaterialTheme.colorScheme.errorContainer,
						contentColor = MaterialTheme.colorScheme.onErrorContainer,
						onClick = onClickDelete,
					)
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
			value = id?.toString() ?: "",
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Description",
			value = description,
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Created On",
			value = createdTimestamp?.timeStampToPrettyFull(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Modified On",
			value = modifiedTimestamp?.timeStampToPrettyFull(),
		)
	}
}
