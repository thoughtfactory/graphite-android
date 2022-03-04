package com.syncodec.momento.noteComponent.miscellaneous

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.richText.viewer.string.InlineContent
import com.syncodec.momento.custom.richText.viewer.string.RichTextString
import com.syncodec.momento.custom.richText.viewer.string.Text
import com.syncodec.momento.custom.richText.viewer.string.richTextString
import com.syncodec.momento.R
import com.syncodec.momento.custom.richText.viewer.*
import org.json.JSONArray
import org.json.JSONObject

private const val CONTENT = "content"
private const val CONTENT_TYPE = "type"
private const val ATTRS = "attrs"
private const val MARKS = "marks"
private const val TYPE = "type"

private const val DOC = "doc"
private const val PARAGRAPH = "paragraph"
private const val HEADING = "heading"
private const val BLOCKQUOTE = "blockquote"
private const val BULLET_LIST = "bulletList"
private const val ORDERED_LIST = "orderedList"
private const val TASK_LIST = "taskList"
private const val LIST_ITEM = "listItem"
private const val TASK_ITEM = "taskItem"
private const val TEXT = "text"
private const val HARD_BREAK = "hardBreak"

private const val LEVEL = "level"
private const val CHECKED = "checked"

private const val BOLD = "bold"
private const val ITALIC = "italic"
private const val UNDERLINE = "underline"
private const val STRIKE = "strike"
private const val SUPERSCRIPT = "superscript"
private const val SUBSCRIPT = "subscript"

@Preview
@Composable
fun ViewerComponent(
	@PreviewParameter(MockNoteDataList::class)
	noteData: String
) {
	val tiptapData = JSONObject(noteData)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(12.dp, 0.dp)
			.verticalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.height(12.dp))
		RenderContent(
			tiptapData = tiptapData,
			richTextScope = null,
			nestLevel = 0
		)
		Spacer(modifier = Modifier.height(96.dp))
	}
}

@Composable
private fun RenderContent(
	tiptapData: JSONObject,
	richTextScope: RichTextScope?,
	nestLevel: Int
) {
	when (tiptapData.optString(CONTENT_TYPE)) {
		DOC -> {
			RenderDoc(
				contentList = tiptapData.optJSONArray(CONTENT),
				nestLevel = nestLevel + 1
			)
		}
	}
}

@Composable
private fun RenderDoc(
	contentList: JSONArray?,
	nestLevel: Int
) {
	RichText(
		modifier = Modifier
			.fillMaxWidth(),
	) {
		for (i in 0 until (contentList?.length() ?: 0)) {
			val content = contentList!!.optJSONObject(i)
			when (content.optString(TYPE)) {
				PARAGRAPH -> RenderParagraph(
					attrs = content.optJSONObject(ATTRS),
					contentList = content.optJSONArray(CONTENT),
					nestLevel = nestLevel + 1
				)
				HEADING -> RenderHeading(
					attrs = content.optJSONObject(ATTRS),
					contentList = content.optJSONArray(CONTENT),
					nestLevel = nestLevel + 1
				)
				BLOCKQUOTE -> RenderBlockquote(
					attr = content.optJSONObject(ATTRS),
					contentList = content.optJSONArray(CONTENT),
					nestLevel = nestLevel + 1
				)
				BULLET_LIST -> RenderList(
					contentList = content.optJSONArray(CONTENT),
					listType = ListType.Unordered,
					nestLevel = nestLevel + 1
				)
				ORDERED_LIST -> RenderList(
					contentList = content.optJSONArray(CONTENT),
					listType = ListType.Ordered,
					nestLevel = nestLevel + 1
				)
				TASK_LIST -> RenderList(
					contentList = content.optJSONArray(CONTENT),
					listType = ListType.Task,
					nestLevel = nestLevel + 1
				)
			}
		}
	}
}

