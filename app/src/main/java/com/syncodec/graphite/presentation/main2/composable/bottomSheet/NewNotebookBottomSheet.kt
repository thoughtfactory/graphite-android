package com.syncodec.graphite.presentation.main2.composable.bottomSheet

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun NewNotebookBottomSheet(
    bottomSheet2State: GenericBottomSheet2State = GenericBottomSheet2State.initialize(),
    onCreateNewNotebook: (ChapterBox) -> Unit
) {

    val textField2Controller = rememberTextField2Controller(initialFocus = true)
    val textField2Controller2 = rememberTextField2Controller()

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            modifier = Modifier,
            title = stringResource(R.string.new_notebook)
        ) {

            GenericTextField2(
                controller = textField2Controller,
                label = stringResource(R.string.notebook_title),
                placeholder = stringResource(R.string.notebook_title_placeholder),
                errorMessage = "error1",
                keyboardOptions = GenericTextField2Defaults.getTextNextKeyboardOptionsDefault()
            )
            GenericTextField2(
                controller = textField2Controller2,
                label = stringResource(R.string.description_optional),
                placeholder = stringResource(R.string.notebook_description_placeholder)
            )

            Button(onClick = {
                val validatedTitle = textField2Controller.validate {
                    it.length>3
                }

                val chapterBox = ChapterBox(
                    title = validatedTitle
                )

                onCreateNewNotebook(chapterBox)

            }) { }
        }
    }
}
