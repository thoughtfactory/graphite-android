package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.DeleteButton
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.ShareButton
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.EditButton
import com.syncodec.graphite.presentation.common.row.SameHeightRowGrid


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MenuBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	onClickShareAll : () -> Unit = {},
	onClickEdit : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.menu),
		) {
			SameHeightRowGrid {
				ShareButton(onClick = onClickShareAll)
				EditButton(onClick = onClickEdit)
				DeleteButton(onClick = onClickDelete)
			}
		}
	}
}