@Composable
private fun RichTextScope.RenderParagraph(
	attrs: JSONObject?,
	contentList: JSONArray?,
	nestLevel: Int
) {
	richTextString {
		var textLength = 0
		for (i in 0 until (contentList?.length() ?: 0)) {
			val content = contentList!!.optJSONObject(i)
			val text = content.optString(TEXT)

			when (content.optString(TYPE)) {
				TEXT -> RenderText(
					text = text,
					marks = content.optJSONArray(MARKS),
					start = textLength,
					end = textLength + text.length,
					nestLevel = nestLevel + 1
				)
				HARD_BREAK -> RenderText(
					text = "\n",
					marks = content.optJSONArray(MARKS),
					start = textLength,
					end = textLength + text.length,
					nestLevel = nestLevel + 1
				)
			}
			textLength += text.length
		}

		Text(
			text = toRichTextString(),
			modifier = Modifier,
			onTextLayout = {
			}
		)
	}
}

@Composable
private fun RichTextScope.RenderHeading(
	attrs: JSONObject?,
	contentList: JSONArray?,
	nestLevel: Int
) {
	val level = attrs?.optInt(LEVEL)

	if (level != null) {
		Heading(level = level) {
			richTextString {
				var textLength = 0
				for (i in 0 until (contentList?.length() ?: 0)) {
					val content = contentList!!.optJSONObject(i)
					val text = content.optString(TEXT)
					when (content.optString(TYPE)) {
						TEXT -> RenderText(
							text = text,
							marks = content.optJSONArray(MARKS),
							start = textLength,
							end = textLength + text.length,
							nestLevel = nestLevel + 1
						)
					}
					textLength += text.length
				}
				Text(text = toRichTextString())
			}
		}
	} else {
		richTextString {
			var textLength = 0
			for (i in 0 until (contentList?.length() ?: 0)) {
				val content = contentList!!.optJSONObject(i)
				val text = content.optString(TEXT)
				when (content.optString(TYPE)) {
					TEXT -> RenderText(
						text = text,
						marks = content.optJSONArray(MARKS),
						start = textLength,
						end = textLength + text.length,
						nestLevel = nestLevel + 1
					)
				}
				textLength += text.length
			}
			Text(text = toRichTextString())
		}
	}
}


@Composable
private fun RichTextScope.RenderBlockquote(
	attr: JSONObject?,
	contentList: JSONArray?,
	nestLevel: Int
) {
	BlockQuote {
		for (i in 0 until (contentList?.length() ?: 0)) {
			val content = contentList!!.optJSONObject(i)
			when (content.optString(TYPE)) {
				PARAGRAPH -> RenderParagraph(
					attrs = content.optJSONObject(ATTRS),
					contentList = content.optJSONArray(CONTENT),
					nestLevel = nestLevel + 1
				)
			}
		}
	}
}

@Composable
private fun RichTextScope.RenderList(
	contentList: JSONArray?,
	listType: ListType,
	nestLevel: Int,
) {
	val itemList: MutableList<Pair<@Composable (RichTextScope.() -> Unit), Boolean?>> = mutableListOf()

	for (i in 0 until (contentList?.length() ?: 0)) {
		val content = contentList!!.optJSONObject(i)
		when (content.optString(TYPE)) {
			LIST_ITEM -> itemList.add(
				RenderListItem(
					contentList = content.optJSONArray(CONTENT),
					attrs = content.optJSONObject(ATTRS),
					nestLevel = nestLevel + 1
				)
			)
			TASK_ITEM -> itemList.add(
				RenderListItem(
					contentList = content.optJSONArray(CONTENT),
					attrs = content.optJSONObject(ATTRS),
					nestLevel = nestLevel + 1
				)
			)
		}
	}

	if (itemList.isNotEmpty()) {
		FormattedList(
			listType = listType,
			*itemList.toTypedArray()
		)
	}
}

