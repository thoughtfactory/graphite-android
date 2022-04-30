package com.syncodec.graphite.miscellaneous

class StringUtils {
	companion object {
		fun String.addEmptyLines(lines: Int) = this + "\n".repeat(lines)
	}
}
