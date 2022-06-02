package com.syncodec.graphite.konstant

import com.syncodec.graphite.R

class ErrorCode {
	companion object {
		enum class ErrorCode {
			URL_RANGE_SELECTION_ERROR
		}

		val errorCodeMessageMap: Map<ErrorCode, String> = mapOf(
			ErrorCode.URL_RANGE_SELECTION_ERROR to "Please select some text to add link"
		)

		val errorCodeResourceMap: Map<ErrorCode, Int> = mapOf(
			ErrorCode.URL_RANGE_SELECTION_ERROR to R.drawable.ic_alert
		)
	}
}
