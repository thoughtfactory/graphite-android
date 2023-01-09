package com.syncodec.graphite.presentation.note.composable.buildingBlock


import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.pager.HorizontalPagerIndicator
import com.syncodec.graphite.presentation.common.richText.viewer.BlockQuote
import com.syncodec.graphite.presentation.common.richText.viewer.FormattedList
import com.syncodec.graphite.presentation.common.richText.viewer.Heading
import com.syncodec.graphite.presentation.common.richText.viewer.ListType
import com.syncodec.graphite.presentation.common.richText.viewer.RichText
import com.syncodec.graphite.presentation.common.richText.viewer.RichTextScope
import com.syncodec.graphite.presentation.common.richText.viewer.RichTextStyle
import com.syncodec.graphite.presentation.common.richText.viewer.RichTextThemeIntegration
import com.syncodec.graphite.presentation.common.richText.viewer.string.RichTextString
import com.syncodec.graphite.presentation.common.richText.viewer.string.RichTextStringStyle
import com.syncodec.graphite.presentation.common.richText.viewer.string.Text
import com.syncodec.graphite.presentation.common.richText.viewer.string.richTextString
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.noteViewerTimestamp
import com.syncodec.graphite.utils.roundTo
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewFile
import io.realm.kotlin.types.RealmUUID
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


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
	noteId : RealmUUID,
	content : String?,
	userTimestamp : Long,
	title : String?,
	latLng : LatLng?,
	address : String?,
	parentChapter : ChapterObject?,
	attachmentList : List<Pair<File?, Uri?>>,
	connectedTag : List<TagObjectLite>,
	onClickChapter : () -> Unit,
) {
	val context = LocalContext.current

	val tipTapData = content?.let {
		try {
			JSONObject(it)
		} catch (e : Exception) {
			Toast.makeText(context, "Error reading data", Toast.LENGTH_SHORT).show()
			null
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		if (attachmentList.isNotEmpty()) {
			AttachmentView(
				noteId = noteId,
				attachmentList = attachmentList
			)
			Spacer(modifier = Modifier.height(8.dp))
		}

		Box(
			modifier = Modifier.padding(12.dp, 0.dp)
		) {
			Header(
				userTimestamp = userTimestamp,
				title = title,
				latLng = latLng,
				address = address,
				parentChapter = parentChapter,
				connectedTag = connectedTag,
				onClickChapter = onClickChapter
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		if (tipTapData != null) {
			Box(
				modifier = Modifier.padding(12.dp, 0.dp)
			) {
				RenderContent(
					tipTapData = tipTapData,
					richTextScope = null,
					nestLevel = 0
				)
			}
		}
		Spacer(modifier = Modifier.height(96.dp))
	}
}

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
private fun AttachmentView(
	noteId : RealmUUID,
	attachmentList : List<Pair<File?, Uri?>>,
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val pagerState = rememberPagerState()

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(screenHeight * 0.31f)
	) {
		HorizontalPager(
			count = attachmentList.size,
			state = pagerState,
			modifier = Modifier
				.fillMaxWidth()
				.height(screenHeight * 0.31f)
		) { pageIndex ->
			val (file, uri) = attachmentList.getOrNull(pageIndex) ?: return@HorizontalPager
			AttachmentPreview(
				uri = uri,
				file = file,
				clickable = true,
				showActionButton = false,
				modifier = Modifier.fillMaxSize(),
				onClick = { file?.viewFile(context) },
			)
		}

		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				MenuButton(
					icon = R.drawable.ic_file,
					tint = MaterialTheme.colorScheme.onBackground,
					containerColor = MaterialTheme.colorScheme.background
				) {
					Intent(context, AttachmentActivity::class.java).apply {
						putExtra(Extra.Companion.Constant.NOTE_ID.name, noteId.bytes)
						context.startActivity(this)
					}
				}

				Spacer(modifier = Modifier.weight(1f))

				Box(
					modifier = Modifier
						.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
						.padding(8.dp, 0.dp)
				) {
					AnimatedContent(targetState = pagerState.currentPage) { page ->
						Text(
							text = "${page + 1}/${attachmentList.size}",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(12.dp, 8.dp)
						)
					}
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(50))
			) {
				HorizontalPagerIndicator(
					pagerState = pagerState,
					pageCount = attachmentList.size,
					modifier = Modifier.padding(8.dp, 4.dp)
				)
			}

			Spacer(modifier = Modifier.height(4.dp))

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.large)
			) {
				AnimatedContent(
					targetState = attachmentList[pagerState.currentPage].first
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth()
					) {
						Spacer(modifier = Modifier.width(12.dp))
						Text(
							text = it?.name ?: "Unknown",
							modifier = Modifier.weight(1f),
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold
						)

						val size = it?.length()

						val kb = size?.div(1024)
						val mb = kb?.div(1024)
						val gb = mb?.div(1024)

						Text(
							text = if (gb == 0L) {
								if (mb == 0L) {
									if (kb == 0L) {
										"$size B"
									} else {
										"$kb KB"
									}
								} else {
									"$mb MB"
								}
							} else {
								"$gb GB"
							},
							modifier = Modifier,
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold
						)

						Spacer(modifier = Modifier.width(12.dp))

						MenuButton(
							icon = R.drawable.ic_share,
							tint = MaterialTheme.colorScheme.onBackground
						) {
							val data = attachmentList.getOrNull(pagerState.currentPage)
							data?.first?.share(context)
						}

						Spacer(modifier = Modifier.width(12.dp))
					}
				}
			}
		}
	}
}

