package com.syncodec.momento.attachmentComponent

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.AttachmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttachmentViewModel(application: Application) : AndroidViewModel(application) {

	private val attachmentRepository: AttachmentRepository =
		AttachmentRepository.getInstance(momento = application as Momento)

	lateinit var activityState: AttachmentActivity.ActivityState

	var isNote: Boolean? = null
	lateinit var key: String
	var attachmentList: SnapshotStateList<Pair<AttachmentDbEntry, Uri>> = mutableStateListOf()

	var status: MutableState<Status> = mutableStateOf(Status.INIT)

	fun initData() {
		status.value = Status.LOADING
		viewModelScope.launch(Dispatchers.IO) {
			if (isNote == true) {
				attachmentRepository.getAttachmentForNoteAsFlow(noteKey = key).collect {
					attachmentList.clear()
					it.forEach {
						attachmentList.add(Pair(it, attachmentRepository.getAttachmentUri(it.key)))
					}

					status.value = Status.LOADED
				}

			} else {
				attachmentRepository.getAttachmentForNotebookAsFlow(notebookKey = key).collect {
					attachmentList.clear()
					it.forEach {
						attachmentList.add(Pair(it, attachmentRepository.getAttachmentUri(it.key)))
					}

					status.value = Status.LOADED
				}
			}
		}
	}
}
