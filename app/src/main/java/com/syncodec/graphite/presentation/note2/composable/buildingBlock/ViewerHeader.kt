package com.syncodec.graphite.presentation.note2.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.ui.LocationContainer
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.toDate
import com.syncodec.graphite.utils.toDayTime
import com.syncodec.graphite.utils.toMonthYear
import java.io.File
import java.time.Instant


@Preview
@Composable
fun ViewerHeader(
	modifier: Modifier = Modifier,
	title: String? = null,
	userTimestamp: Long? = null,
	locationData: LocationData = LocationData.Init,
	parentChapter: ChapterObjectLite? = null,
	fileList: List<File> = listOf(),
	connectedTagList: List<TagObject> = listOf(),
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		if (fileList.isNotEmpty()) {
			AttachmentCarousel(
				modifier = modifier,
				fileList = fileList
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
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
							fontWeight = FontWeight.Bold,
						)
						Spacer(modifier = Modifier.height(2.dp))
						Text(
							text = userTimestamp.toMonthYear(),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
							fontWeight = FontWeight.Bold,
						)
					}
				}

				Spacer(modifier = Modifier.weight(1f))
				Spacer(modifier = Modifier.width(64.dp))

				ChapterView(parentChapter = parentChapter)
			}

			Spacer(modifier = Modifier.height(8.dp))

			SelectionContainer {
				LocationView(
					locationData = locationData
				)
			}

			if (true) {
				Spacer(modifier = Modifier.height(8.dp))
				TagView(
					connectedTagList = listOf(TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance()),
				)
			}

			TitleView(title = title)
		}
	}
}

@Preview
@Composable
private fun ChapterView(
	parentChapter: ChapterObjectLite? = null,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable { }
			.padding(vertical = 12.dp, horizontal = 16.dp)
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_fa_notebook),
			contentDescription = "Parent chapter",
			tint = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.requiredSize(16.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = parentChapter?.title ?: "",
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Bold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
		)
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
				Placeholder(12.sp, 10.sp, PlaceholderVerticalAlign.TextCenter)
			) {
				Row {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_map_marker_dot_solid),
						contentDescription = "Location data",
						tint = Color.LocationContainer,
						modifier = Modifier
					)
					Spacer(modifier = Modifier.width(2.dp))
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

				Text(annotatedString, inlineContent = inlineContentMap)
			}

			is LocationData.Removed -> Unit
			is LocationData.NoPermission -> Unit
			is LocationData.AutoFetchDisabled -> Unit
			is LocationData.Error -> Unit
		}
	}
}

@Preview
@Composable
private fun TagView(
	connectedTagList: List<TagObject> = listOf(),
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
	val containerColor = Color(tagObject.color).copy(alpha = 0.13f)
	val contentColor = Color(tagObject.color)

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
private fun TitleView(
	title: String? = null,
) {
	title?.let {
		if (it.isNotEmpty()) {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = it,
				style = MaterialTheme.typography.displaySmall,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp)
			)
		}
	}
}

@Preview
@Composable
private fun Preview() {
	ViewerHeader(
		title = "Title",
		userTimestamp = Instant.now().toEpochMilli(),
		locationData = LocationData.Success(latLng = LatLng(0.0, 0.0), address = "Address"),
		parentChapter = ChapterObjectLite.getRandomInstance(),
		fileList = listOf(),
		connectedTagList = listOf(TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance())
	)
}
