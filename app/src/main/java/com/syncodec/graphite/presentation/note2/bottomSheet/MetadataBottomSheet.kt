package com.syncodec.graphite.presentation.note2.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheetInfo
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheetSkeleton2
import com.syncodec.graphite.utils.timeStampToPrettyFull
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerMetadataBottomSheet(
    bottomSheetState: GenericBottomSheet2State = GenericBottomSheet2State.initialize(),
    noteObjectFlow: StateFlow<NoteObject?>,
) {

    val noteObject by noteObjectFlow.collectAsState()

    GenericBottomSheet2(
        bottomSheetState = bottomSheetState
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.metadata),
        ) {
            GenericBottomSheetInfo(
                key = stringResource(id = R.string.id),
                value = noteObject?.id?.toString() ?: stringResource(id = R.string.unknown),
            )
            GenericBottomSheetInfo(
                key = stringResource(id = R.string.created_on),
                value = noteObject?.createdTimestamp?.timeStampToPrettyFull() ?: stringResource(id = R.string.unknown),
            )
            GenericBottomSheetInfo(
                key = stringResource(id = R.string.modified_on),
                value = noteObject?.modifiedTimestamp?.timeStampToPrettyFull() ?: stringResource(id = R.string.unknown),
            )
            GenericBottomSheetInfo(
                key = stringResource(id = R.string.parent_id),
                value = noteObject?.parentId?.toString() ?: stringResource(id = R.string.unknown),
            )
        }
    }

}