@Composable
private fun RenderListItem(
	contentList: JSONArray?,
	attrs: JSONObject?,
	nestLevel: Int
): Pair<@Composable (RichTextScope.() -> Unit), Boolean?> {
	return Pair(
		{
			for (i in 0 until (contentList?.length() ?: 0)) {
				val content = contentList!!.optJSONObject(i)
				when (content.optString(TYPE)) {
					PARAGRAPH -> RenderParagraph(
						attrs = content.optJSONObject(ATTRS),
						contentList = content.optJSONArray(CONTENT),
						nestLevel = nestLevel + 1
					)
				}
			}
		},
		attrs?.optBoolean(CHECKED)
	)
}

@Composable
private fun RichTextString.Builder.RenderText(
	text: String?,
	marks: JSONArray?,
	start: Int,
	end: Int,
	nestLevel: Int
) {
	if (text != null) {
		for (i in 0 until (marks?.length() ?: 0)) {
			val mark = marks!!.getJSONObject(i)
			when (mark.optString(TYPE)) {
				BOLD -> {
					addFormat(
						format = RichTextString.Format.Bold,
						start = start,
						end = end
					)
				}
				ITALIC -> {
					addFormat(
						format = RichTextString.Format.Italic,
						start = start,
						end = end
					)
				}
				UNDERLINE -> {
					addFormat(
						format = RichTextString.Format.Underline,
						start = start,
						end = end
					)
				}
				STRIKE -> {
					addFormat(
						format = RichTextString.Format.Strikethrough,
						start = start,
						end = end
					)
				}
				SUPERSCRIPT -> {
					addFormat(
						format = RichTextString.Format.Superscript,
						start = start,
						end = end
					)
				}
				SUBSCRIPT -> {
					addFormat(
						format = RichTextString.Format.Subscript,
						start = start,
						end = end
					)
				}
			}
		}
		append(text = text)
	}
}

private val checkBoxFalse = InlineContent {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.height(20.dp),
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_checkbox_unchecked),
			contentDescription = "Unchecked",
			tint = MaterialTheme.colorScheme.primary,
			modifier = Modifier
				.requiredSize(18.dp)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}


class MockNoteDataList : PreviewParameterProvider<List<String>> {
	override val values = sequenceOf(
		listOf(
			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"“You've gotta dance like there's nobody watching,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Love like you'll never be hurt,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Sing like there's nobody listening,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"And live like it's heaven on earth.”\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"― William W. Purkey\"}]}]}",
			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain text\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"bold\"}],\"text\":\"bold\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"italic\"}],\"text\":\"italic\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"underline\"}],\"text\":\"underline\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"strike\"}],\"text\":\"strike\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain\"},{\"type\":\"text\",\"marks\":[{\"type\":\"superscript\"}],\"text\":\"super\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain\"},{\"type\":\"text\",\"marks\":[{\"type\":\"subscript\"}],\"text\":\"sub\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"paragraph\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":1},\"content\":[{\"type\":\"text\",\"text\":\"H1\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":2},\"content\":[{\"type\":\"text\",\"text\":\"H2\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"H3\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"H4\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":5},\"content\":[{\"type\":\"text\",\"text\":\"H5\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":6},\"content\":[{\"type\":\"text\",\"text\":\"H6\"}]},{\"type\":\"blockquote\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"hardBreak\"},{\"type\":\"text\",\"text\":\"blockquote\"}]}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"left align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"center\"},\"content\":[{\"type\":\"text\",\"text\":\"center align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"right\"},\"content\":[{\"type\":\"text\",\"text\":\"right align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"justify\"},\"content\":[{\"type\":\"text\",\"text\":\"justify\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Bullet list\"}]},{\"type\":\"bulletList\",\"content\":[{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 1\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 2\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 3\"}]}]}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Ordered list\"}]},{\"type\":\"orderedList\",\"attrs\":{\"start\":1},\"content\":[{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 1\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 2\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 3\"}]}]}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Task list\"}]},{\"type\":\"taskList\",\"content\":[{\"type\":\"taskItem\",\"attrs\":{\"checked\":false},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 1\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":true},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 2\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":true},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 3\"}]}]}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"}}]}"
		)
	)
}
