package com.syncodec.momento.database.note

data class Note(
	val key: String,
) {
	var content: String? = null
	var attachmentKeyList: MutableList<String> = mutableListOf()

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + (content?.hashCode() ?: 0)
		result = 31 * result + attachmentKeyList.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
//		WARN    to emit data from flow on every changes. Hashcode comparison not working
		return false
	}

	//	override fun equals(other: Any?): Boolean {
//		if (this.hashCode() == other.hashCode()) return false
//		if (javaClass != other?.javaClass) return false
//
//		other as Note
//
//		if (key != other.key) return false
//
//		return true
//	}
}
