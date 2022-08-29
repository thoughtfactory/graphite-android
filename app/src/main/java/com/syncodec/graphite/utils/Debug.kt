package com.syncodec.graphite.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.navigation.NavHostController
import com.syncodec.graphite.presentation.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObjectLite
import io.realm.kotlin.types.ObjectId
import kotlin.random.Random


class FunctionPreviewParameter: PreviewParameterProvider<() -> Unit> {
	override val values = sequenceOf({})
}

class StringPreviewParameter: PreviewParameterProvider<String> {
	override val values: Sequence<String> = sequenceOf(generatePrimaryKey(), generatePrimaryKey(), generatePrimaryKey())
}

class MenuBottomSheetButtonDataPreviewParameter: PreviewParameterProvider<MenuBottomSheetButtonData> {
	override val values = sequenceOf(
		MenuBottomSheetButtonData(
			title = "Title",
			icon = R.drawable.ic_settings,
			onClick = {}
		)
	)
}

class NoteDayMapPreviewParameter: PreviewParameterProvider<Map<Long, List<NoteObjectLite>>> {
	override val values: Sequence<Map<Long, List<NoteObjectLite>>> = sequenceOf(
		mapOf(
			System.currentTimeMillis() to listOf(
				NoteObjectLite(
					id = ObjectId.create(),
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
					attachmentCount = 0,
					isFavourite = true,
					isLocked = false,
				)
			)
		)
	)
}
