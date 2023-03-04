package com.syncodec.graphite.di.repository

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.util.StdConverter
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmUUID
import java.io.IOException
import java.util.Base64


class RealmUUIDDeserializer(vc : Class<*>? = null) : StdDeserializer<RealmUUID?>(vc) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(jp : JsonParser, ctxt : DeserializationContext?) : RealmUUID {
		val node : JsonNode = jp.codec.readTree(jp)

		try {
			val id = node.asText()
			if (id == null || id.isEmpty()) throw Exception() else return RealmUUID.from(id)
		} catch (e : Exception) {
			node["bytes"]?.asText()?.let {
				return RealmUUID.from(Base64.getDecoder().decode(it))
			} ?: throw Exception()
		} catch (e : Exception) {
			val id = node.binaryValue()
			if (id == null || id.isEmpty()) throw Exception() else return RealmUUID.from(id)
		} catch (e : Exception) {
			return RealmUUID.random()
		}
	}
}

class RealmUUIDKeyDeserializer : KeyDeserializer() {
	override fun deserializeKey(key : String?, ctxt : DeserializationContext?) : Any {
		return key?.let { RealmUUID.from(it) } ?: RealmUUID.random()
	}
}

class RealmUUIDSerializer(t : Class<RealmUUID?>? = null) : StdSerializer<RealmUUID>(t) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun serialize(
		value : RealmUUID?, jgen : JsonGenerator, provider : SerializerProvider
	) {
		jgen.writeString(value.toString())
	}
}

class RealmListConverter : StdConverter<RealmList<RealmUUID>, List<RealmUUID>>() {
	override fun convert(value : RealmList<RealmUUID>?) : List<RealmUUID> = value?.toList() ?: emptyList()

}
