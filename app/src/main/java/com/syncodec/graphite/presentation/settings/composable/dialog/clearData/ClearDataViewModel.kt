package com.syncodec.graphite.presentation.settings.composable.dialog.clearData

import android.util.Log
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.repository.KoinRepository
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ClearDataViewModel(private val repository : KoinRepository) : ViewModel() {

	fun clearData(callback : (Boolean, Exception?) -> Unit) {
		repository.clearRealm { isSuccess, exception ->
			exception?.printStackTrace()
			if (isSuccess) repository.initializeRealm(callback)
			else callback(false, exception)
		}
	}
}
