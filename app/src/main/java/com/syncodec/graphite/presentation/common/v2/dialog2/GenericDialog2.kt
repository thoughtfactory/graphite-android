package com.syncodec.graphite.presentation.common.v2.dialog2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialogActionButton.RedButton
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialogActionButton.SecondaryButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object GenericDialog2 {
    @OptIn(ExperimentalMaterial3Api::class)
    class State<T> {

        private val _isDialogVisibleFlow: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
        val isDialogVisibleFlow: StateFlow<Boolean> = this._isDialogVisibleFlow.asStateFlow()

        private val _dataMutableFlow: MutableStateFlow<T?> = MutableStateFlow(value = null)
        val dataFlow = _dataMutableFlow.asStateFlow()

        fun openDialog() {
            this._isDialogVisibleFlow.tryEmit(value = true)
        }

        fun openDialog(data: T?) {
            this._isDialogVisibleFlow.tryEmit(value = true)
            this._dataMutableFlow.tryEmit(value = data)
        }

        fun closeDialog() {
            this._isDialogVisibleFlow.tryEmit(value = false)
        }

        fun closeDialog(data: T?) {
            this._isDialogVisibleFlow.tryEmit(value = false)
            this._dataMutableFlow.tryEmit(value = data)
        }

        companion object {
            @Composable
            fun rememberDialogState(): State<Nothing> = remember { State() }

            @Composable
            fun <T> rememberDialogStateT(): State<T> = remember { State() }
        }
    }

    @Composable
    fun <T> Composable(
        state: State<T> = State.rememberDialogStateT(),
        title: String,
        secondaryButton: (@Composable RowScope.() -> Unit)? = null,
        primaryButton: (@Composable RowScope.() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val isDialogVisible by state.isDialogVisibleFlow.collectAsState()

        if (isDialogVisible) Dialog(
            onDismissRequest = { state.closeDialog() },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    content()
                    when {
                        primaryButton != null && secondaryButton == null -> Row { primaryButton() }
                        primaryButton != null && secondaryButton != null -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                secondaryButton()
                                Spacer(modifier = Modifier.width(width = 6.dp))
                                primaryButton()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun <T> Composable(
        state: State<T> = State.rememberDialogStateT(),
        icon: Int? = null,
        iconTint: Color = MaterialTheme.colorScheme.onBackground,
        title: String = "Title",
        contentText: String? = null,
        dualActionButton: (@Composable RowScope.() -> Unit)? = null,
        thirdActionButton: (@Composable ColumnScope.() -> Unit)? = null,
        content: @Composable () -> Unit = {}
    ) {

        val isDialogVisible by state.isDialogVisibleFlow.collectAsState()

        BackHandler(enabled = isDialogVisible) { state.closeDialog() }

        if (isDialogVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
            ) {
                Dialog(
                    onDismissRequest = { state.closeDialog() },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false,
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.77f)
                            .wrapContentHeight()
                            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.extraLarge)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 24.dp)
                        ) {
                            icon?.let {
                                Icon(
                                    painter = painterResource(id = it),
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .requiredSize(size = 32.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(height = 16.dp))
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(height = 16.dp))

                            contentText?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            content()

                            Spacer(modifier = Modifier.height(height = 24.dp))

                            thirdActionButton?.let {
                                it()
                                Spacer(modifier = Modifier.height(height = 2.dp))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                content = { dualActionButton?.invoke(this) }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DeleteDialog(
        state: State<List<Long>> = State.rememberDialogStateT(),
        onClickDelete: (List<Long>) -> Unit,
        onClickCancel: () -> Unit
    ) {
        val selectedItemList by state.dataFlow.collectAsState()
        Composable(
            state = state,
            icon = R.drawable.ic_fa_delete,
            title = stringResource(id = R.string.delete),
            contentText = stringResource(id = R.string.dialog_delete_selected_items),
            dualActionButton = {
                SecondaryButton(text = stringResource(id = R.string.cancel), onClick = onClickCancel)
                RedButton(text = stringResource(id = R.string.delete)) { onClickDelete(selectedItemList ?: return@RedButton) }
            },
        )
    }
}
