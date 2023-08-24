package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ViewerMetadataBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	noteId: RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	parentId: RealmUUID? = null,
) {
	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Metadata",
		) {
			GenericBottomSheetInfo2(
				key = "ID",
				value = noteId?.toString() ?: "Not saved",
			)
			GenericBottomSheetInfo2(
				key = "Created on",
				value = createdTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo2(
				key = "Modified on",
				value = modifiedTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo2(
				key = "Parent ID",
				value = parentId?.toString() ?: "null",
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorMetadataBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	noteId: RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	parentId: RealmUUID? = null,
) {
	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Metadata",
		) {
			GenericBottomSheetInfo2(
				key = "ID",
				value = noteId?.toString() ?: "Not saved",
			)
			GenericBottomSheetInfo2(
				key = "Created on",
				value = createdTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo2(
				key = "Modified on",
				value = modifiedTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo2(
				key = "Parent ID",
				value = parentId?.toString() ?: "null",
			)
		}
	}
}
