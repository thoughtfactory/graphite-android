package com.syncodec.graphite.presentation.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.service.syncInator.SyncInatorService


class SyncState {
//	companion object {
//		val syncStatusIcon = mapOf(
//			SyncInatorService.Companion.SyncStatus.Init::class to R.drawable.ic_fa_cloud,
//			SyncInatorService.Companion.SyncStatus.Idle::class to R.drawable.ic_cloud_done,
//			SyncInatorService.Companion.SyncStatus.Locked::class to R.drawable.ic_cloud_x,
//			SyncInatorService.Companion.SyncStatus.Connected::class to R.drawable.ic_cloud_syncing,
//			SyncInatorService.Companion.SyncStatus.Syncing::class to R.drawable.ic_cloud_syncing,
//			SyncInatorService.Companion.SyncStatus.AutoSyncDisabled::class to R.drawable.ic_cloud,
//			SyncInatorService.Companion.SyncStatus.Failed::class to R.drawable.ic_cloud_x,
//			SyncInatorService.Companion.SyncStatus.CredentialError::class to R.drawable.ic_cloud_exclamation,
//		)
//
//		@Composable
//		fun getSyncStatusIconColor(syncStatus : SyncInatorService.Companion.SyncStatus) = when (syncStatus) {
//			is SyncInatorService.Companion.SyncStatus.Init -> MaterialTheme.colorScheme.primary
//			is SyncInatorService.Companion.SyncStatus.Idle -> if (syncStatus.isAutoSyncDisabled) Color(0xFFF7D060) else Color(0xFF98D8AA)
//			is SyncInatorService.Companion.SyncStatus.Locked -> Color(0xFFE94560)
//			is SyncInatorService.Companion.SyncStatus.Connected -> Color(0xFF82AAE3)
//			is SyncInatorService.Companion.SyncStatus.Syncing -> Color(0xFF82AAE3)
//			is SyncInatorService.Companion.SyncStatus.AutoSyncDisabled -> Color(0xFFF7D060)
//			is SyncInatorService.Companion.SyncStatus.Failed -> Color(0xFFE94560)
//			is SyncInatorService.Companion.SyncStatus.CredentialError -> Color(0xFFE94560)
//			else -> MaterialTheme.colorScheme.primary
//		}
//	}
}
