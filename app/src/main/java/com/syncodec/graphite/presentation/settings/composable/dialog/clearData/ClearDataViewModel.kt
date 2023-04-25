package com.syncodec.graphite.presentation.settings.composable.dialog.clearData

import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.repository.repository.Repository
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ClearDataViewModel(private val repository : Repository) : ViewModel() {

	fun clearData(callback : (Boolean, Exception?) -> Unit) {
		repository.clearRealm { isSuccess, exception ->
			if (isSuccess) repository.initializeRealm(callback)
			else callback(false, exception)
		}
	}
}
