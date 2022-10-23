package com.syncodec.graphite.presentation.common.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionDialog(
	showDialog: Boolean,
	onDismiss: () -> Unit,
	onPermissionAvailable: () -> Unit
) {
	val context = LocalContext.current

	val notificationPermission =	rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

	SideEffect {
		if (showDialog && notificationPermission.status.isGranted) {
			onPermissionAvailable()
		}
	}

	GenericDialog(
		showDialog = showDialog && !notificationPermission.status.isGranted,
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Location Permission",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "We will need this permission to access current location.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Request",
			secondaryText = "Discard",
			onPrimaryClick = {
				Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					this.data = Uri.fromParts("package", context.packageName, null)
					startActivity(context, this, null)
				}
			},
			onSecondaryClick = onDismiss
		)
	}
}
