package com.syncodec.graphite.presentation.note2.composable

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.syncodec.graphite.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.base.SCALE_AND_FADE_ENTER_FAST
import com.syncodec.graphite.presentation.base.SCALE_AND_FADE_EXIT_FAST
import com.syncodec.graphite.presentation.common.button.WideButton
import com.syncodec.graphite.presentation.common.button.WideTextButton
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogButton
import com.syncodec.graphite.presentation.common.keyboard.keyboardAsState
import com.syncodec.graphite.presentation.common.modifier.conditional
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDatePicker(
    dateTimePickerState: DateTimePickerState
) {

    val isDatePickerVisible by dateTimePickerState.isDatePickerVisibleFlow.collectAsState()
    val isTimePickerVisible by dateTimePickerState.isTimePickerVisibleFlow.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .conditional(isDatePickerVisible) { clickable { dateTimePickerState.onClickOutside() } }
    ) {
        AnimatedVisibility(
            visible = isDatePickerVisible,
            enter = SCALE_AND_FADE_ENTER_FAST,
            exit = SCALE_AND_FADE_EXIT_FAST,
            label = "datePicker_animation"
        ) {
            Surface(
                modifier = Modifier.padding(32.dp),
                color = MaterialTheme.colorScheme.surfaceDim,
                shape = MaterialTheme.shapes.extraLarge,
                onClick = {},
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    DatePicker(
                        state = dateTimePickerState.datePickerState,
                        showModeToggle = false,
                        modifier = Modifier
                    )

                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        WideTextButton(text = stringResource(R.string.cancel)) {}
                        Spacer(modifier = Modifier.width(12.dp))
                        WideButton(text = stringResource(R.string.ok)) {}
                    }
                }
            }
        }
    }
}

data class DateTimePickerState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val datePickerState: DatePickerState,
    val timePickerState: TimePickerState,
) {

    private val _isDatePickerVisibleFlow = MutableStateFlow(false)
    val isDatePickerVisibleFlow = _isDatePickerVisibleFlow.asStateFlow()

    private val _isTimePickerVisibleFlow = MutableStateFlow(false)
    val isTimePickerVisibleFlow = _isTimePickerVisibleFlow.asStateFlow()

    @OptIn(ExperimentalMaterial3Api::class)
    fun onClickDateTime(selectedDateTime: Long?) {
        datePickerState.selectedDateMillis = selectedDateTime ?: Instant.now().toEpochMilli()
        _isDatePickerVisibleFlow.tryEmit(true)
    }

    fun onClickOutside() {
        _isDatePickerVisibleFlow.tryEmit(false)
        _isTimePickerVisibleFlow.tryEmit(false)
    }

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun initialize() = DateTimePickerState(datePickerState = rememberDatePickerState(), timePickerState = rememberTimePickerState())
    }
}
