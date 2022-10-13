package com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.tone
import java.io.File


@Composable
fun AttachmentPreview(
	modifier: Modifier = Modifier,
	attachment: AttachmentObject,
	uri: Uri?,
	file: File?,
	clickable: Boolean,
	showActionButton: Boolean,
	onClick: (() -> Unit)? = null,
	onRemove: (() -> Unit)? = null,
	openLink: (() -> Unit)? = null,
) {
	Box(
		modifier = modifier
			.focusable(true)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
			.clickable(clickable && onClick != null) { onClick?.invoke() },
		contentAlignment = Alignment.Center
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
