package com.syncodec.graphite.di.model.importer



data class JourneyNote(
	val text: String? = null,
	val dateModified: Long? = null,
	val dateJournal: Long? = null,
	val id: String? = null,
	val previewText: String? = null,
	val address: String? = null,
	val lat: Double? = null,
	val lon: Double? = null,
	val favourite: Boolean? = null,
	val photos: List<String?>? = null,
	val tags: List<String?>? = null,
)

//{
//	"text": "Dard ankho he Nikla to sabne bola kayar he ye\nPer jab Dard shabdo se Nikla to sabne bola shahar he ye\n- RJ Vashisht",
//	"date_modified": 1544436691831,
//	"date_journal": 1544436610246,
//	"id": "1544436610212-3fec101f663efd4f",
//	"preview_text": "",
//	"address": "",
//	"music_artist": "",
//	"music_title": "",
//	"lat": 1.7976931348623157E308,
//	"lon": 1.7976931348623157E308,
//	"mood": 0,
//	"label": "",
//	"folder": "",
//	"sentiment": 0,
//	"timezone": "Asia\/Kolkata",
//	"favourite": false,
//	"type": "",
//	"linked_account_id": "drive-b7a572f95b9bb4cc73dae24ef6c0f7a08cac2d7266f20ebe790c40c4944ee6d1",
//	"weather": {
//	"id": -1,
//	"degree_c": 1.7976931348623157E308,
//	"description": "",
//	"icon": "",
//	"place": ""
//},
//	"photos": [],
//	"tags": []
//}
