package com.syncodec.graphite.presentation.main2.composable.bottomSheet

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabItem
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialog2State
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.CoverColorSelector
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.CoverImageSelector
import com.syncodec.graphite.presentation.main2.composable.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.getRandomColor
import dev.chrisbanes.haze.HazeState
import kotlinx.serialization.InternalSerializationApi


sealed class SelectedThumbnail {

    data class Color(val value: androidx.compose.ui.graphics.Color) : SelectedThumbnail()
    data class ResourceImage(val value: Int) : SelectedThumbnail()
    data class CustomImage(val uri: Uri) : SelectedThumbnail()

    @OptIn(InternalSerializationApi::class)
    fun toThumbnail(context: Context): Thumbnail {
        when (this) {
            is Color -> return Thumbnail.Color(argbValue = this.value.toArgb())
            is ResourceImage -> {
                this.value
                val bitmap = BitmapFactory.decodeResource(context.resources, this.value)
                val aspectRatio = try {
                    bitmap.width.toFloat() / bitmap.height.toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                    1f
                }
                val previewThumbnail = ThumbnailUtils.extractThumbnail(bitmap, (256 * aspectRatio).toInt(), 256)
                val base64String = previewThumbnail.encodeBase64()
                base64String ?: return Thumbnail.None
                return Thumbnail.Image(base64String = base64String)
            }

//            is CustomImage -> Thumbnail.Image(base64String = this.value.toArgb())
            is CustomImage -> TODO()
        }
    }

    override fun hashCode(): Int {
        return when (this) {
            is Color -> hashCode()
            is ResourceImage -> hashCode()
            is CustomImage -> hashCode()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SelectedThumbnail) return false
        return true
    }
}

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun NewNotebookBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    outerHazeState: HazeState = remember { HazeState() },
    onCreateNewNotebook: (ChapterBox) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val colorPickerDialogState = GenericDialog2State.initialize()

    val titleTextFieldController = rememberTextField2Controller(initialFocus = false)
    val descriptionTextFieldController = rememberTextField2Controller(initialFocus = false)
    var selectedThumbnail: SelectedThumbnail by remember { mutableStateOf(SelectedThumbnail.Color(value = getRandomColor())) }

    var selectedCoverType by remember { mutableIntStateOf(0) }
    val coverItemList = remember {
        listOf(
            TabItem(text = context.getString(R.string.color)) { selectedCoverType = 0 },
            TabItem(text = context.getString(R.string.image)) { selectedCoverType = 1 },
        )
    }

    fun saveNotebook() {
        val titleValidationResult = titleTextFieldController.validate { it.isNotBlank() }
        val descriptionValidationResult = descriptionTextFieldController.validate { true }

        if (titleValidationResult.isValidated && descriptionValidationResult.isValidated) {
            val chapterBox = ChapterBox(title = titleValidationResult.text, description = descriptionValidationResult.text, thumbnail = selectedThumbnail.toThumbnail(context = context))
            onCreateNewNotebook(chapterBox)

            titleTextFieldController.reset()
            descriptionTextFieldController.reset()
            bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
        outerHazeState = outerHazeState
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(R.string.new_notebook),
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            GenericTextField2(
                controller = titleTextFieldController,
                label = stringResource(R.string.notebook_title),
                placeholder = stringResource(R.string.notebook_title_placeholder),
                errorMessage = "error1",
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(8.dp))

            GenericTextField2(
                controller = descriptionTextFieldController,
                label = stringResource(R.string.description_optional),
                placeholder = stringResource(R.string.notebook_description_placeholder),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = stringResource(R.string.cover), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))

            GenericTabRow(
                tabItemList = coverItemList,
                selectedTabIndex = selectedCoverType,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = selectedCoverType,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                if (it == 0) CoverColorSelector(
                    selectedThumbnail = selectedThumbnail,
                    onSelectColor = { selectedThumbnail = it },
                    onClickCustomColorPicker = { colorPickerDialogState.openDialog() }
                )
                else CoverImageSelector(selectedThumbnail = selectedThumbnail) { selectedThumbnail = it }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::saveNotebook,
                content = { Text(text = stringResource(R.string.save)) }
            )
        }
    }

    ColorPickerDialog(
        state = colorPickerDialogState,
        selectedColor = (selectedThumbnail as? SelectedThumbnail.Color)?.value,
        onSelectColor = { color -> selectedThumbnail = SelectedThumbnail.Color(value = color) }
    )
}
