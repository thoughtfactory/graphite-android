package com.syncodec.graphite.presentation.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.biometric.BiometricComposable
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.service.syncInator.DropboxSyncServiceConnectionManager
import com.syncodec.graphite.service.syncInator.GDriveSyncServiceConnectionManager
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : FragmentActivity() {

	private val viewModel by viewModel<MainViewModel>()

	private val syncStatus = MutableStateFlow<SyncInatorService.Companion.SyncStatus>(SyncInatorService.Companion.SyncStatus.Init)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

		val dataStoreInstance = DataStoreInstance(this)

		setContent {
			BaseComposable {
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)

				AnimatedContent(
					targetState = isFirstTime,
					transitionSpec = { fadeIn(tween(470)) togetherWith fadeOut(tween(470)) },
					label = "isFirstTime_animation"
				) {
					when (it) {
						true -> FirstTimeScreen()
						false -> BiometricComposable {
							MainScreen(
//									syncStatus = syncStatus,
//									testConnectionResponse = testConnectionResponse,
//									testDropboxConnection = { viewModel.testRemoteConnection(syncProvider) },
//									onClickSyncNow = { syncProvider?.onClickSyncNow() },
//									onClickForceSync = { syncProvider?.onClickForceSync() },
							)
						}

						null -> LoadingView()
					}
				}
			}
		}
	}


	private var dropboxServiceConnectionManager: DropboxSyncServiceConnectionManager? = null
	private var gDriveSyncServiceConnectionManager: GDriveSyncServiceConnectionManager? = null

	private fun startSyncService(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
		when (syncProvider) {
			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> {
				if (dropboxServiceConnectionManager == null) {
					dropboxServiceConnectionManager = DropboxSyncServiceConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) { syncInator.syncStatus.collect { syncStatus.tryEmit(it) } }
					}
					gDriveSyncServiceConnectionManager?.service?.hardCutOff()
					gDriveSyncServiceConnectionManager?.unbindFromService()
					gDriveSyncServiceConnectionManager = null
				}
			}

			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> {
				if (gDriveSyncServiceConnectionManager == null) {
					gDriveSyncServiceConnectionManager = GDriveSyncServiceConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) { syncInator.syncStatus.collect { syncStatus.tryEmit(it) } }
					}
					dropboxServiceConnectionManager?.service?.hardCutOff()
					dropboxServiceConnectionManager?.unbindFromService()
					dropboxServiceConnectionManager = null
				}
			}

			else -> {
				dropboxServiceConnectionManager?.service?.hardCutOff()
				dropboxServiceConnectionManager?.unbindFromService()
				dropboxServiceConnectionManager = null

				gDriveSyncServiceConnectionManager?.service?.hardCutOff()
				gDriveSyncServiceConnectionManager?.unbindFromService()
				gDriveSyncServiceConnectionManager = null
			}
		}
	}

	private fun SyncDataStoreInstance.Companion.SyncProvider.onClickSyncNow() {
//		when (this) {
//			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dropboxServiceConnectionManager?.service?.onClickSyncNow()
//			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDriveSyncServiceConnectionManager?.service?.onClickSyncNow()
//			else -> null
//		}
	}

	private fun SyncDataStoreInstance.Companion.SyncProvider.onClickForceSync() {
//		when (this) {
//			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dropboxServiceConnectionManager?.service?.onClickForceSync()
//			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDriveSyncServiceConnectionManager?.service?.onClickForceSync()
//			else -> null
//		}
	}

	override fun onDestroy() {
		dropboxServiceConnectionManager?.unbindFromService()
		gDriveSyncServiceConnectionManager?.unbindFromService()
		super.onDestroy()
	}

	companion object {
		var isInStack = false
	}
}
