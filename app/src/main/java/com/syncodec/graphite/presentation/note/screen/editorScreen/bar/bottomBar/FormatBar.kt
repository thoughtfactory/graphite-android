package com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.buildingBlock.ToolbarButton
import com.syncodec.graphite.presentation.ui.LocalIsPro
import java.util.Calendar


@Preview
@Composable
fun FormatBar(
	textFormat : RichTextEditor.Companion.TextFormat = RichTextEditor.Companion.TextFormat(),
	userTimestamp : Long? = null,
	onClickTimePicker : () -> Unit = {},
	onClickMetadata : () -> Unit = {},
	onClickLocation : () -> Unit = {},
	onClickAttachment : () -> Unit = {},
	onClickTag : () -> Unit = {},
	onClickHeading : () -> Unit = {},
	onClickSwapEditor : () -> Unit = {},
	onEditorAction: (RichTextEditor.Companion.EditorAction) -> Unit = {},
) {
	val isPro = LocalIsPro.current

	val calendar = remember { Calendar.getInstance() }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.horizontalScroll(rememberScrollState()),
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.height(48.dp)
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.31f), MaterialTheme.shapes.small
				)
				.clip(MaterialTheme.shapes.small)
				.clickable { onClickTimePicker() }
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Spacer(modifier = Modifier.width(12.dp))

				Text(
					text = DateFormat.format("dd", userTimestamp ?: calendar.timeInMillis).toString(),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.width(4.dp))

				Column(
					verticalArrangement = Arrangement.Center,
					modifier = Modifier.fillMaxHeight(),
				) {
					Text(
						text = DateFormat.format("MMM, yyyy", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)

					Text(
						text = DateFormat.format("hh:mm aa", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.31f), MaterialTheme.shapes.small
				)
				.clip(MaterialTheme.shapes.small),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(2.dp),
			) {
				Spacer(modifier = Modifier.width(2.dp))

				ToolbarButton(
					icon = R.drawable.ic_info,
					contentDescription = "Info",
					isChecked = false,
					padding = 0,
					onClick = onClickMetadata
				)

				ToolbarButton(
					icon = R.drawable.ic_map_marker,
					contentDescription = "Location",
					isChecked = false,
					padding = 0,
					onClick = onClickLocation
				)

				ToolbarButton(
					icon = R.drawable.ic_attachment_new,
					contentDescription = "Attachment",
					isChecked = false,
					padding = 0,
					onClick = onClickAttachment
				)

				ToolbarButton(
					icon = R.drawable.ic_tag,
					contentDescription = "Tag",
					isChecked = false,
					padding = 0,
					onClick = onClickTag
				)

				if (BuildConfig.DEBUG) {
					ToolbarButton(
						icon = R.drawable.ic_setting,
						contentDescription = "Swap editor",
						isChecked = false,
						padding = 0,
						onClick = onClickSwapEditor
					)
				}

				Spacer(modifier = Modifier.width(2.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		if (isPro) {
			ProFormatToolbar(
				textFormat = textFormat,
				onClickHeading = onClickHeading,
				onEditorAction = onEditorAction,
			)
		} else {
			FreeFormatToolbar(
				textFormat = textFormat,
				onEditorAction = onEditorAction,
			)
		}

		Spacer(modifier = Modifier.width(12.dp))
	}
}
