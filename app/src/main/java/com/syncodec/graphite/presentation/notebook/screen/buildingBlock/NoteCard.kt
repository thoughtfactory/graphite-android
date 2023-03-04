package com.syncodec.graphite.presentation.notebook.screen.buildingBlock

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.getAttachmentCountFromNoteId
import com.syncodec.graphite.presentation.ui.AttachmentContainer
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LocationContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.addEmptyLines
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.roundTo
import io.realm.kotlin.types.RealmUUID


@Immutable
class NoteCardColors constructor(
	val containerColor : Color,
	val contentColor : Color,
	val selectedContainerColor : Color = containerColor,
	val selectedContentColor : Color = contentColor,
	val backgroundColor : Color = Color.Transparent,
) {
	@Composable
	internal fun containerColor(checked : Boolean) : State<Color> {
		return rememberUpdatedState(if (checked) containerColor else selectedContainerColor)
	}

	@Composable
	internal fun contentColor(enabled : Boolean) : State<Color> {
		return rememberUpdatedState(if (enabled) contentColor else selectedContentColor)
	}

	override fun hashCode() : Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + selectedContainerColor.hashCode()
		result = 31 * result + selectedContentColor.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is NoteCardColors) return false

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (selectedContainerColor != other.selectedContainerColor) return false
		if (selectedContentColor != other.selectedContentColor) return false

		return true
	}
}

object NoteCardDefaults {
	@Composable
	fun noteCardColors(
		containerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.17f),
		contentColor : Color = MaterialTheme.colorScheme.onSurface,
		selectedContainerColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
		selectedContentColor : Color = MaterialTheme.colorScheme.onSurface,
		backgroundColor : Color = MaterialTheme.colorScheme.background,
	) : NoteCardColors = NoteCardColors(
		containerColor = containerColor,
		contentColor = contentColor,
		selectedContainerColor = selectedContainerColor,
		selectedContentColor = selectedContentColor,
		backgroundColor = backgroundColor,
	)

	@Composable
	fun noteCardColorsOnSurface(
		containerColor : Color = MaterialTheme.colorScheme.background.copy(alpha = if (isSystemInDarkTheme()) 0.31f else 0.47f),
		iconColor : Color = MaterialTheme.colorScheme.onBackground,
		checkedContainerColor : Color = MaterialTheme.colorScheme.background,
		checkedIconColor : Color = MaterialTheme.colorScheme.onBackground,
		backgroundColor : Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
	) : NoteCardColors = NoteCardColors(
		containerColor = containerColor,
		contentColor = iconColor,
		selectedContainerColor = checkedContainerColor,
		selectedContentColor = checkedIconColor,
		backgroundColor = backgroundColor,
	)
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun NoteCard(
	id : RealmUUID = RealmUUID.random(),
	timestamp : String = "4th May 2021 07:13 AM",
	title : String? = null,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	contentThumbnail : String? = "The question is, what color will everything be at the moment I come for you? What will the sky be saying?",
	thumbnail : String? = null,
	address : String? = "Tennis Court, Nirma University, Ahmedabad, Gujarat, India",
	latLng : LatLng? = LatLng(latitude = 23.12601812343727, longitude = 72.54642652228279),
	tagList : List<TagObject> = listOf(),
	isSelected : Boolean = false,
	colors : NoteCardColors = NoteCardDefaults.noteCardColors(),
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {}
) {
	val context = LocalContext.current

	val containerColor by animateColorAsState(targetValue = if (isSelected) colors.selectedContainerColor else colors.containerColor)
	val contentColor by animateColorAsState(targetValue = if (isSelected) colors.selectedContentColor else colors.contentColor)

	CompositionLocalProvider(
		LocalContentColor provides contentColor,
	) {
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 8.dp, 12.dp, 0.dp)
					.background(containerColor, MaterialTheme.shapes.large)
					.clip(MaterialTheme.shapes.large)
					.combinedClickable(
						onClick = onClick,
						onLongClick = onLongClick
					),
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(10.dp)
				) {
					Header(
						timestamp = timestamp,
						title = title,
						isFavourite = isFavourite,
						isLocked = isLocked,
						attachmentCount = context.getAttachmentCountFromNoteId(noteId = id)
					)
					Spacer(modifier = Modifier.height(4.dp))
					Content(
						contentThumbnail = contentThumbnail,
						thumbnail = thumbnail,
						tagList = tagList,
					)
					Footer(address = address, latLng = latLng)
				}
			}
			androidx.compose.animation.AnimatedVisibility(
				visible = isSelected,
				enter = scaleIn(tween(300)),
				exit = scaleOut(tween(300)),
				modifier = Modifier.align(Alignment.TopEnd)
			) {
				Box(
					modifier = Modifier
						.requiredSize(32.dp)
						.align(Alignment.TopEnd)
						.background(colors.backgroundColor, CircleShape)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check_circle),
						contentDescription = null,
						tint = colors.backgroundColor.getInverseBWColor(),
						modifier = Modifier
							.requiredSize(24.dp)
							.align(Alignment.Center)
					)
				}
			}
		}
	}
}

