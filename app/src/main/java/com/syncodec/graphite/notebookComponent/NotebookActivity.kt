package com.syncodec.graphite.notebookComponent

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.R
import com.syncodec.graphite.attachmentComponent.AttachmentActivity
import com.syncodec.graphite.custom.DeleteDialog
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.DataStore
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.notebookComponent.miscellaneous.TopBar
import com.syncodec.graphite.notebookComponent.modalBottomSheet.BottomSheetType
import com.syncodec.graphite.notebookComponent.modalBottomSheet.SheetLayout
import com.syncodec.graphite.notebookComponent.screen.NotebookScreen
import com.syncodec.graphite.ui.theme.GraphiteBase
import com.syncodec.graphite.vaultComponent.EvokeReason
import com.syncodec.graphite.vaultComponent.VaultScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NotebookActivity : ComponentActivity() {

	private val viewModel by viewModels<NotebookViewModel>()

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name).also {
			if (it == null) {
				finish()
			} else {
				viewModel.notebookKey = it
				viewModel.initData()
			}
		}

		setContent {
			viewModel.activityState = rememberActivityState()

			GraphiteBase {
				val systemUiController = rememberSystemUiController()
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)

				Screen()
			}
		}
	}

	override fun onBackPressed() {
		val activityState = viewModel.activityState
		when {
			viewModel.vaultState.value == Graphite.Companion.VaultState.TRY_OPEN -> viewModel.vaultState.value = Graphite.Companion.VaultState.CLOSED
			activityState.isSelected.value -> {
				activityState.isSelected.value = false
				activityState.selectedItemList.clear()
			}
			activityState.showFavourite.value || activityState.showArchived.value || activityState.showLocked.value -> {
				activityState.showFavourite.value = false
				activityState.showArchived.value = false
				activityState.showLocked.value = false
			}
			viewModel.chapterPath.isNotEmpty() -> {
				viewModel.chapterPath.removeLast()
				viewModel.chapterNamePath.removeLast()
			}
			else -> super.onBackPressed()
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	private fun onPerformAction(action: Action, data: Any?) {
		val activityState = viewModel.activityState
		when (action) {
			Action.BACK -> onBackPressed()
			Action.MENU -> {
				activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
				activityState.scope.launch { activityState.bottomSheetState.show() }
			}
			Action.SHOW_DELETE -> activityState.showDeleteDialog.value = true
			Action.CLICK_NEW_CHAPTER -> {
				activityState.bottomSheetType.value = BottomSheetType.NewChapterBottomSheet
				activityState.scope.launch { activityState.bottomSheetState.show() }
			}
			Action.ON_NEW_CHAPTER -> {
				data as Pair<*, *>
				viewModel.putChapter(data.first as String, data.second as String)
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.CLICK_NEW_NOTE -> {
				activityState.bottomSheetType.value = BottomSheetType.NewNoteBottomSheet
				activityState.scope.launch { activityState.bottomSheetState.show() }
			}
			Action.ON_NEW_NOTE -> {
				data as String
				activityState.scope.launch { activityState.bottomSheetState.hide() }

				Intent(this, NoteActivity::class.java).apply {
					putExtra(
						Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
						viewModel.notebookKey
					)
					putStringArrayListExtra(
						Konstant.Companion.Konstant.CHAPTER_KEY.name,
						ArrayList(viewModel.chapterPath)
					)
					putExtra(Konstant.Companion.Konstant.TITLE.name, data)
					putExtra(Konstant.Companion.Konstant.IS_NEW.name, true)
					putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, false)

					startActivity(this)
				}
			}
			Action.CLICK_NOTE_HEADER ->
				activityState.showNotes.value = !activityState.showNotes.value
			Action.CLICK_NOTE -> {
				data as String
				val selectedItemList = activityState.selectedItemList

				if (activityState.isSelected.value) {
					if (data in selectedItemList) {
						selectedItemList.remove(data)
					} else {
						selectedItemList.add(data)
					}
				} else {
					Intent(this, NoteActivity::class.java).apply {
						putExtra(
							Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
							viewModel.notebookKey
						)
						putStringArrayListExtra(
							Konstant.Companion.Konstant.CHAPTER_KEY.name,
							ArrayList(viewModel.chapterPath)
						)
						putExtra(Konstant.Companion.Konstant.NOTE_KEY.name, data)
						putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, true)
						putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)

						putExtra(
							Konstant.Companion.Konstant.SHOW_ARCHIVED.name,
							viewModel.activityState.showArchived.value
						)
						putExtra(
							Konstant.Companion.Konstant.SHOW_LOCKED.name,
							viewModel.vaultState.value == Graphite.Companion.VaultState.OPENED
						)

						startActivity(this)
					}
				}
			}
			Action.LONG_CLICK_NOTE -> {
				data as String
				val selectedItemList = activityState.selectedItemList

				if (activityState.isSelected.value) {
					if (data in selectedItemList) {
						selectedItemList.remove(data)
					} else {
						selectedItemList.add(data)
					}
				} else {
					activityState.isSelected.value = true
					if (activityState.selectedItemList.contains(data)) {
						activityState.selectedItemList.remove(data)
					} else {
						activityState.selectedItemList.add(data)
					}
				}
			}
			Action.CLICK_CHAPTER_HEADER ->
				activityState.showChapters.value = !activityState.showChapters.value
			Action.CLICK_CHAPTER -> {
				data as ChapterDbEntry
				val selectedItemList = activityState.selectedItemList

				if (activityState.isSelected.value) {
					if (data.key in selectedItemList) {
						selectedItemList.remove(data.key)
					} else {
						selectedItemList.add(data.key)
					}
				} else {
					viewModel.chapterPath.add(data.key)
					viewModel.chapterNamePath.add(data.title)
				}
			}
			Action.LONG_CLICK_CHAPTER -> {
				data as String
				activityState.isSelected.value = true
				if (activityState.selectedItemList.contains(data)) {
					activityState.selectedItemList.remove(data)
				} else {
					activityState.selectedItemList.add(data)
				}
			}
			Action.NAVIGATE_CHAPTER -> {
				data as Int
				for (i in 0 until data) {
					viewModel.chapterPath.removeLastOrNull()
					viewModel.chapterNamePath.removeLastOrNull()
				}
			}
			Action.ATTACHMENT -> {
				Intent(this, AttachmentActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.IS_NOTE.name, false)
					putExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name, viewModel.notebookKey)
					startActivity(this)
				}
			}
			Action.SET_AS_DEFAULT -> {
				val dataStore = DataStore(this)
				dataStore.putDefaultNotebookKey(notebookKey = viewModel.notebookKey)
				activityState.scope.launch { activityState.bottomSheetState.hide() }
				Toast.makeText(
					this,
					"${viewModel.notebookDbEntry.value?.title} set as default notebook",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.ATLAS -> null
			Action.EDIT_NOTEBOOK -> {
				activityState.bottomSheetType.value = BottomSheetType.EditBottomSheet
				activityState.scope.launch { activityState.bottomSheetState.show() }
			}
			Action.VAULT -> {
				viewModel.vaultState.value = when (viewModel.vaultState.value) {
					Graphite.Companion.VaultState.NOT_OPENED -> Graphite.Companion.VaultState.TRY_OPEN
					Graphite.Companion.VaultState.OPENED -> {
						Toast.makeText(this, "Vault closed...", Toast.LENGTH_SHORT).show()
						Graphite.Companion.VaultState.CLOSED
					}
					Graphite.Companion.VaultState.CLOSED -> Graphite.Companion.VaultState.TRY_OPEN
					else -> Graphite.Companion.VaultState.NOT_OPENED
				}
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.UPDATE_NOTEBOOK -> {
				data as NotebookDbEntry
				viewModel.updateNotebook(notebook = data)
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.TOGGLE_FAVOURITE -> {
				activityState.showFavourite.value = !activityState.showFavourite.value
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.TOGGLE_ARCHIVED -> {
				activityState.showArchived.value = !activityState.showArchived.value
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
		ExperimentalMaterialApi::class
	)
	@Composable
	private fun Screen() {
		val dataStore = remember { DataStore(context = this) }

		val activityState = viewModel.activityState
		val status by viewModel.status
		val passcode by dataStore.getPasscode.collectAsState(initial = null)

		val showFavourite by activityState.showFavourite
		val showArchived by activityState.showArchived
		val showLocked by activityState.showLocked
		val vaultState by viewModel.vaultState

		ModalBottomSheetLayout(
			sheetState = activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = { SheetLayout { action, data -> onPerformAction(action, data) } },
		) {
			Crossfade(targetState = viewModel.vaultState.value) {
				if (it == Graphite.Companion.VaultState.TRY_OPEN) {
					VaultScreen(
						evokeReason = if (passcode == "") EvokeReason.NEW_PASSCODE else EvokeReason.UNLOCK_VAULT,
						onSuccess = {
							viewModel.vaultState.value = Graphite.Companion.VaultState.OPENED
							viewModel.activityState.showLocked.value = true
						}
					) { Toast.makeText(this, "Error opening vault...", Toast.LENGTH_SHORT).show() }
				} else {
					Scaffold(
						containerColor = MaterialTheme.colorScheme.background,
						topBar = {
							if (status == Status.LOADED) {
								TopBar(
									notebookDbEntry = viewModel.notebookDbEntry.value!!,
									chapterNamePath = viewModel.chapterNamePath,
									isSelected = activityState.isSelected.value,
									selectedItemSize = activityState.selectedItemList.size,
									showFavorite = activityState.showFavourite.value,
									showArchived = activityState.showArchived.value,
								) { action, data -> onPerformAction(action, data) }
							}
						},
						floatingActionButton = {
							Column {
								FloatingActionButton(
									onClick = { onPerformAction(Action.CLICK_NEW_CHAPTER, null) }
								) {
									Icon(
										painter = painterResource(id = R.drawable.ic_notebook),
										contentDescription = "New chapter",
										modifier = Modifier.requiredSize(24.dp)
									)
								}

								Spacer(modifier = Modifier.height(16.dp))

								FloatingActionButton(
									onClick = { onPerformAction(Action.CLICK_NEW_NOTE, null) }
								) {
									Icon(
										painter = painterResource(id = R.drawable.ic_note),
										contentDescription = "New note",
										modifier = Modifier.requiredSize(24.dp)
									)
								}
							}
						},
						floatingActionButtonPosition = FabPosition.End
					) {
						when (status) {
							Status.INIT -> LoadingView()
							Status.LOADING -> LoadingView()
							Status.LOADED ->
								NotebookScreen(
									noteList = viewModel.noteList.filter { it.chapterPath == viewModel.chapterPath }
										.filter {
											when {
												showArchived && showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.isArchived && it.isFavourite && it.isLocked
												showArchived && showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.isArchived && it.isFavourite
												showArchived && showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.isArchived && it.isFavourite && !it.isLocked
												showArchived && !showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.isArchived && it.isLocked
												showArchived && !showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.isArchived
												showArchived && !showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.isArchived && !it.isLocked
												!showArchived && showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.isArchived && it.isFavourite && it.isLocked
												!showArchived && showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.isArchived && it.isFavourite
												!showArchived && showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> !it.isArchived && it.isFavourite && !it.isLocked
												!showArchived && !showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.isArchived && it.isLocked
												!showArchived && !showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.isArchived
												!showArchived && !showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> !it.isArchived && !it.isLocked
												else -> !it.isArchived && !it.isLocked
											}
										},
									chapterList = viewModel.chapterList.filter { it.chapterPath == viewModel.chapterPath },
									selectedItemList = activityState.selectedItemList,
									showNotes = activityState.showNotes.value,
									showChapters = activityState.showChapters.value,
								) { action, data -> onPerformAction(action, data) }
							Status.ERROR -> null
						}

						DeleteDialog(
							showDeleteDialog = activityState.showDeleteDialog.value,
							selectedItemSize = activityState.selectedItemList.size,
							onDismiss = { activityState.showDeleteDialog.value = false },
							onDelete = {
								val selectedItemList = activityState.selectedItemList.toList()
								viewModel.deleteNote(selectedItemList)
								viewModel.deleteChapter(selectedItemList)

								Toast.makeText(
									this,
									"${if (selectedItemList.size == 1) "1 entry" else "${selectedItemList.size} entries"} deleted",
									Toast.LENGTH_SHORT
								).show()
								activityState.selectedItemList.clear()
								activityState.isSelected.value = false
								activityState.showDeleteDialog.value = false
							},
						)
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val scope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
	) {
		var bottomSheetType: MutableState<BottomSheetType> =
			mutableStateOf(BottomSheetType.NewNoteBottomSheet)
		var selectedItemList: SnapshotStateList<String> = mutableStateListOf()

		var isSelected = mutableStateOf(false)
		var showDeleteDialog = mutableStateOf(false)
		var showNotes = mutableStateOf(true)
		var showChapters = mutableStateOf(true)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showLocked = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun rememberActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember { ActivityState(coroutineScope, bottomSheetState) }

	enum class Action {
		BACK,
		MENU,
		SHOW_DELETE,
		CLICK_NEW_CHAPTER,
		ON_NEW_CHAPTER,
		CLICK_NEW_NOTE,
		ON_NEW_NOTE,
		CLICK_NOTE_HEADER,
		CLICK_NOTE,
		LONG_CLICK_NOTE,
		CLICK_CHAPTER_HEADER,
		CLICK_CHAPTER,
		LONG_CLICK_CHAPTER,
		NAVIGATE_CHAPTER,
		ATTACHMENT,
		SET_AS_DEFAULT,
		ATLAS,
		EDIT_NOTEBOOK,
		UPDATE_NOTEBOOK,
		VAULT,
		TOGGLE_FAVOURITE,
		TOGGLE_ARCHIVED,
	}
}
