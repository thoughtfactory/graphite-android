package com.syncodec.graphite.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.service.SyncerService


class SyncState {
	companion object {
		val syncStatusIcon = mapOf(
			SyncerService.Companion.SyncStatus.Init::class to R.drawable.ic_cloud,
			SyncerService.Companion.SyncStatus.Idle::class to R.drawable.ic_cloud_done,
			SyncerService.Companion.SyncStatus.Locked::class to R.drawable.ic_cloud_x,
			SyncerService.Companion.SyncStatus.Connected::class to R.drawable.ic_cloud_syncing,
			SyncerService.Companion.SyncStatus.Syncing::class to R.drawable.ic_cloud_syncing,
			SyncerService.Companion.SyncStatus.Paused::class to R.drawable.ic_cloud_x,
			SyncerService.Companion.SyncStatus.Failed::class to R.drawable.ic_cloud_x,
			SyncerService.Companion.SyncStatus.CredentialError::class to R.drawable.ic_cloud_exclamation,
		)

		@Composable
		fun getSyncStatusIconColor(syncStatus : SyncerService.Companion.SyncStatus) = when (syncStatus::class) {
			SyncerService.Companion.SyncStatus.Init::class -> MaterialTheme.colorScheme.primary
			SyncerService.Companion.SyncStatus.Idle::class -> Color(0xFF98D8AA)
			SyncerService.Companion.SyncStatus.Locked::class -> Color(0xFFE94560)
			SyncerService.Companion.SyncStatus.Connected::class -> Color(0xFF82AAE3)
			SyncerService.Companion.SyncStatus.Syncing::class -> Color(0xFF82AAE3)
			SyncerService.Companion.SyncStatus.Paused::class -> Color(0xFFF7D060)
			SyncerService.Companion.SyncStatus.Failed::class -> Color(0xFFE94560)
			SyncerService.Companion.SyncStatus.CredentialError::class -> Color(0xFFE94560)
			else -> MaterialTheme.colorScheme.primary
		}
	}
}
