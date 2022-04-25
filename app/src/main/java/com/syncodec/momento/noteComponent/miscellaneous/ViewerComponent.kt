package com.syncodec.momento.noteComponent.miscellaneous

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.R
import com.syncodec.momento.custom.richText.viewer.*
import com.syncodec.momento.custom.richText.viewer.string.RichTextString
import com.syncodec.momento.custom.richText.viewer.string.RichTextStringStyle
import com.syncodec.momento.custom.richText.viewer.string.Text
import com.syncodec.momento.custom.richText.viewer.string.richTextString
import com.syncodec.momento.custom.squircle.SquircleShape
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.noteViewerTimestamp
import com.syncodec.momento.miscellaneous.roundTo
import com.syncodec.momento.noteComponent.NoteActivity
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

@Composable
fun ViewerComponent(
	noteDbEntry: NoteDbEntry,
	connectedTag: List<String>,
	onAction: (NoteActivity.Action) -> Unit
) {
	val tiptapData = remember { if (noteDbEntry.content != null) noteDbEntry.content!! else null }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp, 0.dp)
			.verticalScroll(rememberScrollState())
	) {
		if (noteDbEntry.attachmentKeyList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(14.dp))
			Thumbnail(bitmap = noteDbEntry.attachmentThumbnail) { onAction(it) }
		}

		Spacer(modifier = Modifier.height(8.dp))

		Header(
			userTimestamp = noteDbEntry.userTimestamp,
			latlng = noteDbEntry.latLng,
			address = noteDbEntry.address,
			connectedTag = connectedTag
		)

		Spacer(modifier = Modifier.height(16.dp))

		if (tiptapData != null) {
			RenderContent(
				tiptapData = tiptapData,
				richTextScope = null,
				nestLevel = 0
			)
		}
		Spacer(modifier = Modifier.height(96.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Thumbnail(
	bitmap: Bitmap?,
	onAction: (NoteActivity.Action) -> Unit
) {
	if (bitmap != null) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
				.clip(SquircleShape(12.0))
				.clickable { onAction(NoteActivity.Action.OPEN_ATTACHMENT) }
		) {
			Image(
				painter = rememberImagePainter(
					data = bitmap,
					builder = { crossfade(300) }
				),
				contentDescription = "Attachment",
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		}
	}
}

@Composable
private fun Header(
	userTimestamp: Long,
	latlng: LatLng?,
	address: String?,
	connectedTag: List<String>
) {
	val timestamp = noteViewerTimestamp(userTimestamp)
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = timestamp[0],
				style = MaterialTheme.typography.bodyMedium.copy(fontSize = 48.sp),
				color = MaterialTheme.colorScheme.primary
			)
			Spacer(modifier = Modifier.width(4.dp))
			Column(
				modifier = Modifier,
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = timestamp[1],
					style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.primary
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = timestamp[2],
					style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
		if (!address.isNullOrBlank() || latlng != null) {
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_location_pin_3),
					contentDescription = "Location",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = address ?: "Lat : ${latlng?.latitude?.roundTo(6)}, " +
					"Lng : ${latlng?.longitude?.roundTo(6)}",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
		if (connectedTag.isNotEmpty()) {
			Spacer(modifier = Modifier.height(6.dp))
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				mainAxisSpacing = 8.dp,
				crossAxisSpacing = 0.dp
			) {
				connectedTag.forEach {
					Text(
						text = "#$it",
						modifier = Modifier,
						style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
						color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.47f),
					)
				}
			}
			Spacer(modifier = Modifier.height(4.dp))
		}
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
	val colorScheme = MaterialTheme.colorScheme
	val typography = MaterialTheme.typography
	val richTextStyle by remember {
		mutableStateOf(
			viewerTextStyle(
				colorScheme = colorScheme,
				typography = typography
			)
		)
	}

	val textSelectionColors = TextSelectionColors(
		handleColor = colorScheme.secondary,
		backgroundColor = colorScheme.secondary.copy(0.47f)
	)

	Surface(
		color = MaterialTheme.colorScheme.background
	) {
		CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
			SelectionContainer {
				MaterialRichText(
					style = richTextStyle,
					modifier = Modifier.fillMaxWidth(),
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
	val itemList: MutableList<Pair<@Composable (RichTextScope.() -> Unit), Boolean?>> =
		mutableListOf()

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
		if (marks == null || marks.length() == 0) {
			addFormat(
				format = RichTextString.Format.UnFormat,
				start = start,
				end = end
			)
		} else {
			for (i in 0 until marks.length()) {
				val mark = marks.getJSONObject(i)
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
					else -> {
						addFormat(
							format = RichTextString.Format.UnFormat,
							start = start,
							end = end
						)
					}
				}
			}
		}
		append(text = text)
	}
}

