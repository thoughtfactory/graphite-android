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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun GenericDialog2(
    state: GenericDialog2State = GenericDialog2State.initialize(),
    title: String,
    contentText: String,
    primaryButton: (@Composable () -> Unit)? = null,
    secondaryButton: (@Composable () -> Unit)? = null,
) {
    val isDialogVisible by state.isDialogVisibleFlow.collectAsState()

    if (isDialogVisible) {
        Dialog(
            onDismissRequest = { state.closeDialog() },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    primaryButton != null && secondaryButton == null -> primaryButton()
                    primaryButton != null && secondaryButton != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            primaryButton()
                            Spacer(modifier = Modifier.width(4.dp))
                            secondaryButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenericDialog2(
    state: GenericDialog2State = GenericDialog2State.initialize(),
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
                            Spacer(modifier = Modifier.width(6.dp))
                            primaryButton()
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
data class GenericDialog2State(
    val isDialogVisibleFlow: MutableStateFlow<Boolean>,
) {
    fun openDialog() {
        isDialogVisibleFlow.tryEmit(true)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun closeDialog() {
        isDialogVisibleFlow.tryEmit(false)
    }

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun initialize(): GenericDialog2State = GenericDialog2State(isDialogVisibleFlow = MutableStateFlow(false))
    }
}


@Preview
@Composable
fun GenericDialog(
    showDialog: Boolean = true,
    icon: Int? = null,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    title: String = "Title",
    contentText: String? = null,
    dualActionButton: (@Composable () -> Unit)? = null,
    thirdActionButton: (@Composable ColumnScope.() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    BackHandler(enabled = showDialog) { onDismissRequest() }

    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
        ) {
            Dialog(
                onDismissRequest = onDismissRequest,
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false,
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.77f)
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraLarge)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        icon?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        contentText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        content()

                        Spacer(modifier = Modifier.height(24.dp))

                        thirdActionButton?.let {
                            it()
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        dualActionButton?.invoke()
                    }
                }
            }
        }
    }
}

