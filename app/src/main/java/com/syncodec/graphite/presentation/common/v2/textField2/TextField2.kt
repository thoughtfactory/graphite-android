package com.syncodec.graphite.presentation.common.v2.textField2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.ui.ANIMATION_TIME
import com.syncodec.graphite.utils.onlyIfComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random


object GenericTextField2 {

    enum class Mode {
        Edit,
        ReadOnly,
        Clickable
    }

    object Colors {
        @Composable
        fun getDefaultColors(): TextFieldColors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
            disabledTextColor = MaterialTheme.colorScheme.onBackground,
            errorTextColor = MaterialTheme.colorScheme.onErrorContainer,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
            cursorColor = MaterialTheme.colorScheme.onBackground,
            errorCursorColor = MaterialTheme.colorScheme.onErrorContainer,
//        selectionColors =,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
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
    }

    object Options {
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

        fun getTextSearchKeyboardOptionsDefault(): KeyboardOptions = getTextKeyboardOptionsDefault(imeAction = ImeAction.Search)
    }

    object Actions {
        fun getKeyboardActions(onAction: (KeyboardActionScope.() -> Unit)?): KeyboardActions = KeyboardActions(
            onDone = onAction,
            onGo = onAction,
            onNext = onAction,
            onPrevious = onAction,
            onSend = onAction,
            onSearch = onAction,
        )

        fun getKeyboardActionsDefault() = getKeyboardActions(onAction = null)
    }

    data class TextField2Controller(
        var textFlow: MutableStateFlow<String>,
        val initialFocus: Boolean
    ) {

        val key = Random.nextInt()

        constructor(initialText: String, initialFocus: Boolean) : this(textFlow = MutableStateFlow(value = initialText), initialFocus)

        val focusRequester = FocusRequester()

        var isErrorFlow: MutableStateFlow<Boolean> = MutableStateFlow(value = false)

        fun onValueChange(value: String) {
            this.textFlow.tryEmit(value)
        }

        fun validate(howToValidate: (String) -> Boolean): ValidationResult {
            val text = textFlow.value
            val isValidated = howToValidate(text)
            this.isErrorFlow.tryEmit(!isValidated)
            return ValidationResult(isValidated = isValidated, text = text)
        }

        fun reset() {
            this.textFlow.tryEmit(value = "")
            this.isErrorFlow.tryEmit(value = false)
        }

        fun requestFocus() = focusRequester.requestFocus()

        fun removeFocus() = focusRequester.freeFocus()

        data class ValidationResult(val isValidated: Boolean, val text: String)

        companion object {
            fun initialize(initialText: String, initialFocus: Boolean): TextField2Controller = TextField2Controller(initialText = initialText, initialFocus)
        }
    }

    @Composable
    fun rememberTextField2Controller(initialText: String = "", initialFocus: Boolean = false): TextField2Controller {
        return remember { TextField2Controller.initialize(initialText = initialText, initialFocus = initialFocus) }
    }

    @Composable
    fun Composable(
        modifier: Modifier = Modifier,
        controller: TextField2Controller = rememberTextField2Controller(initialFocus = false),
        textStyle: TextStyle = LocalTextStyle.current,
        label: String? = null,
        placeholder: String? = null,
        errorMessage: String? = null,
        mode: Mode = Mode.Edit,
        minLines: Int = 1,
        maxLines: Int = 1,
        singleLine: Boolean = true,
        prefixIcon: @Composable (() -> Unit)? = null,
        suffixIcon: @Composable (RowScope.() -> Unit)? = { GraIconButton.ClearTextButton { controller.onValueChange("") } },
        keyboardOptions: KeyboardOptions = Options.getTextNextKeyboardOptionsDefault(),
        keyboardActions: KeyboardActions = Actions.getKeyboardActionsDefault(),
        colors: TextFieldColors = Colors.getDefaultColors(),
        onClick: () -> Unit = {},
    ) {

        val text by controller.textFlow.collectAsState()
        val isError by controller.isErrorFlow.collectAsState()

        Column {
            TextField(
                value = text,
                shape = MaterialTheme.shapes.medium,
                onValueChange = controller::onValueChange,
                textStyle = textStyle,
                label = label?.let { string -> { Text(string) } },
                placeholder = placeholder?.let { string -> { Text(string) } },
                leadingIcon = prefixIcon?.let { function -> { function.invoke() } },
                trailingIcon = { if (suffixIcon == null) null else Row { suffixIcon(); Spacer(modifier = Modifier.width(width = 6.dp)) } },
                readOnly = mode != Mode.Edit,
                enabled = mode != Mode.Clickable,
                minLines = minLines,
                maxLines = maxLines,
                singleLine = singleLine,
                colors = colors,
                isError = isError,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = controller.focusRequester)
                    .clip(shape = MaterialTheme.shapes.medium)
                    .onlyIfComposable(modifier = { clickable(onClick = onClick) }) { true }
                    .border(border = BorderStroke(width = 1.dp, color = colors.unfocusedTextColor), shape = MaterialTheme.shapes.medium)
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

        LaunchedEffect(key1 = Unit) {
            if (controller.initialFocus) {
                delay(timeMillis = 300)               //  300 millisecond
                controller.requestFocus()
            }
        }
    }

    @Composable
    fun BottomSheetTextField(
        controller: TextField2Controller,
        label: String?,
        placeholder: String? = null,
        errorMessage: String? = null,
        minLines: Int = 1,
        maxLines: Int = 1,
        singleLine: Boolean = true,
        prefixIcon: @Composable (() -> Unit)? = null,
        suffixIcon: @Composable (RowScope.() -> Unit)? = { GraIconButton.ClearTextButton { controller.onValueChange("") } },
        keyboardOptions: KeyboardOptions = Options.getTextNextKeyboardOptionsDefault(),
        keyboardActions: KeyboardActions = Actions.getKeyboardActionsDefault()
    ) {
        Composable(
            controller = controller,
            label = label,
            placeholder = placeholder,
            errorMessage = errorMessage,
            minLines = minLines,
            maxLines = maxLines,
            singleLine = singleLine,
            prefixIcon = prefixIcon,
            suffixIcon = suffixIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}
