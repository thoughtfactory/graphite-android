package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetInfo
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetSkeleton2
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
			title = stringResource(id = R.string.metadata),
		) {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.id),
				value = noteId?.toString() ?: stringResource(id = R.string.not_saved),
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.created_on),
				value = createdTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.modified_on),
				value = modifiedTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.parent_id),
				value = parentId?.toString() ?: stringResource(id = R.string.unknown),
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
			title = stringResource(id = R.string.metadata),
		) {
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.id),
				value = noteId?.toString() ?: stringResource(id = R.string.not_saved),
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.created_on),
				value = createdTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.modified_on),
				value = modifiedTimestamp?.timeStampToPrettyFull() ?: "",
			)
			GenericBottomSheetInfo(
				key = stringResource(id = R.string.parent_id),
				value = parentId?.toString() ?: stringResource(id = R.string.unknown),
			)
		}
	}
}
