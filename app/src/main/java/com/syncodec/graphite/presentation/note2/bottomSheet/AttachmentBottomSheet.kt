package com.syncodec.graphite.presentation.note2.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetActionButton
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.component.SameHeightRowGridLayout


@Composable
fun AttachmentBottomSheet(
    bottomSheetState: GenericBottomSheet2State = GenericBottomSheet2State.initialize(),
) {

    GenericBottomSheet2(
        bottomSheetState = bottomSheetState
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.attachments),
        ) {
            SameHeightRowGridLayout {
                BottomSheetActionButton(
                    icon = R.drawable.ic_fa_camera,
                    text = stringResource(id = R.string.camera),
                    contentDescription = stringResource(id = R.string.camera),
                ) {
//                    createTempAttachmentFileToExpose(context = context, name = "${RealmUUID.random()}.jpg").first.let { uri ->
//                        photoUri = uri
//                        takePicture.launch(uri)
//                    }
                }
                BottomSheetActionButton(
                    icon = R.drawable.ic_fa_gallery,
                    text = stringResource(id = R.string.gallery),
                    contentDescription = stringResource(id = R.string.gallery),
                ) {
//                    mediaPickerRequest.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
                BottomSheetActionButton(
                    icon = R.drawable.ic_fa_new_file,
                    text = stringResource(id = R.string.file),
                    contentDescription = stringResource(id = R.string.file),
                ) {
//                    filePickerRequest.launch(arrayOf("*/*"))
                }
            }
        }
    }
}
