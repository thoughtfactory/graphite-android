package com.syncodec.graphite.presentation.tags

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.tags.composable.dialog.TagDialogType
import com.syncodec.graphite.presentation.tags.composable.screen.TagScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.getRandomColor
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TagsActivity : ComponentActivity() {

	private val viewModel by viewModels<TagsViewModel>()

	@OptIn(ExperimentalComposeUiApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {

				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				val keyboardController = LocalSoftwareKeyboardController.current
				val focusManager = LocalFocusManager.current

				var tagName by remember { mutableStateOf("") }
				var tagColor by remember { mutableStateOf(getRandomColor()) }

				val tagList = viewModel.tagList

				var showEditTagDialog by remember { mutableStateOf(false) }
				var showDeleteTagDialog by remember { mutableStateOf(false) }

				var currentTag by remember { mutableStateOf<TagObject?>(null) }

				fun openDialog(dialogType : TagDialogType, data : TagObject) {
					try {
						currentTag = data
						when (dialogType) {
							TagDialogType.EDIT -> showEditTagDialog = true
							TagDialogType.DELETE -> showDeleteTagDialog = true
						}
					} catch (e : Exception) {
						Toast.makeText(this, "Error editing tag", Toast.LENGTH_SHORT).show()
					}
				}

				fun closeDialog(dialogType : TagDialogType) {
					when (dialogType) {
						TagDialogType.EDIT -> showEditTagDialog = false
						TagDialogType.DELETE -> showDeleteTagDialog = false
					}
					try {
						keyboardController?.hide()
						focusManager.clearFocus()
					} catch (e : Exception) {
					}
				}

				this.onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							try {
								keyboardController?.hide()
								focusManager.clearFocus()
							} catch (e : Exception) {
							}
							if (showEditTagDialog || showDeleteTagDialog) {
								closeDialog(TagDialogType.EDIT)
								closeDialog(TagDialogType.DELETE)
							} else {
								finish()
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalTagObject provides currentTag,
					LocalOnChangeTagName provides { tagName = it },
					LocalOnChangeTagColor provides { tagColor = it },
					LocalTagList provides tagList,
					LocalPutTag provides {
						tagName = ""
						viewModel.putTag(it)
						closeDialog(TagDialogType.EDIT)
						try {
							keyboardController?.hide()
							focusManager.clearFocus()
						} catch (e : Exception) {
						}
					},
					LocalShowEditTagDialog provides showEditTagDialog,
					LocalShowDeleteTagDialog provides showDeleteTagDialog,
					LocalDeleteTag provides {
						viewModel.deleteTag(currentTag)
						closeDialog(TagDialogType.DELETE)
						try {
							keyboardController?.hide()
							focusManager.clearFocus()
						} catch (e : Exception) {
						}
					},
					LocalOpenDialog provides { dialogType, data -> openDialog(dialogType, data) },
					LocalCloseDialog provides ::closeDialog,
					LocalOnBackPressed provides { this.onBackPressedDispatcher.onBackPressed() }
				) {
					TagScreen()
				}
			}
		}
	}

	companion object {
		val LocalTagList = compositionLocalOf<SnapshotStateList<TagObject>> { mutableStateListOf() }
		val LocalPutTag = compositionLocalOf<(TagObject) -> Unit> { {} }

		val LocalTagObject = compositionLocalOf<TagObject?> { null }
		val LocalOnChangeTagName = compositionLocalOf<(String) -> Unit> { {} }
		val LocalOnChangeTagColor = compositionLocalOf<(Color) -> Unit> { {} }

		val LocalShowEditTagDialog = compositionLocalOf<Boolean> { false }
		val LocalShowDeleteTagDialog = compositionLocalOf<Boolean> { false }

		val LocalDeleteTag = compositionLocalOf<() -> Unit> { {} }

		val LocalOpenDialog = compositionLocalOf<(TagDialogType, TagObject) -> Unit> { { _, _ -> } }
		val LocalCloseDialog = compositionLocalOf<(TagDialogType) -> Unit> { {} }

		val LocalOnBackPressed = compositionLocalOf<() -> Unit> { {} }
	}
}
