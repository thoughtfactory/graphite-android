package com.syncodec.graphite.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObjectLite
import io.realm.kotlin.types.RealmUUID
import java.time.Instant


class FunctionPreviewParameter: PreviewParameterProvider<() -> Unit> {
	override val values = sequenceOf({})
}

//class StringPreviewParameter: PreviewParameterProvider<String> {
//	override val values: Sequence<String> = sequenceOf(generatePrimaryKey(), generatePrimaryKey(), generatePrimaryKey())
//}

class NoteDayMapPreviewParameter: PreviewParameterProvider<Map<Long, List<NoteObjectLite>>> {
	override val values: Sequence<Map<Long, List<NoteObjectLite>>> = sequenceOf(
		mapOf(
			Instant.now().toEpochMilli() to listOf(
				NoteObjectLite(
					id = RealmUUID.random(),
					parentId = RealmUUID.random(),
					createdTimestamp = Instant.now().toEpochMilli(),
					modifiedTimestamp = Instant.now().toEpochMilli(),
					userTimestamp = Instant.now().toEpochMilli(),
					title = "npr_71",
					color = null,
					latLng = LatLng(23.0225, 72.5714),
					address = "Nirma University",
					contentThumbnail = "Looking down the misty path to uncertain destinations",
					thumbnail = null,
					isFavourite = true,
					isLocked = false,
				)
			)
		)
	)
}
