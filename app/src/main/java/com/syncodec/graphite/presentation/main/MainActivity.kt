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
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.biometric.BiometricComposable
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.service.syncInator.DropboxSyncServiceConnectionManager
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.service.syncInator.SyncStat
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : FragmentActivity() {

	private val viewModel by viewModel<MainViewModel>()

	private val syncStat = MutableStateFlow<SyncStat>(SyncStat.Init)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

		val dataStoreInstance = DataStoreInstance(this)

		startSyncService(SyncDataStoreInstance.Companion.SyncProvider.Dropbox)

		setContent {
			BaseComposable {
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)

				AnimatedContent(
					targetState = isFirstTime,
					transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) },
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

	private fun startSyncService(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
		when (syncProvider) {
			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> {
				if (dropboxServiceConnectionManager == null) {
					dropboxServiceConnectionManager = DropboxSyncServiceConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) { syncInator.syncStat.collect { syncStat.tryEmit(it) } }
					}
				}
			}

			else -> {
//				dropboxServiceConnectionManager?.service?.hardCutOff()
				dropboxServiceConnectionManager?.unbindFromService()
				dropboxServiceConnectionManager = null
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
//		dropboxServiceConnectionManager?.unbindFromService()
		super.onDestroy()
	}

	companion object {
		var isInStack = false
	}
}
