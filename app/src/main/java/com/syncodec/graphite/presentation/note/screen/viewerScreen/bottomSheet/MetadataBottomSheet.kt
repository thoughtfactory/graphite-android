package com.syncodec.graphite.presentation.note.screen.viewerScreen.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyValueCard
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.github.esentsov.PackagePrivate
import io.realm.kotlin.types.RealmUUID


@PackagePrivate
@Preview
@Composable
fun MetadataBottomSheet(
	noteId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
) {
	GenericBottomSheet(
		title = "Metadata",
		icon = R.drawable.ic_info,
	) {
		BottomSheetKeyValueCard(
			key = "ID",
			value = noteId?.toString() ?: "Unsaved",
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Created",
			value = createdTimestamp?.timeStampToPrettyFull() ?: "Unsaved",
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Modified",
			value = modifiedTimestamp?.timeStampToPrettyFull() ?: "Unsaved",
		)
	}
}
