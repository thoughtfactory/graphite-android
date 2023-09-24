package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.sheets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MetadataBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	id: RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	extraContent : @Composable ColumnScope.() -> Unit = {}
) {
	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.metadata),
		) {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.id),
				value = id?.toString() ?: stringResource(id = R.string.not_saved),
			)
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.created_on),
				value = createdTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.modified_on),
				value = modifiedTimestamp?.timeStampToPrettyFull() ?: "",
			)
			extraContent()
		}
	}
}
