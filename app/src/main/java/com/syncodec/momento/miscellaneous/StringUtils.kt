package com.syncodec.momento.miscellaneous

class StringUtils {
	companion object {
		fun String.addEmptyLines(lines: Int) = this + "\n".repeat(lines)
	}
}
