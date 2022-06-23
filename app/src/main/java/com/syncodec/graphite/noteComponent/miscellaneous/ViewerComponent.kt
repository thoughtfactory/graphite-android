package com.syncodec.graphite.noteComponent.miscellaneous

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.richText.viewer.*
import com.syncodec.graphite.custom.richText.viewer.string.RichTextString
import com.syncodec.graphite.custom.richText.viewer.string.RichTextStringStyle
import com.syncodec.graphite.custom.richText.viewer.string.Text
import com.syncodec.graphite.custom.richText.viewer.string.richTextString
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.noteViewerTimestamp
import com.syncodec.graphite.miscellaneous.roundTo
import com.syncodec.graphite.noteComponent.NoteActivity
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
	noteContent: JSONObject?,
	attachmentMap: Map<String, Pair<AttachmentDbEntry, Uri?>>,
	connectedTag: List<String>,
	onAction: (NoteActivity.Action) -> Unit
) {
	val tiptapData = remember { noteContent }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp, 0.dp)
			.verticalScroll(rememberScrollState())
	) {
		if (noteDbEntry.attachmentKeyList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(14.dp))
			Thumbnail(attachmentMap = attachmentMap) { onAction(it) }
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

@Composable
private fun Thumbnail(
	attachmentMap: Map<String, Pair<AttachmentDbEntry, Uri?>>,
	onAction: (NoteActivity.Action) -> Unit
) {
	val context = LocalContext.current

	if (attachmentMap.isNotEmpty()) {
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(attachmentMap.values.first().second)
				.crossfade(300)
				.build(),
			placeholder = null,
			contentDescription = "Attachment",
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
				.clip(RoundedCornerShape(12.dp))
				.clickable { onAction(NoteActivity.Action.OPEN_ATTACHMENT) }
		)
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
					painter = painterResource(id = R.drawable.ic_map_marker),
					contentDescription = "Location",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = address ?: ("Lat : ${latlng?.latitude?.roundTo(6)}, " +
							"Lng : ${latlng?.longitude?.roundTo(6)}"),
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

	if (level != null && level > 0 && level < 7) {
		Heading(level = level) {
			richTextString {
				var textLength = 0
				for (i in 0 until (contentList?.length() ?: 0)) {
					val content = contentList!!.optJSONObject(i)
					val text = content.optString(TEXT)
					when (content.optString(TYPE)) {
						TEXT -> append(text = text)
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
				renderListItem(
					contentList = content.optJSONArray(CONTENT),
					attrs = content.optJSONObject(ATTRS),
					nestLevel = nestLevel + 1
				)
			)
			TASK_ITEM -> itemList.add(
				renderListItem(
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
private fun renderListItem(
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
