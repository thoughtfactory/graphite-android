package com.syncodec.graphite.presentation.tags.composable.dialog

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.getRandomColor


enum class TagDialogType {
	EDIT,
	DELETE
}

@Composable
fun TagDialog() {

	val context = LocalContext.current

	val tag = TagsActivity.LocalTagObject.current

	val showEditTagDialog = TagsActivity.LocalShowEditTagDialog.current
	val showDeleteTagDialog = TagsActivity.LocalShowDeleteTagDialog.current

	val closeDialog = TagsActivity.LocalCloseDialog.current

	val putTag = TagsActivity.LocalPutTag.current
	val deleteTag = TagsActivity.LocalDeleteTag.current

	EditTagDialog(
		showDialog = showEditTagDialog,
		tag = tag?.tag ?: "Tag",
		color = tag?.color?.let { Color(it) } ?: getRandomColor(),
		onSave = { _tag, color ->
			if (tag == null) {
				Toast.makeText(context, "Error editing tag", Toast.LENGTH_SHORT).show()
			} else {
				TagObject().apply {
					this.id = tag.id
					this.tag = _tag
					this.color = color.toArgb()
					putTag(this)
				}
			}
		},
	) {
		closeDialog(TagDialogType.EDIT)
	}

	DeleteDialog(
		showDialog = showDeleteTagDialog,
		message = "Are you sure you want to delete this tag? Notes with this tag will be not be removed.",
		onDismiss = {
			closeDialog(TagDialogType.DELETE)
		}
	) {
		deleteTag()
		closeDialog(TagDialogType.DELETE)
	}
}
