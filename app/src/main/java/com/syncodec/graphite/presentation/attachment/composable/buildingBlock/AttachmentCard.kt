package com.syncodec.graphite.presentation.attachment.composable.buildingBlock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment.AttachmentPreview
import com.syncodec.graphite.utils.Extra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun AttachmentCard(
	uri : Uri,
	file : File,
	attachmentObject : AttachmentObject
) {
	val activity = LocalContext.current as AttachmentActivity
	val scope = rememberCoroutineScope()

	Box(
		modifier = Modifier
			.aspectRatio(1f)
			.padding(1.dp)
	) {
		AttachmentPreview(
			attachment = attachmentObject,
			uri = uri,
			file = file,
			clickable = true,
			showActionButton = false,
			modifier = Modifier.fillMaxSize(),
			onClick = {
				try {
					Intent(
						Intent.ACTION_VIEW,
						FileProvider.getUriForFile(activity, "com.syncodec.fileprovider", file)
					).apply {
						addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

						activity.startActivity(this)
					}
				} catch (e : ActivityNotFoundException) {
					Toast.makeText(activity, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
				} catch (e : Exception) {
					e.printStackTrace()
					Toast.makeText(activity, "Error viewing file", Toast.LENGTH_SHORT).show()
				}
			}
		)

		Column(
			horizontalAlignment = Alignment.End,
			modifier = Modifier
				.fillMaxSize()
				.padding(6.dp)
		) {
			MenuButton(
				icon = R.drawable.ic_note,
				containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
			) {
				scope.launch(Dispatchers.IO) {
					Intent(activity, NoteActivity::class.java).apply {
						putExtra(Extra.Companion.Constant.IS_NEW.name, false)
						putExtra(Extra.Companion.Constant.CHAPTER_ID.name, attachmentObject.getParentChapterId().toString())
						putExtra(Extra.Companion.Constant.NOTE_ID.name, attachmentObject.parentNoteId.toString())
						putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

						activity.startActivity(this)
					}
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_share,
				containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
			) {
				try {
					val sharingIntent = Intent(Intent.ACTION_SEND)
					sharingIntent.type = attachmentObject.mimeType ?: "*/*"
					sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

					Intent.createChooser(sharingIntent, "Share using").apply {
						addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
						activity.startActivity(this)
					}
				} catch (e : Exception) {
					e.printStackTrace()
					Toast.makeText(activity, "Error sharing file", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}
}