@Composable
private fun Header(
	userTimestamp : Long,
	title : String?,
	latLng : LatLng?,
	address : String?,
	parentChapter : ChapterObject?,
	connectedTag : List<TagObjectLite>,
	onClickChapter : () -> Unit
) {
	val timestamp = noteViewerTimestamp(userTimestamp)

	var showMap by remember { mutableStateOf(false) }

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
			Spacer(modifier = Modifier.weight(1f))
			if (parentChapter != null) {
				Box(
					modifier = Modifier
						.widthIn(96.dp)
						.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
						.clip(MaterialTheme.shapes.medium)
						.clickable { onClickChapter() }
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(12.dp, 8.dp)
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_notebook),
							contentDescription = "Chapter",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.requiredSize(IconButtonSize)
								.padding(2.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = parentChapter.title ?: "",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
			}
		}
		if (! address.isNullOrBlank() || latLng != null) {
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_map_marker),
					contentDescription = "Location",
					tint = Color(0xFF318DFD),
					modifier = Modifier.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = address ?: ("Lat : ${latLng?.latitude?.roundTo(6)}, " +
							"Lng : ${latLng?.longitude?.roundTo(6)}"),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f)
				)

				Spacer(modifier = Modifier.width(4.dp))

				IconButton(
					onClick = { showMap = ! showMap },
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_atlas),
						contentDescription = "Location",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(16.dp)
					)
				}
			}
		}

		AnimatedVisibility(
			visible = showMap,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			Column(modifier = Modifier.fillMaxWidth()) {
				Spacer(modifier = Modifier.height(4.dp))
				LocationMap(latLng = latLng)
				Spacer(modifier = Modifier.height(4.dp))
			}
		}

		if (connectedTag.isNotEmpty()) {
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				mainAxisSpacing = 8.dp,
				crossAxisSpacing = 4.dp
			) {
				connectedTag.forEach {
					Box(
						modifier = Modifier.background(color = Color(it.color).copy(alpha = 0.71f), shape = MaterialTheme.shapes.medium)
					) {
						Text(
							text = it.tag,
							style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
							color = Color(it.color).getInverseBWColor(),
							modifier = Modifier.padding(12.dp, 8.dp)
						)
					}
				}
			}
			Spacer(modifier = Modifier.height(0.dp))
		}
		if (! title.isNullOrBlank()) {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = title,
				modifier = Modifier,
				style = MaterialTheme.typography.headlineLarge,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun LocationMap(
	latLng : LatLng?,
) {
	val context = LocalContext.current
	val cameraPositionState = rememberCameraPositionState()

	LaunchedEffect(key1 = latLng) {
		latLng?.toGLatLng()?.let {
			cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 13f))
		}
	}

	GoogleMap(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.clip(MaterialTheme.shapes.medium),
		cameraPositionState = cameraPositionState,
		googleMapOptionsFactory = {
			GoogleMapOptions().apply {
				this.rotateGesturesEnabled(false)
				this.rotateGesturesEnabled(false)
				this.scrollGesturesEnabledDuringRotateOrZoom(false)
				this.tiltGesturesEnabled(false)
				this.zoomGesturesEnabled(false)
			}
		},
		uiSettings = MapUiSettings(
			compassEnabled = false,
			indoorLevelPickerEnabled = false,
			mapToolbarEnabled = false,
			myLocationButtonEnabled = false,
			rotationGesturesEnabled = false,
			scrollGesturesEnabled = false,
			scrollGesturesEnabledDuringRotateOrZoom = false,
			tiltGesturesEnabled = false,
			zoomControlsEnabled = false,
			zoomGesturesEnabled = false
		),
		properties = MapProperties(
			mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, if (isSystemInDarkTheme()) R.raw.map_style_dark else R.raw.map_style_light)
		),
	) {
		Marker(
			state = MarkerState(position = cameraPositionState.position.target),
		)
	}
}

