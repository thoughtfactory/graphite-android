package com.syncodec.graphite.presentation.attachment.composable.buildingBlock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.AttachmentContainer
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun AttachmentHeader(
	noteId : RealmUUID = RealmUUID.random(),
	title : String? = "Title",
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	attachmentCount : Int = 0,
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick,
			)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 12.dp),
		) {
			Text(
				text = if (title.isNullOrEmpty()) "Untitled" else title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			)
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.Bottom,
			) {
				Text(
					text = noteId.toString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.weight(1f)
				)
				Spacer(modifier = Modifier.width(16.dp))
				StateInfo(
					isFavourite = isFavourite,
					isLocked = isLocked,
					attachmentCount = attachmentCount,
				)
			}
		}
	}
}

@Preview
@Composable
fun StateInfo(
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	attachmentCount : Int = 0,
) {
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

@Preview
@Composable
private fun HeaderText(
	modifier : Modifier = Modifier,
	text : String = "Header",
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodySmall,
		color = contentColor,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		modifier = modifier
	)
}
