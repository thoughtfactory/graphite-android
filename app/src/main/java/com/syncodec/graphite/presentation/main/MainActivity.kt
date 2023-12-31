package com.syncodec.graphite.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.service.syncInator.SyncerConnectionManager
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
	private val dropboxAccountInfo = MutableStateFlow<NetworkRequest<DropboxApi.Companion.DropboxAccountInfo>>(NetworkRequest.Init)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

		val dataStoreInstance = DataStoreInstance(this)

		startSyncService(SyncDataStoreInstance.Companion.SyncProvider.Dropbox)

		enableEdgeToEdge()

		setContent {
			BaseComposable {

				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)

				val syncStat1 by syncStat.collectAsState()
				val dropboxAccountInfo1 by dropboxAccountInfo.collectAsState()

				AnimatedContent(
					targetState = isFirstTime,
					transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) },
					label = "isFirstTime_animation"
				) {
					when (it) {
						true -> FirstTimeScreen()
						false -> MainScreen(
							syncStat = syncStat1,
							dropboxAccountInfo = dropboxAccountInfo1,
							onClickTestConnection = {},
							onClickForceSync = { this@MainActivity.syncerConnectionManager?.service?.syncNow(forced = true) },
							onClickSyncNow = { this@MainActivity.syncerConnectionManager?.service?.syncNow() }
						)

						null -> LoadingView()
					}
				}
			}
		}
	}


	private var syncerConnectionManager: SyncerConnectionManager? = null

	private fun startSyncService(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
		when (syncProvider) {
			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> {
				if (syncerConnectionManager == null) {
					syncerConnectionManager = SyncerConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) {
							launch { syncInator.syncStat.collect { syncStat.tryEmit(it) } }
							launch { syncInator.dropboxAccountInfo.collect { dropboxAccountInfo.tryEmit(it) } }
						}
					}
				}
			}

			else -> {
//				dropboxServiceConnectionManager?.service?.hardCutOff()
				syncerConnectionManager?.unbindFromService()
				syncerConnectionManager = null
				syncStat.tryEmit(SyncStat.NotConnected)
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
