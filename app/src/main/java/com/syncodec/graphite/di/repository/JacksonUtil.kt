package com.syncodec.graphite.di.repository

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.realm.kotlin.types.RealmUUID
import java.io.IOException


class RealmUUIDDeserializer @JvmOverloads constructor(vc : Class<*>? = null) : StdDeserializer<RealmUUID?>(vc) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(jp : JsonParser, ctxt : DeserializationContext?) : RealmUUID {
		val node : JsonNode = jp.codec.readTree(jp)
		val id = node.asText()
		return RealmUUID.from(id)
	}
}
