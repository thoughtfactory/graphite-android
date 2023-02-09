package com.syncodec.graphite.presentation.dropbox.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.di.sync.dropbox.DropboxResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class DropboxScreenViewModel(private val dBox : DBox) : ViewModel() {

	val dropboxState = MutableStateFlow<DBox.Companion.DropboxState>(DBox.Companion.DropboxState.Init)

	init {
		viewModelScope.launch(Dispatchers.IO) {
			dropboxState.tryEmit(DBox.Companion.DropboxState.Loading)
			dBox.testConnectionConnection {
				when (it) {
					is DropboxResponse.Init -> null
					is DropboxResponse.Success<*> -> (it.result as Pair<*, *>).let { data ->
						dropboxState.tryEmit(DBox.Companion.DropboxState.Connected(data.first as FullAccount, data.second as SpaceUsage))
					}

					else -> dropboxState.tryEmit(DBox.Companion.DropboxState.Error)
				}
			}
		}
	}

	fun exchangeCodeForToken(code : String) {
		viewModelScope.launch(Dispatchers.IO) {
			dBox.exchangeCodeForToken(code) { dropboxResponse ->
				when (dropboxResponse) {
					is DropboxResponse.Init -> null
					is DropboxResponse.Success<*> -> testConnection()
					is DropboxResponse.Loading -> null
					is DropboxResponse.Error -> null
				}
			}
		}
	}

	fun testConnection() {
		viewModelScope.launch(Dispatchers.IO) {
			if (dropboxState.value != DBox.Companion.DropboxState.Loading) {
				dropboxState.tryEmit(DBox.Companion.DropboxState.Loading)
				dBox.testConnectionConnection {
					when (it) {
						is DropboxResponse.Init -> null
						is DropboxResponse.Success<*> -> (it.result as Pair<*, *>).let { data ->
							dropboxState.tryEmit(DBox.Companion.DropboxState.Connected(data.first as FullAccount, data.second as SpaceUsage))
						}

						is DropboxResponse.Loading -> null
						is DropboxResponse.Error -> null
					}
				}
			}
		}
	}
}
