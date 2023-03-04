package com.syncodec.graphite.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObjectLite
import io.realm.kotlin.types.RealmUUID


class FunctionPreviewParameter: PreviewParameterProvider<() -> Unit> {
	override val values = sequenceOf({})
}

//class StringPreviewParameter: PreviewParameterProvider<String> {
//	override val values: Sequence<String> = sequenceOf(generatePrimaryKey(), generatePrimaryKey(), generatePrimaryKey())
//}

class NoteDayMapPreviewParameter: PreviewParameterProvider<Map<Long, List<NoteObjectLite>>> {
	override val values: Sequence<Map<Long, List<NoteObjectLite>>> = sequenceOf(
		mapOf(
			System.currentTimeMillis() to listOf(
				NoteObjectLite(
					id = RealmUUID.random(),
					parentId = RealmUUID.random(),
					createdTimestamp = System.currentTimeMillis(),
					modifiedTimestamp = System.currentTimeMillis(),
					userTimestamp = System.currentTimeMillis(),
					title = "npr_71",
					color = null,
					latLng = LatLng(),
					address = "Nirma University",
					contentThumbnail = "Looking down the misty path to uncertain destinations",
					thumbnail = null,
					thumbnailType = null,
					isFavourite = true,
					isLocked = false,
				)
			)
		)
	)
}
