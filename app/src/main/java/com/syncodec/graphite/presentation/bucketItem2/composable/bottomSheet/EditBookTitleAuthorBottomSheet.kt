package com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.presentation.common.button.GraIconButton
import com.syncodec.graphite.presentation.common.v2.SurfaceVariantButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.TextField2Controller
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import dev.chrisbanes.haze.HazeState
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Preview
@Composable
fun EditBookTitleAuthorBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<BucketItemBook?> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    outerHazeState: HazeState = remember { HazeState() },
    onUpdateBucketItemData: (BucketItemBook) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isBottomSheetVisible by bottomSheet2State.isBottomSheetVisibleFlow.collectAsState()
    val bucketItemBook by bottomSheet2State.dataFlow.collectAsState()

    val titleTextFieldController = rememberTextField2Controller(initialFocus = false)
    var authorTextFieldControllerList: List<TextField2Controller> by remember { mutableStateOf(value = listOf()) }

    LaunchedEffect(key1 = isBottomSheetVisible, key2 = bucketItemBook) {
        titleTextFieldController.onValueChange(value = bucketItemBook?.bookTitle() ?: "")
        authorTextFieldControllerList = bucketItemBook?.allBookAuthor()?.map { TextField2Controller.initialize(initialText = it, initialFocus = false) } ?: listOf()
    }

    fun update() {
        val titleValidationResult = titleTextFieldController.validate { it.isNotBlank() }
        if (!titleValidationResult.isValidated) Toast.makeText(context, context.getString(R.string.toast_no_book_title), Toast.LENGTH_SHORT).show()
        else {
            val authorList = authorTextFieldControllerList
                .map { it.validate { it.isNotBlank() } }
                .filter { it.isValidated }
                .map { it.text }
            val updatedBucketItemBook = bucketItemBook?.toCustom()?.copy(
                title = titleValidationResult.text,
                authorList = authorList
            ) ?: BucketItemBook.Custom(
                title = titleValidationResult.text,
                authorList = authorList
            )
            bottomSheet2State.hideSheet(scope = scope) { onUpdateBucketItemData(updatedBucketItemBook) }
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
        outerHazeState = outerHazeState
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.edit),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2(
                controller = titleTextFieldController,
                label = stringResource(id = R.string.book_title),
                errorMessage = stringResource(id = R.string.title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            authorTextFieldControllerList.forEachIndexed { index, textFieldController ->
                GenericTextField2(
                    controller = textFieldController,
                    label = stringResource(id = R.string.author) + " ${index + 1}",
                    keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault(),
                    suffixIcon = { GraIconButton.ClearTextButton { authorTextFieldControllerList = authorTextFieldControllerList.toMutableList().apply { removeAt(index) } } }
                )
                Spacer(modifier = Modifier.height(height = 4.dp))
            }

            SurfaceVariantButton(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (authorTextFieldControllerList.isEmpty() || authorTextFieldControllerList.lastOrNull()?.textFlow?.value?.isNotBlank() == true) authorTextFieldControllerList = authorTextFieldControllerList + TextField2Controller.initialize(initialText = "", initialFocus = true) },
                content = { Text(text = stringResource(id = R.string.add_author)) }
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::update,
                content = { Text(text = stringResource(id = R.string.update)) }
            )
        }
    }
}