@OptIn(ExperimentalUnitApi::class)
private fun viewerTextStyle(
	colorScheme: ColorScheme,
	typography: Typography
): RichTextStyle {
	return RichTextStyle(
		stringStyle = RichTextStringStyle(
			unFormatStyle = SpanStyle(
				fontWeight = FontWeight.Normal,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodyMedium.fontSize,
			),
			boldStyle = SpanStyle(
				fontWeight = FontWeight.Bold,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodyMedium.fontSize,
			),
			italicStyle = SpanStyle(
				fontWeight = null,
				fontStyle = FontStyle.Italic,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodyMedium.fontSize,
			),
			underlineStyle = SpanStyle(
				textDecoration = TextDecoration.Underline,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodyMedium.fontSize,
			),
			strikethroughStyle = SpanStyle(
				textDecoration = TextDecoration.LineThrough,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodyMedium.fontSize,
			),
			subscriptStyle = SpanStyle(
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodySmall.fontSize,
				baselineShift = BaselineShift.Subscript
			),
			superscriptStyle = SpanStyle(
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodySmall.fontSize,
				baselineShift = BaselineShift.Superscript
			),
			linkStyle = SpanStyle(
				textDecoration = TextDecoration.Underline,
				fontFamily = typography.bodyMedium.fontFamily,
				fontSize = typography.bodySmall.fontSize,
				color = colorScheme.onSecondaryContainer
			)
		)
	)
}

@Composable
private fun MaterialRichText(
	modifier: Modifier = Modifier,
	style: RichTextStyle? = null,
	children: @Composable RichTextScope.() -> Unit
) {
	SetupMaterialRichText {
		RichText(
			modifier = modifier,
			style = style,
			children = children
		)
	}
}

@Composable
private fun SetupMaterialRichText(
	child: @Composable () -> Unit
) {
	val isApplied = LocalMaterialThemingApplied.current

	if (!isApplied) {
		RichTextThemeIntegration(
			textStyle = { LocalTextStyle.current },
			contentColor = { MaterialTheme.colorScheme.onBackground },
			ProvideTextStyle = { textStyle, content ->
				ProvideTextStyle(textStyle, content)
			},
			ProvideContentColor = { color, content ->
				CompositionLocalProvider(LocalContentColor provides color) {
					content()
				}
			}
		) {
			CompositionLocalProvider(LocalMaterialThemingApplied provides true) {
				child()
			}
		}
	} else {
		child()
	}
}

private val LocalMaterialThemingApplied = compositionLocalOf { false }


class MockNoteDataList : PreviewParameterProvider<List<String>> {
	override val values = sequenceOf(
		listOf(
			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"“You've gotta dance like there's nobody watching,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Love like you'll never be hurt,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Sing like there's nobody listening,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"And live like it's heaven on earth.”\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"― William W. Purkey\"}]}]}",
			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain text\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"bold\"}],\"text\":\"bold\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"italic\"}],\"text\":\"italic\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"underline\"}],\"text\":\"underline\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"strike\"}],\"text\":\"strike\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain\"},{\"type\":\"text\",\"marks\":[{\"type\":\"superscript\"}],\"text\":\"super\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"plain\"},{\"type\":\"text\",\"marks\":[{\"type\":\"subscript\"}],\"text\":\"sub\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"paragraph\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":1},\"content\":[{\"type\":\"text\",\"text\":\"H1\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":2},\"content\":[{\"type\":\"text\",\"text\":\"H2\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"H3\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"H4\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":5},\"content\":[{\"type\":\"text\",\"text\":\"H5\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":6},\"content\":[{\"type\":\"text\",\"text\":\"H6\"}]},{\"type\":\"blockquote\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"hardBreak\"},{\"type\":\"text\",\"text\":\"blockquote\"}]}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"left align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"center\"},\"content\":[{\"type\":\"text\",\"text\":\"center align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"right\"},\"content\":[{\"type\":\"text\",\"text\":\"right align\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"justify\"},\"content\":[{\"type\":\"text\",\"text\":\"justify\"}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Bullet list\"}]},{\"type\":\"bulletList\",\"content\":[{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 1\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 2\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"bullet 3\"}]}]}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Ordered list\"}]},{\"type\":\"orderedList\",\"attrs\":{\"start\":1},\"content\":[{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 1\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 2\"}]}]},{\"type\":\"listItem\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"order 3\"}]}]}]},{\"type\":\"heading\",\"attrs\":{\"textAlign\":\"left\",\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Task list\"}]},{\"type\":\"taskList\",\"content\":[{\"type\":\"taskItem\",\"attrs\":{\"checked\":false},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 1\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":true},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 2\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":true},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"task 3\"}]}]}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"}}]}"
		)
	)
}
