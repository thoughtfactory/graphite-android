package com.syncodec.graphite.presentation.tags.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.explorer.ExplorerActivity
import com.syncodec.graphite.presentation.tags.composable.bar.TopBar
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.SearchView
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagItem
import com.syncodec.graphite.presentation.tags.composable.dialog.TagDialog
import com.syncodec.graphite.presentation.tags.composable.dialog.TagDialogType
import com.syncodec.graphite.utils.Extra


@Preview
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun TagScreen(
	tagList : List<TagObject> = listOf(),
	putTag : (TagObject) -> Unit = {},
	deleteTag : (TagObject) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val isPro by BaseApplication.isPro.collectAsState()

	val softwareKeyboardController = LocalSoftwareKeyboardController.current

	var tagName by remember { mutableStateOf("") }

	var isEditTagDialogVisible by remember { mutableStateOf(false) }
	var isDeleteTagDialogVisible by remember { mutableStateOf(false) }

	fun openDialog(dialogType : TagDialogType) = when (dialogType) {
		TagDialogType.Edit -> isEditTagDialogVisible = true
		TagDialogType.Delete -> isDeleteTagDialogVisible = true
	}

	fun closeDialog(dialogType : TagDialogType) = when (dialogType) {
		TagDialogType.Edit -> isEditTagDialogVisible = false
		TagDialogType.Delete -> isDeleteTagDialogVisible = false
	}

	var previewTag by remember { mutableStateOf<TagObject?>(null) }

	GenericScaffold(
		topBar = { TopBar() },
		dialogContent = {
			TagDialog(
				isEditTagDialogVisible = isEditTagDialogVisible,
				isDeleteTagDialogVisible = isDeleteTagDialogVisible,
				previewTag = previewTag,
				onSave = { tag, color ->
					previewTag?.clone()?.apply {
						this.tag = tag
						this.color = color.toArgb()
						putTag(this)
						closeDialog(TagDialogType.Edit)
					}
				},
				deleteTag = deleteTag,
				closeDialog = ::closeDialog,
			)
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			SearchView(
				value = tagName,
				placeholder = "Add or search tag",
				isTagPresent = true,
				onAddTag = {
					tagList.find { it.tag == tagName }?.let {
						Toast.makeText(context, "Tag already exists", Toast.LENGTH_SHORT).show()
					} ?: TagObject().apply {
						this.tag = tagName
						previewTag = this
						openDialog(TagDialogType.Edit)
						softwareKeyboardController?.hide()
						tagName = ""
					}
				},
			) { tagName = it }

			Spacer(modifier = Modifier.height(4.dp))

			tagList.filter { it.tag.contains(tagName) }.let { tagList1 ->
				if (tagList1.isEmpty()) EmptyView()
				else LazyColumn(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					tagList1.forEach {
						item(key = it.id.toString()) {
							Box(
								modifier = Modifier.animateItemPlacement()
							) {
								TagItem(
									tag = it,
									onDelete = {
										previewTag = it
										openDialog(TagDialogType.Delete)
									},
									onEdit = {
										previewTag = it
										openDialog(TagDialogType.Edit)
									},
								) {
									Intent(context, ExplorerActivity::class.java).apply {
										putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Search.name)
										putExtra(Extra.Companion.Extra.TagId.name, it.id.bytes)
										context.startActivity(this)
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
