package com.syncodec.graphite.presentation.settings.composable.dialog.clearData

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ClearDataDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel : ClearDataViewModel = koinViewModel()

	var timeout by remember { mutableStateOf(1) }

	LaunchedEffect(key1 = showDialog) {
		if (showDialog) {
			timeout = if(BuildConfig.DEBUG) 1 else 10
			scope.launch {
				while (timeout > 0) {
					delay(1000)
					timeout --
				}
			}
		}
	}

	DisposableEffect(key1 = showDialog) {
		onDispose { if (! showDialog) timeout = 0 }
	}

	val clearButtonContainerColor by animateColorAsState(
		targetValue = if (timeout < 1) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	var isClearingData by remember { mutableStateOf(false) }

	GenericDialog(
		showDialog = showDialog,
		icon = R.drawable.ic_warning,
		iconTint = MaterialTheme.colorScheme.error,
		title = "Clear Data",
		contentText = "Clearing data is a destructive action. It is advised to backup your data before clearing it.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Clear Data${if (timeout > 0) " ($timeout)" else ""}",
				secondaryText = "Cancel",
				primaryColor = clearButtonContainerColor,
				primaryEnabled = timeout < 1,
				secondaryEnabled = ! isClearingData,
				onClickPrimary = {
					isClearingData = true
					viewModel.clearData { success, exception ->
						scope.launch(Dispatchers.Main) {
							if (success) {
								onDismiss()
								isClearingData = false
								Toast.makeText(context, "Data cleared successfully", Toast.LENGTH_SHORT).show()
							} else {
								isClearingData = false
								Toast.makeText(context, "Failed to clear data", Toast.LENGTH_SHORT).show()
							}
						}
					}
				},
				onClickSecondary = { onDismiss() },
			)
		},
		onDismissRequest = { if (! isClearingData) onDismiss() }
	)
}
