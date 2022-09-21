package com.syncodec.graphite.di.model

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class QuoteObject: RealmObject {
	@PrimaryKey val id: ObjectId = ObjectId.create()

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
