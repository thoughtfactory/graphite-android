package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LocationContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.roundTo
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun ChapterCard(
	id : RealmUUID = RealmUUID.random(),
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	title : String? = null,
	description : String? = null,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	color : Color? = null,
	thumbnail : String? = null,
	noteCount : Int = 0,
	chapterCount : Int = 0,
	isSelected : Boolean = false,
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {}
) {
	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) else color ?: MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else color?.getInverseBWColor() ?: MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	var boxHeight by remember { mutableStateOf<Int?>(null) }

	var bitmap by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = thumbnail?.hashCode()) {
		bitmap = thumbnail?.decodeBase64ToBitmap()
	}

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
					)
					.onGloballyPositioned { coordinates -> boxHeight = coordinates.size.height },
			) {
				if (! isSelected) boxHeight?.let { height ->
					bitmap?.let {
						Image(
							bitmap = it.asImageBitmap(),
							contentDescription = "Chapter Thumbnail",
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.fillMaxWidth()
								.height(with(LocalDensity.current) { height.toDp() }),
						)
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(with(LocalDensity.current) { height.toDp() })
								.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f))
						)
					}
				}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(10.dp)
				) {
					Header(
						title = title,
						noteCount = noteCount,
						chapterCount = chapterCount,
						isFavourite = isFavourite,
						isLocked = isLocked,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Content(
						description = description,
						createdTimestamp = createdTimestamp,
						modifiedTimestamp = modifiedTimestamp,
					)
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
						.background(MaterialTheme.colorScheme.background, CircleShape)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check_circle),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
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
	title : String? = "Untitled",
	noteCount : Int = 0,
	chapterCount : Int = 0,
	isFavourite : Boolean = true,
	isLocked : Boolean = true,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			maxLines = 1,
			modifier = Modifier.weight(1f)
		)
		Spacer(modifier = Modifier.width(8.dp))
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colorScheme.onSurface,
		) {
			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), MaterialTheme.shapes.small)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(8.dp, 4.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_note),
						contentDescription = "Note count",
						tint = Color(0xFF7986CB),
						modifier = Modifier.requiredSize(14.dp)
					)
					HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
					HeaderText(text = "$noteCount", modifier = Modifier.padding(horizontal = 2.dp))
					HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
					Icon(
						painter = painterResource(id = R.drawable.ic_notebook),
						contentDescription = "Chapter count",
						tint = Color(0xFFFF8A65),
						modifier = Modifier.requiredSize(14.dp)
					)
					HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
					HeaderText(text = "$chapterCount", modifier = Modifier.padding(horizontal = 2.dp))
					if (isLocked) {
						HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
						Icon(
							painter = painterResource(id = R.drawable.ic_shield),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
					}
					if (isFavourite) {
						HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
						Icon(
							painter = painterResource(id = R.drawable.ic_favourite),
							contentDescription = "Favourite",
							tint = Color.FavouriteContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun ColumnScope.Content(
	description : String? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
) {
	this.apply {
		Text(
			text = if (description.isNullOrEmpty()) "No description" else description,
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			maxLines = 1,
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Created on:   ${createdTimestamp?.timeStampToPrettyFull() ?: "Unknown"}",
			style = MaterialTheme.typography.labelMedium,
			fontStyle = FontStyle.Italic,
			maxLines = 1,
		)
		Text(
			text = "Modified on: ${modifiedTimestamp?.timeStampToPrettyFull() ?: "Unknown"}",
			style = MaterialTheme.typography.labelMedium,
			fontStyle = FontStyle.Italic,
			maxLines = 1,
		)
	}
}

@Preview
@Composable
private fun Footer(
	address : String? = "Tennis Court, Nirma University, Ahmedabad, Gujarat, India",
	latLng : LatLng? = LatLng(latitude = 23.12601812343727, longitude = 72.54642652228279),
) {
	if (address != null || latLng != null) {
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
