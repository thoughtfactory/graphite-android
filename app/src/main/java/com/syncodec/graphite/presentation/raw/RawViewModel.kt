package com.syncodec.graphite.presentation.raw

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.json.JSONObject


class RawViewModel: ViewModel() {


	val jsonObject: MutableState<JSONObject?> = mutableStateOf(null)

//	fun readObject(RealmUUID: RealmUUID, objectType: Extra.Companion.ObjectType) {
//		viewModelScope.launch(Dispatchers.IO) {
//			when (objectType) {
//				Extra.Companion.ObjectType.ATTAHCMENT -> {
//					Repository.getAttachment(RealmUUID)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.BUCKET_ITEM -> {
//					Repository.getBucketItem(RealmUUID)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.BUCKET -> {
//					Repository.getBucket(RealmUUID)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.CHAPTER -> {
//					Repository.getChapter(RealmUUID)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.NOTE -> {
//					Repository.getNote(RealmUUID)?.let {
//						withContext(Dispatchers.Main) {
//							jsonObject.value = JSONObject(objectMapper.writeValueAsString(it.toRaw()))
//						}
//					}
//				}
//				Extra.Companion.ObjectType.TAG -> {
//					Repository.getTag(RealmUUID)?.let {
//						jsonObject.value = objectMapper.writeValueAsString(it).let { json -> JSONObject(json) }
//					}
//				}
//			}
//		}
//	}
}
