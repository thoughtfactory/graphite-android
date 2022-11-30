package com.syncodec.graphite.presentation.tags.composable.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.presentation.tags.composable.bar.TopBar
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.SearchView
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagItem
import com.syncodec.graphite.presentation.tags.composable.dialog.TagDialog
import com.syncodec.graphite.presentation.tags.composable.dialog.TagDialogType


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TagScreen() {
	val context = LocalContext.current

	val softwareKeyboardController = LocalSoftwareKeyboardController.current

	val onChangeTag = TagsActivity.LocalOnChangeTagName.current
	val onChangeColor = TagsActivity.LocalOnChangeTagColor.current

	var tagName by remember { mutableStateOf("") }

	val tagList = TagsActivity.LocalTagList.current

	val openDialog = TagsActivity.LocalOpenDialog.current

	val onBackPressed = TagsActivity.LocalOnBackPressed.current

	val isPro by BaseApplication.isPro

	Scaffold(
		topBar = { TopBar(onBackPressed = onBackPressed) },
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				Spacer(modifier = Modifier.height(16.dp))

				SearchView(
					text = tagName,
					placeholder = "Add or search tag",
					isTagPresent = true,
					onAddTag = {
						if (! isPro && tagList.size >= 4) {
							Toast.makeText(context, "Join Graphite Pro to add more tags", Toast.LENGTH_SHORT).show()
						} else {
							tagList.find { it.tag == tagName }?.let {
								Toast.makeText(context, "Tag already exists", Toast.LENGTH_SHORT).show()
							} ?: TagObject().apply {
								this.tag = tagName
								openDialog(TagDialogType.EDIT, this)
								softwareKeyboardController?.hide()
								tagName = ""
							}
						}
					},
				) { tagName = it }

				Spacer(modifier = Modifier.height(8.dp))

				if (tagList.isEmpty()) {
					EmptyView()
				} else {
					LazyColumn(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						tagList.forEach {
							item(key = it.id.toString()) {
								Box(
									modifier = Modifier.animateItemPlacement()
								) {
									TagItem(
										tag = it,
										onDelete = { openDialog(TagDialogType.DELETE, it) },
									) {
										onChangeTag(it.tag)
										onChangeColor(Color(it.color))
										openDialog(TagDialogType.EDIT, it)
									}
								}
							}
						}
					}
				}
			}

			TagDialog()
		}
	}
}
