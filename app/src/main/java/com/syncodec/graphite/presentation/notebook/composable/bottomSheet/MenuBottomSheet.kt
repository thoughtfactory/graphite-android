package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2ListButtonDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2Defaults
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.row.SameHeightRowGrid
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MenuBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	isChapterDefault: Boolean = true,
	onClickAttachments: () -> Unit = {},
	onClickAtlas: () -> Unit = {},
	onClickCalendar: () -> Unit = {},
	onClickSetAsDefault: () -> Unit = {},
	onClickEdit: () -> Unit = {},
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
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_gallery,
					text = stringResource(id = R.string.attachments),
					contentDescription = stringResource(id = R.string.attachments),
					onClick = onClickAtlas,
				)
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_atlas,
					text = stringResource(id = R.string.atlas),
					contentDescription = stringResource(id = R.string.atlas),
					onClick = onClickAtlas,
				)
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_calendar,
					text = stringResource(id = R.string.calendar),
					contentDescription = stringResource(id = R.string.calendar),
					onClick = onClickCalendar,
				)
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_sparkles,
					text = stringResource(id = R.string.set_as_default),
					contentDescription = stringResource(id = R.string.set_as_default),
					checked = isChapterDefault,
					onClick = onClickSetAsDefault
				)
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_pen,
					text = stringResource(id = R.string.edit),
					contentDescription = stringResource(id = R.string.edit),
					onClick = onClickEdit,
				)
				GenericBottomSheetButton2(
					icon = R.drawable.ic_fa_delete_duotone,
					text = stringResource(id = R.string.delete),
					contentDescription = stringResource(id = R.string.delete),
					colors = GenericBottomSheetButton2Defaults.errorButtonColors(),
					onClick = onClickDelete,
				)
			}
		}
	}
}
