package com.syncodec.graphite.presentation.note.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.LatLng
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.presentation.base.LocationContainer
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.toDate
import com.syncodec.graphite.utils.toDayTime
import com.syncodec.graphite.utils.toMonthYear
import io.realm.kotlin.types.RealmUUID
import java.time.Instant


@Preview
@Composable
fun ViewerHeader(
	modifier: Modifier = Modifier,
	noteId : RealmUUID? = null,
	title: String? = null,
	userTimestamp: Long? = null,
	locationData: LocationData = LocationData.Init,
	parentChapter: ChapterObjectLite? = null,
	savedAttachmentList : List<NoteViewModel.Companion.AttachmentState.Saved> = listOf(),
	connectedTagList: Set<TagObject> = setOf(),
	onClickSelectChapter : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		if (savedAttachmentList.isNotEmpty()) {
			AttachmentCarousel(
				modifier = modifier,
				noteId = noteId,
				fileList = savedAttachmentList.map { it.file }
			)
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background)
				.padding(top = 8.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
				) {
					Text(
						text = userTimestamp.toDate(),
						style = MaterialTheme.typography.displayMedium,
						fontWeight = FontWeight.Bold,
					)

					Spacer(modifier = Modifier.width(8.dp))

					Column(
						verticalArrangement = Arrangement.SpaceBetween,
						modifier = Modifier,
					) {
						Text(
							text = userTimestamp.toDayTime(),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
							fontWeight = FontWeight.Bold,
						)
						Spacer(modifier = Modifier.height(2.dp))
						Text(
							text = userTimestamp.toMonthYear(),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
							fontWeight = FontWeight.Bold,
						)
					}
				}
			}

			SelectionContainer {
				LocationView(locationData = locationData)
			}

			if (connectedTagList.isNotEmpty()) {
				TagView(
					connectedTagList = connectedTagList,
				)
			}
		}
	}
}

@Preview
@Composable
private fun LocationView(
	locationData: LocationData = LocationData.Init,
) {
	val inlineContentMap = remember {
		mapOf(
			"mapMarker" to InlineTextContent(
				Placeholder(9.sp, 9.sp, PlaceholderVerticalAlign.AboveBaseline)
			) {
				Row {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_map_marker_dot_solid),
						contentDescription = "Location data",
						tint = Color.LocationContainer,
						modifier = Modifier
					)
				}
			}
		)
	}
	val spanStyle = SpanStyle(
		color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
		fontSize = MaterialTheme.typography.labelMedium.fontSize,
		fontWeight = FontWeight.Bold,
		fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
		fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
	)

	Column {
		Spacer(modifier = Modifier.height(4.dp))
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp)
		) {
			when (locationData) {
				is LocationData.Init -> Unit
				is LocationData.Loading -> Unit
				is LocationData.SuccessOnlyLatLng -> Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {
					val annotatedString = buildAnnotatedString {
						appendInlineContent(id = "mapMarker")
						append(locationData.latLng.toString())
						addStyle(style = spanStyle, start = 0, end = length)
					}

					Text(annotatedString, inlineContent = inlineContentMap)
				}

				is LocationData.SuccessOnlyAddress -> Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {
					val annotatedString = buildAnnotatedString {
						appendInlineContent(id = "mapMarker")
						append(locationData.address)
						addStyle(style = spanStyle, start = 0, end = length)
					}

					Text(annotatedString, inlineContent = inlineContentMap)
				}

				is LocationData.Success -> Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {
					val annotatedString = buildAnnotatedString {
						appendInlineContent(id = "mapMarker")
						append(locationData.address)
						addStyle(style = spanStyle, start = 0, end = length)
					}

					Text(
						text = annotatedString,
						inlineContent = inlineContentMap,
						lineHeight = 12.sp
					)
				}

				is LocationData.Removed -> Unit
				is LocationData.NoPermission -> Unit
				is LocationData.AutoFetchDisabled -> Unit
				is LocationData.Error -> Unit
			}
		}
	}
}

@Preview
@Composable
private fun TagView(
	connectedTagList: Set<TagObject> = setOf(),
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth(),
		contentPadding = PaddingValues(horizontal = 8.dp)
	) {
		connectedTagList.forEach { tagObject ->
			item {
				TagItemView(tagObject = tagObject)
			}
		}
	}
}

@Preview
@Composable
private fun TagItemView(
	tagObject: TagObject = TagObject.getRandomInstance()
) {
	val containerColor = Color(tagObject.color).copy(alpha = 0.42f)
	val contentColor = Color(tagObject.color).getInverseBWColor()

	SuggestionChip(
		onClick = { /*TODO*/ },
		label = {
			Text(
				text = tagObject.tag,
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
				fontWeight = FontWeight.Bold,
			)
		},
		colors = SuggestionChipDefaults.suggestionChipColors(containerColor = containerColor),
		border = null,
		modifier = Modifier.padding(horizontal = 4.dp)
	)
}

@Preview
@Composable
private fun Preview() {
	ViewerHeader(
		title = "Title",
		userTimestamp = Instant.now().toEpochMilli(),
		locationData = LocationData.Success(latLng = LatLng(0.0, 0.0), address = "Court 3, Tennis Court, Nirma University, Ahmedabad, Gujarat, India"),
		parentChapter = ChapterObjectLite.getRandomInstance(),
		connectedTagList = setOf(TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance())
	)
}
