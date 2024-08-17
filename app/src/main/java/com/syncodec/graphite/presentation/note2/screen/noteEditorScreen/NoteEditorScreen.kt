package com.syncodec.graphite.presentation.note2.screen.noteEditorScreen

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.note2.bar.EditorTopBar
import com.syncodec.graphite.presentation.note2.bar.editorBar.EditorBottomBar
import com.syncodec.graphite.presentation.note2.bottomSheet.AttachmentBottomSheet
import com.syncodec.graphite.presentation.note2.bottomSheet.ViewerMetadataBottomSheet
import com.syncodec.graphite.presentation.note2.kitKat.KitKat
import com.syncodec.graphite.presentation.note2.model.NoteViewModel2
import com.syncodec.graphite.utils.LocationData
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.syncodec.graphite.presentation.common.keyboard.keyboardAsState
import com.syncodec.graphite.presentation.note2.composable.DateTimePickerState
import com.syncodec.graphite.presentation.note2.composable.NoteDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NoteEditorScreen(
    noteViewModel2: NoteViewModel2 = koinViewModel(),
    kitKat: KitKat = KitKat(LocalContext.current),
) {

    val noteObject by noteViewModel2.noteObjectFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val metadataBottomSheet2State = GenericBottomSheet2State.initialize()
    val attachmentBottomSheet2State = GenericBottomSheet2State.initialize()
    val dateTimePickerState = DateTimePickerState.initialize()


    GenericScaffold2(
        topBar = {
            EditorTopBar()
        },
        bottomBar = {
            EditorBottomBar(
                kitKatFormatFlow = kitKat.kitKatFormatFlow,
                userTimestamp = noteObject?.userTimestamp,
                locationData = LocationData.Init,
                onClickDatePicker = {
                    keyboardController?.hide()
                    dateTimePickerState.onClickDateTime(noteObject?.userTimestamp)
                },
                onClickMetadata = { metadataBottomSheet2State.openSheet() },
                noClickLocation = {},
                onClickAttachments = { attachmentBottomSheet2State.openSheet() },
                onClickTags = {},
                onKitKatAction = kitKat::onKitKatAction,
            )
        },
        dialogContent = {
            NoteDatePicker(
                dateTimePickerState = dateTimePickerState
            )
        }
    ) {
        KitKatView(
            kitKat = kitKat,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        ViewerMetadataBottomSheet(
            bottomSheetState = metadataBottomSheet2State,
            noteObjectFlow = noteViewModel2.noteObjectFlow
        )

        AttachmentBottomSheet(
            bottomSheetState = attachmentBottomSheet2State
        )

    }

}

@Preview
@Composable
private fun KitKatView(
    modifier: Modifier = Modifier,
    kitKat: KitKat = KitKat(LocalContext.current)
) {
    AndroidView(
        modifier = modifier,
        factory = { kitKat.also { if (it.parent != null) (it.parent as ViewGroup).removeView(it) } }
    )
}
