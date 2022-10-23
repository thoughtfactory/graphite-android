package com.syncodec.graphite.presentation.raw

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class RawViewModel: ViewModel() {

	val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val jsonObject: MutableState<JSONObject?> = mutableStateOf(null)

//	fun readObject(objectId: ObjectId, objectType: Extra.Companion.ObjectType) {
//		viewModelScope.launch(Dispatchers.IO) {
//			when (objectType) {
//				Extra.Companion.ObjectType.ATTAHCMENT -> {
//					Repository.getAttachment(objectId)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.BUCKET_ITEM -> {
//					Repository.getBucketItem(objectId)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.BUCKET -> {
//					Repository.getBucket(objectId)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.CHAPTER -> {
//					Repository.getChapter(objectId)?.let {
//						jsonObject.value = JSONObject(objectMapper.writeValueAsString(it))
//					}
//				}
//				Extra.Companion.ObjectType.NOTE -> {
//					Repository.getNote(objectId)?.let {
//						withContext(Dispatchers.Main) {
//							jsonObject.value = JSONObject(objectMapper.writeValueAsString(it.toRaw()))
//						}
//					}
//				}
//				Extra.Companion.ObjectType.TAG -> {
//					Repository.getTag(objectId)?.let {
//						jsonObject.value = objectMapper.writeValueAsString(it).let { json -> JSONObject(json) }
//					}
//				}
//			}
//		}
//	}
}
