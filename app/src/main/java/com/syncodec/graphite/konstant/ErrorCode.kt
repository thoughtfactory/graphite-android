package com.syncodec.graphite.konstant

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle

class ErrorCode {
	companion object {
		enum class ErrorCode {
			URL_RANGE_SELECTION_ERROR
		}

		val errorCodeMessageMap: Map<ErrorCode, String> = mapOf(
			ErrorCode.URL_RANGE_SELECTION_ERROR to "Please select some text to add link"
		)

		val errorCodeResourceMap: Map<ErrorCode, ImageVector> = mapOf(
			ErrorCode.URL_RANGE_SELECTION_ERROR to TablerIcons.AlertCircle
		)
	}
}
