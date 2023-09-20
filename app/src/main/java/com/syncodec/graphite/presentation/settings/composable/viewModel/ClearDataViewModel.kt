package com.syncodec.graphite.presentation.settings.composable.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ClearDataViewModel(repositoryStatusStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStatusStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
	}

	@WorkerThread
	suspend fun clearData() : Boolean {
		return _repository.value?.resetRealm() ?: false
	}
}
