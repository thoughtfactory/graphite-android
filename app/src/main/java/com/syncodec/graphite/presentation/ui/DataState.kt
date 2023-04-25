package com.syncodec.graphite.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.service.syncService.SyncerService


class SyncState {
	companion object {
		val syncStatusIcon = mapOf(
			SyncerService.Companion.SyncStatus.Init::class to R.drawable.ic_cloud,
			SyncerService.Companion.SyncStatus.Idle::class to R.drawable.ic_cloud_done,
			SyncerService.Companion.SyncStatus.Locked::class to R.drawable.ic_cloud_x,
			SyncerService.Companion.SyncStatus.Connected::class to R.drawable.ic_cloud_syncing,
			SyncerService.Companion.SyncStatus.Syncing::class to R.drawable.ic_cloud_syncing,
			SyncerService.Companion.SyncStatus.AutoSyncDisabled::class to R.drawable.ic_cloud,
			SyncerService.Companion.SyncStatus.Failed::class to R.drawable.ic_cloud_x,
			SyncerService.Companion.SyncStatus.CredentialError::class to R.drawable.ic_cloud_exclamation,
		)

		@Composable
		fun getSyncStatusIconColor(syncStatus : SyncerService.Companion.SyncStatus) = when (syncStatus) {
			is SyncerService.Companion.SyncStatus.Init -> MaterialTheme.colorScheme.primary
			is SyncerService.Companion.SyncStatus.Idle -> if (syncStatus.isAutoSyncDisabled) Color(0xFFF7D060) else Color(0xFF98D8AA)
			is SyncerService.Companion.SyncStatus.Locked -> Color(0xFFE94560)
			is SyncerService.Companion.SyncStatus.Connected -> Color(0xFF82AAE3)
			is SyncerService.Companion.SyncStatus.Syncing -> Color(0xFF82AAE3)
			is SyncerService.Companion.SyncStatus.AutoSyncDisabled -> Color(0xFFF7D060)
			is SyncerService.Companion.SyncStatus.Failed -> Color(0xFFE94560)
			is SyncerService.Companion.SyncStatus.CredentialError -> Color(0xFFE94560)
			else -> MaterialTheme.colorScheme.primary
		}
	}
}
