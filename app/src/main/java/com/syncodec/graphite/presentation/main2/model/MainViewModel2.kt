package com.syncodec.graphite.presentation.main2.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.repo.Repo
import com.syncodec.graphite.di.repo.WrappedRepo
import com.syncodec.graphite.utils.DataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel2(
    private val wrappedRepo: WrappedRepo,
    private val dataStoreInstance: DataStoreInstance,
) : ViewModel() {

    init {
        Log.d(TAG, "init")
    }

    val repoFlow = wrappedRepo.repoFlow

    private val _mainScreenState = MutableStateFlow(MainScreenState())
    val mainState = _mainScreenState.asStateFlow()

    init {

        viewModelScope.launch(Dispatchers.IO) {
            dataStoreInstance.getIsFirstTime.collectLatest { isFirstTime ->
                val newMainState = mainState.value.copy(isFirstTime = isFirstTime)
                _mainScreenState.tryEmit(newMainState)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            repoFlow.collectLatest { repo ->
                Log.d(TAG, "repo : ${repo::class}")
                when (repo) {
                    is Repo.ErrorRepo -> Unit
                    is Repo.EncryptedRepo -> {
                        Log.d(TAG, "emit EncryptedRepo")
                        val newMainState = mainState.value.copy(appState = AppState.LockedRepo)
                        _mainScreenState.tryEmit(newMainState)
                    }

                    is Repo.DecryptedRepo -> {
                        Log.d(TAG, "emit DecryptedRepo")
                        val newMainState = mainState.value.copy(appState = AppState.UnlockedRepo)
                        _mainScreenState.tryEmit(newMainState)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MainViewModel2"
    }
}
