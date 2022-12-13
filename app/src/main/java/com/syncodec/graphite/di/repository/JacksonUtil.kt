package com.syncodec.graphite.di.repository

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.realm.kotlin.types.RealmUUID
import java.io.IOException
import java.util.Base64


class RealmUUIDDeserializer @JvmOverloads constructor(vc : Class<*>? = null) : StdDeserializer<RealmUUID?>(vc) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(jp : JsonParser, ctxt : DeserializationContext?) : RealmUUID {
		val node : JsonNode = jp.codec.readTree(jp)

		try {
			val id = node.asText()
			if (id == null || id.isEmpty()) {
				throw Exception()
			} else {
				return RealmUUID.from(id)
			}
		} catch (e : Exception) {
			node["bytes"]?.asText()?.let {
				return RealmUUID.from(Base64.getDecoder().decode(it))
			} ?: run {
				throw Exception()
			}
		}
		catch (e : Exception) {
			val id = node.binaryValue()
			if (id == null || id.isEmpty()) {
				throw Exception()
			} else {
				return RealmUUID.from(id)
			}
		} catch (e : Exception) {
			return RealmUUID.random()
		}
	}
}
