package com.syncodec.graphite.presentation.main

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class MainViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	fun putNotebook(title : String, description : String, color : Color?, bitmap : Bitmap?) {
		ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = bitmap?.encodeBase64()

			repository.putChapterSuspended(this)
		}
	}

	fun putBucket(
		title : String?,
		description : String?,
		bucketType : BucketType,
	) {
		BucketObject().apply {
			this.title = title
			this.description = description
			this.bucketType = bucketType.name

			repository.putBucket(this) { _, _ -> }
		}
	}

	fun delete(idList : List<RealmUUID>) = viewModelScope.launch(Dispatchers.Default) { repository.delete(idList) }

	fun onAuthenticate() {
		repository.isAuthenticated.tryEmit(true)
	}

	fun onAuthFailure() {
		repository.isAuthenticated.tryEmit(false)
	}

	fun onDeauthenticate() {
		repository.isAuthenticated.tryEmit(false)
	}
}
