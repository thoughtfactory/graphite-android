package com.syncodec.graphite.presentation.note.composable.buildingBlock

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment.RenderGeneric
import com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment.RenderImage
import com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment.RenderPdf
import com.syncodec.graphite.utils.tone
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentPreview(
	modifier : Modifier = Modifier,
	attachment : AttachmentObject,
	uri : Uri?,
	file : File?,
	clickable : Boolean,
	showActionButton : Boolean,
	isSelected : Boolean = false,
	onClick : (() -> Unit)? = null,
	onLongClick : (() -> Unit)? = null,
	onRemove : (() -> Unit)? = null,
	openLink : (() -> Unit)? = null,
) {
	val borderColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
		animationSpec = tween(300)
	)

	val scale by animateFloatAsState(
		targetValue = if (isSelected) 0.9f else 1f,
		animationSpec = tween(300)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.focusable(true)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
			.border(2.dp, borderColor)
			.combinedClickable(
				enabled = clickable && onClick != null,
				onClick = { onClick?.invoke() },
				onLongClick = onLongClick
			)
			.graphicsLayer {
				this.scaleX = scale
				this.scaleY = scale
			}
	) {
		val type = attachment.getTypeString()
		val subType = attachment.getSubTypeString()

		when (type) {
			"image" -> RenderImage(type = type, name = attachment.name, uri = uri)
			"video" -> RenderGeneric(type = type, name = attachment.name)
			"audio" -> RenderGeneric(type = type, name = attachment.name)
			else -> {
				when (subType) {
					"pdf" -> RenderPdf(
						type = type,
						name = attachment.name,
						file = file
					)

					else -> RenderGeneric(
						type = type,
						name = attachment.name
					)
				}
			}
		}

		if (showActionButton) {
			Column(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.End,
			) {
				if (onRemove != null) {
					MenuButton(
						icon = R.drawable.ic_close,
						contentDescription = "Remove ${attachment.name}",
					) { onRemove.invoke() }
				}

				Spacer(modifier = Modifier.weight(1f))

				if (openLink != null) {
					MenuButton(
						icon = R.drawable.ic_open_link,
						contentDescription = "Open ${attachment.name}",
					) { openLink.invoke() }
				}
			}
		}
	}
}