@Preview
@Composable
private fun Header(
	timestamp : String = "4th May 2021 07:13 AM",
	title : String? = "Title",
	isFavourite : Boolean = true,
	isLocked : Boolean = true,
	attachmentCount : Int = 1
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth()
	) {
		HeaderText(text = timestamp)
		title?.let {
			HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
			HeaderText(text = it)
		}
		Spacer(modifier = Modifier.weight(1f))
		if (isLocked || isFavourite || attachmentCount > 0) {
			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(8.dp, 4.dp)
				) {
					if (isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_shield),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (isFavourite || attachmentCount > 0) HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
					}
					if (isFavourite) {
						Icon(
							painter = painterResource(id = R.drawable.ic_favourite),
							contentDescription = "Favourite",
							tint = Color.FavouriteContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (attachmentCount > 0) HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
					}
					if (attachmentCount > 0) {
						Icon(
							painter = painterResource(id = R.drawable.ic_file),
							contentDescription = "Attachment count",
							tint = Color.AttachmentContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
						HeaderText(text = "$attachmentCount")
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun Content(
	contentThumbnail : String? = "The question is, what color will everything be at the moment I come for you? What will the sky be saying?",
	thumbnail : String? = null,
	tagList : List<TagObject> = listOf(),
) {
	val contentColor = LocalContentColor.current
	Row(
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = (contentThumbnail ?: "").addEmptyLines(6),
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor.copy(alpha = 0.71f),
				maxLines = if (tagList.isEmpty()) 4 else 3,
				modifier = Modifier.fillMaxWidth()
			)

			TagList(tagList = tagList,)
		}

		thumbnail?.decodeBase64ToBitmap()?.asImageBitmap()?.let {
			Spacer(modifier = Modifier.width(8.dp))
			Box(
				modifier = Modifier
					.requiredSize(72.dp)
					.padding(2.dp)
					.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.medium)
			) {
				Image(
					bitmap = it,
					contentDescription = "Chapter Thumbnail",
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}
		}
	}
}

@Preview
@Composable
private fun ColumnScope.TagList(
	tagList : List<TagObject> = listOf()
) {
	this.apply {
		if (tagList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
			) {
				tagList.forEach { tagObject ->
					Tag(
						tag = tagObject.tag,
						color = Color(tagObject.color),
					)
					Spacer(modifier = Modifier.width(4.dp))
				}
			}
			Spacer(modifier = Modifier.height(2.dp))
		}
	}
}

@Preview
@Composable
private fun ColumnScope.Footer(
	address : String? = "Tennis Court, Nirma University, Ahmedabad, Gujarat, India",
	latLng : LatLng? = LatLng(latitude = 23.12601812343727, longitude = 72.54642652228279),
) {
	if (address != null || latLng != null) {
		this.apply {
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_map_marker),
					contentDescription = null,
					tint = Color.LocationContainer,
					modifier = Modifier.requiredSize(14.dp)
				)
				Spacer(modifier = Modifier.width(4.dp))
				address?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.labelMedium,
						fontStyle = FontStyle.Italic,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)
				} ?: latLng?.let {
					Text(
						text = "${it.latitude?.roundTo(6)}, ${it.longitude?.roundTo(6)}",
						style = MaterialTheme.typography.labelMedium,
						fontStyle = FontStyle.Italic,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)
				}
			}
		}
	}
}

@Preview
@Composable
private fun HeaderText(
	modifier : Modifier = Modifier,
	text : String = "Header",
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodySmall,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		modifier = modifier
	)
}

@Preview
@Composable
private fun Tag(
	tag : String = "Tag",
	color : Color = Color.Yellow,
) {
	Box(
		modifier = Modifier
			.background(color, MaterialTheme.shapes.small)
			.clip(MaterialTheme.shapes.small)
	) {
		Text(
			text = tag,
			style = MaterialTheme.typography.bodySmall,
			color = color.getInverseBWColor(),
			modifier = Modifier.padding(8.dp, 4.dp)
		)
	}
}
