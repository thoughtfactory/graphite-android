package com.syncodec.graphite.presentation.tags.composable.dialog

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import com.syncodec.graphite.utils.toHexString


@Preview
@Composable
fun EditTagDialog(
	showDialog : Boolean = true,
	previewTag : TagObject? = null,
	onSave : (String, Color) -> Unit = { _, _ -> },
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current

	var tag by remember { mutableStateOf("") }
	var color by remember { mutableStateOf(getRandomColor()) }

	LaunchedEffect(key1 = previewTag) {
		previewTag?.let {
			tag = it.tag
			color = Color(it.color)
		}
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Edit Tag",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Save",
				secondaryText = "Discard",
				onClickPrimary = {
					if (tag.isNotBlank()) onSave(tag, color)
					else Toast.makeText(context, "Tag cannot be empty", Toast.LENGTH_SHORT).show()
				},
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss
	) {
		DialogTextField(
			value = tag,
			label = "Tag",
			placeholder = "Add a tag",
		) { tag = it ?: "" }

		Spacer(modifier = Modifier.height(8.dp))

		ClassicColorPicker(
			color = HsvColor.from(color = color), showAlphaBar = false,
			modifier = Modifier
				.fillMaxWidth()
				.height(256.dp),
			onColorChanged = { color = it.toColor() }
		)

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.background(color, MaterialTheme.shapes.medium)
		) {
			Text(
				text = color.toHexString(),
				style = MaterialTheme.typography.bodyMedium,
				color = color.getInverseBWColor(),
				modifier = Modifier.align(Alignment.Center)
			)
		}
	}
}