@Composable
private fun RenderContent(
	tipTapData : JSONObject,
	richTextScope : RichTextScope?,
	nestLevel : Int
) {

	when (tipTapData.optString(CONTENT_TYPE)) {
		DOC -> {
			RenderDoc(
				contentList = tipTapData.optJSONArray(CONTENT),
				nestLevel = nestLevel + 1
			)
		}
	}
}

@Composable
private fun RenderDoc(
	contentList : JSONArray?,
	nestLevel : Int
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
						val content = contentList !!.optJSONObject(i)
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
	attrs : JSONObject?,
	contentList : JSONArray?,
	nestLevel : Int
) {
	richTextString {
		var textLength = 0
		for (i in 0 until (contentList?.length() ?: 0)) {
			val content = contentList !!.optJSONObject(i)
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
	attrs : JSONObject?,
	contentList : JSONArray?,
	nestLevel : Int
) {
	val level = attrs?.optInt(LEVEL)

	if (level != null && level > 0 && level < 7) {
		Heading(level = level) {
			richTextString {
				var textLength = 0
				for (i in 0 until (contentList?.length() ?: 0)) {
					val content = contentList !!.optJSONObject(i)
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
				val content = contentList !!.optJSONObject(i)
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
	attr : JSONObject?,
	contentList : JSONArray?,
	nestLevel : Int
) {
	BlockQuote {
		for (i in 0 until (contentList?.length() ?: 0)) {
			val content = contentList !!.optJSONObject(i)
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
	contentList : JSONArray?,
	listType : ListType,
	nestLevel : Int,
) {
	val itemList : MutableList<Pair<@Composable (RichTextScope.() -> Unit), Boolean?>> =
		mutableListOf()

	for (i in 0 until (contentList?.length() ?: 0)) {
		val content = contentList !!.optJSONObject(i)
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
	contentList : JSONArray?,
	attrs : JSONObject?,
	nestLevel : Int
) : Pair<@Composable (RichTextScope.() -> Unit), Boolean?> {
	return Pair(
		{
			for (i in 0 until (contentList?.length() ?: 0)) {
				val content = contentList !!.optJSONObject(i)
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
	text : String?,
	marks : JSONArray?,
	start : Int,
	end : Int,
	nestLevel : Int
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
	colorScheme : ColorScheme,
	typography : Typography
) : RichTextStyle {
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
	modifier : Modifier = Modifier,
	style : RichTextStyle? = null,
	children : @Composable RichTextScope.() -> Unit
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
	child : @Composable () -> Unit
) {
	val isApplied = LocalMaterialThemingApplied.current

	if (! isApplied) {
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
