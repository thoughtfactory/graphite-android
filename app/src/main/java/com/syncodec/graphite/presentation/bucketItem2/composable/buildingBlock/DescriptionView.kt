package com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun DescriptionView(
    description: String? = null,
    isEditing: Boolean = false,
    onClick: () -> Unit = {}
) {

    description ?: return

    var isExpanded by remember { mutableStateOf(value = false) }
    val descriptionTextFieldState = remember { TextFieldState(initialText = description) }

    SelectableContainer2(
        enabled = true,
        selected = false,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = { if (isEditing) onClick() else isExpanded = !isExpanded },
        onLongClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(height = 6.dp))

            AnimatedContent(
                targetState = Pair(isEditing, isExpanded),
                transitionSpec = { AnimationDefaults.Fade }
            ) { (isEditing1, isExpanded1) ->
                if (isEditing) BasicTextField(
                    state = descriptionTextFieldState,
                    textStyle = MaterialTheme.typography.bodyMedium.merge(color = SelectableContainer2Defaults.defaultColors().onContainerColor),
                ) else Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f),
                    maxLines = if (isExpanded1) Int.MAX_VALUE else 6,
                    overflow = TextOverflow.Ellipsis
                )
//                when {
//                    isEditing1 -> BasicTextField(
//                        state = descriptionTextFieldState,
//                        textStyle = MaterialTheme.typography.bodyMedium.merge(color = SelectableContainer2Defaults.defaultColors().onContainerColor),
//                    )
//
//                }
            }

//            AnimatedContent(
//                targetState = isEditing || isExpanded,
//                transitionSpec = { AnimationDefaults.Fade }
//            ) {
////                BasicTextField(
////                    state = TextFieldState(initialText = description),
//////                    enabled = it,
////                    readOnly = it,
////                    textStyle = MaterialTheme.typography.bodyMedium.merge(color = SelectableContainer2Defaults.defaultColors().onContainerColor),
////                )
//                BasicTextField(
//                    state = descriptionTextFieldState,
//                    textStyle = MaterialTheme.typography.bodyMedium.merge(color = SelectableContainer2Defaults.defaultColors().onContainerColor),
//                    readOnly = !it,
//                    enabled = it,
//                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = if (it) Int.MAX_VALUE else 6),
//                    modifier = Modifier.clickable(enabled = true) { isExpanded = !isExpanded }
////                    maxLines = if (it) Int.MAX_VALUE else 6,
//                )
////                BasicTextField(
////                    value = description,
////                    onValueChange = {},
////                    singleLine = false,
////                    textStyle = MaterialTheme.typography.bodyMedium.merge(color = SelectableContainer2Defaults.defaultColors().onContainerColor),
//////                    color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f),
////                    maxLines = if (it) Int.MAX_VALUE else 6,
//////                    overflow = TextOverflow.Ellipsis,
////                    readOnly = !it,
////                    enabled = !it,
////                    onTextLayout = {
////                        it.lineCount
////                    },
////
////                    decorationBox = { it() },
////                )
////                Text(
////                    text = description,
////                    style = MaterialTheme.typography.bodyMedium,
////                    color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f),
////                    maxLines = if (it) Int.MAX_VALUE else 6,
////                    overflow = TextOverflow.Ellipsis
////                )
//            }
        }
    }
}
