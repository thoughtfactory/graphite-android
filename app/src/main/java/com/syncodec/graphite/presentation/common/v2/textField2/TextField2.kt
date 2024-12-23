package com.syncodec.graphite.presentation.common.v2.textField2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.common.button.GraIconButton
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.ui.ANIMATION_TIME


@Composable
fun GenericTextField2(
    controller: TextField2Controller = rememberTextField2Controller(initialFocus = false),
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = { GraIconButton.ClearTextButton { controller.onValueChange("") } },
    keyboardOptions: KeyboardOptions = GenericTextField2Defaults.getTextNextKeyboardOptionsDefault(),
    colors: TextFieldColors = GenericTextField2Defaults.getDefaultColors(),
) {

    val text by controller.textFlow.collectAsState()
    val isError by controller.isErrorFlow.collectAsState()

    Column {
        OutlinedTextField(
            value = text,
            shape = MaterialTheme.shapes.medium,
            onValueChange = controller::onValueChange,
            label = label?.let { string -> { Text(string) } },
            placeholder = placeholder?.let { string -> { Text(string) } },
            trailingIcon = trailingIcon?.let { function -> { function.invoke() } },
            maxLines = maxLines,
            singleLine = singleLine,
            colors = colors,
            isError = isError,
            keyboardOptions = keyboardOptions,
//        keyboardActions = KeyboardActions(onDone = {}),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(controller.focusRequester)
        )

        errorMessage?.let { errorMessageText ->
            AnimatedVisibility(
                visible = isError,
                enter = expandVertically(tween(ANIMATION_TIME)),
                exit = shrinkVertically(tween(ANIMATION_TIME))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessageText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    LaunchedEffect(controller.initialFocus) { controller.requestFocus() }

}

object GenericTextField2Defaults {

    @Composable
    fun getDefaultColors(): TextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
//        disabledTextColor =,
        errorTextColor = MaterialTheme.colorScheme.onErrorContainer,
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
        errorContainerColor = MaterialTheme.colorScheme.errorContainer,
        cursorColor = MaterialTheme.colorScheme.onBackground,
        errorCursorColor = MaterialTheme.colorScheme.onErrorContainer,
//        selectionColors =,
//        focusedIndicatorColor =,
//        unfocusedIndicatorColor =,
//        disabledIndicatorColor =,
//        errorIndicatorColor =,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
//        disabledLeadingIconColor =,
        errorLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
//        disabledTrailingIconColor =,
        errorTrailingIconColor = MaterialTheme.colorScheme.onErrorContainer,
        focusedLabelColor = MaterialTheme.colorScheme.onBackground,
        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
//        disabledLabelColor = ,
        errorLabelColor = MaterialTheme.colorScheme.onErrorContainer,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(0.71f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(0.42f),
//        disabledPlaceholderColor =,
        errorPlaceholderColor = MaterialTheme.colorScheme.onErrorContainer,
//        focusedSupportingTextColor =,
//        unfocusedSupportingTextColor =,
//        disabledSupportingTextColor =,
//        errorSupportingTextColor =,
//        focusedPrefixColor =,
//        unfocusedPrefixColor =,
//        disabledPrefixColor =,
//        errorPrefixColor =,
//        focusedSuffixColor =,
//        unfocusedSuffixColor =,
//        disabledSuffixColor =,
//        errorSuffixColor =,
    )

    fun getTextKeyboardOptionsDefault(imeAction: ImeAction): KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        autoCorrectEnabled = true,
        keyboardType = KeyboardType.Text,
        imeAction = imeAction,
        platformImeOptions = null,
        showKeyboardOnFocus = true,
        hintLocales = null
    )

    fun getTextNextKeyboardOptionsDefault(): KeyboardOptions = getTextKeyboardOptionsDefault(imeAction = ImeAction.Next)
}


data class TextField2Controller(
    var textFlow: MutableStateFlow<String>,
    val initialFocus: Boolean
) {

    constructor(initialText: String, initialFocus: Boolean) : this(textFlow = MutableStateFlow(initialText), initialFocus)

    val focusRequester = FocusRequester()

    var isErrorFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun onValueChange(value: String) {
        textFlow.tryEmit(value)
    }

    fun validate(howToValidate: (String) -> Boolean): String {
        val isValidated = howToValidate(textFlow.value)
        isErrorFlow.tryEmit(!isValidated)
        return textFlow.value
    }

    fun requestFocus() = focusRequester.requestFocus()

    companion object {
        fun initialize(initialText: String, initialFocus: Boolean): TextField2Controller = TextField2Controller(initialText = initialText, initialFocus)
    }
}

@Composable
fun rememberTextField2Controller(initialText: String = "", initialFocus: Boolean = false): TextField2Controller {
    return remember { TextField2Controller.initialize(initialText = initialText, initialFocus = initialFocus) }
}
