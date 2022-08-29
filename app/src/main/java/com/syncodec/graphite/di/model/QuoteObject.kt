package com.syncodec.graphite.di.model

import com.syncodec.graphite.utils.generatePrimaryKey
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject


class QuoteObject: RealmObject {
	val _id: ObjectId = ObjectId.create()

	var date: String? = null
	var quote: String? = null
	var author: String? = null
	var authorLink: String? = null
//	var bg: ByteArray? = null
	var bgLink: String? = null
	var bgCred: String? = null
	var bgCredLink: String? = null
	var bgProvider: String? = null
	var bgProviderLink: String? = null
	var special: String? = null
	var specialLink: String? = null
	var isFavourite: Boolean? = false
}
