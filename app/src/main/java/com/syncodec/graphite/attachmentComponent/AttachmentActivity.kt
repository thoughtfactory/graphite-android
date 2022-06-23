package com.syncodec.graphite.attachmentComponent

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.attachmentComponent.miscellaneous.TopBar
import com.syncodec.graphite.attachmentComponent.modalBottomSheet.BottomSheetType
import com.syncodec.graphite.attachmentComponent.modalBottomSheet.SheetLayout
import com.syncodec.graphite.attachmentComponent.screen.AttachmentScreen
import com.syncodec.graphite.attachmentComponent.screen.AttachmentViewerScreen
import com.syncodec.graphite.custom.DeleteDialog
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.ui.theme.GraphiteBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class AttachmentActivity : ComponentActivity() {
	private val viewModel by viewModels<AttachmentViewModel>()

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.hasExtra(Konstant.Companion.Konstant.IS_NOTE.name).also {
			if (it) {
				intent.getBooleanExtra(Konstant.Companion.Konstant.IS_NOTE.name, false).also {
					if (it) {
						viewModel.isNote = it
						intent.getStringExtra(Konstant.Companion.Konstant.NOTE_KEY.name).also {
							if (it != null) {
								viewModel.key = it
								viewModel.initData()
							} else {
								finish()
							}
						}
					} else {
						intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name).also {
							if (it == null) {
								finish()
							} else {
								viewModel.key = it
								viewModel.initData()
							}
						}
					}
				}
			} else {
				finish()
			}
		}

		setContent {
			viewModel.activityState = rememberActivityState()

			GraphiteBase {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)
				Screen()
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.activityState.isSelected.value) {
			viewModel.activityState.selectedItemList.clear()
			viewModel.activityState.isSelected.value = false
		} else if (viewModel.activityState.isViewer.value) {
			viewModel.activityState.isViewer.value = false
		} else {
			super.onBackPressed()
		}
	}

	fun saveImageToDownloadFolder(imageFile: String, ibitmap: Bitmap) {
		try {
			val filePath = File(
				getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
				imageFile
			)
			val outputStream: OutputStream = FileOutputStream(filePath)
			ibitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
			outputStream.flush()
			outputStream.close()
			Toast.makeText(
				this@AttachmentActivity,
				imageFile + "Sucessfully saved in Download Folder",
				Toast.LENGTH_SHORT
			).show()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
	private fun onPerformAction(action: Action, data: Any?) {
		val activityState = viewModel.activityState

		when (action) {
			Action.BACK -> onBackPressed()
			Action.MENU -> {
				activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
				activityState.scope.launch { activityState.bottomSheetState.show() }
			}
			Action.OPEN_NOTE -> {
				Intent(this, NoteActivity::class.java).apply {
					putExtra(
						Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
						viewModel.attachmentList[activityState.pagerState.currentPage].first.notebookKey
					)
					putStringArrayListExtra(
						Konstant.Companion.Konstant.CHAPTER_KEY.name,
						ArrayList(viewModel.attachmentList[activityState.pagerState.currentPage].first.chapterPath)
					)
					putExtra(
						Konstant.Companion.Konstant.NOTE_KEY.name,
						viewModel.attachmentList[activityState.pagerState.currentPage].first.noteKey
					)
					putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, true)
					putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
					startActivity(this)
				}
			}
			Action.SHOW_DELETE -> null
			Action.CLICK_ATTACHMENT -> {
				data as Pair<*, *>
				if (activityState.isSelected.value) {
					if ((data.second as AttachmentDbEntry).key in activityState.selectedItemList) {
						activityState.selectedItemList.remove((data.second as AttachmentDbEntry).key)
					} else {
						activityState.selectedItemList.add((data.second as AttachmentDbEntry).key)
					}
				} else {
					activityState.isViewer.value = true
					activityState.scope.launch {
						while (true) {
							if (activityState.pagerState.pageCount != 0) {
								activityState.pagerState.scrollToPage(data.first as Int)
								activityState.listState.scrollToItem(data.first as Int)
								break
							} else {
								delay(100)
							}
						}
					}
				}
			}
			Action.LONG_CLICK_ATTACHMENT -> {
				data as AttachmentDbEntry

				activityState.isSelected.value = true
				if (data.key in activityState.selectedItemList) {
					activityState.selectedItemList.remove(data.key)
				} else {
					activityState.selectedItemList.add(data.key)
				}
			}
			Action.CLICK_PREVIEW -> {
				data as Int
				activityState.scope.launch {
					activityState.pagerState.animateScrollToPage(data)
				}
			}
			Action.ON_PREVIOUS -> {

			}
			Action.ON_NEXT -> {

			}
			Action.DELETE -> null
			Action.SHARE -> null
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
		ExperimentalMaterialApi::class, ExperimentalPagerApi::class
	)
	@Composable
	private fun Screen() {
		val activityState = viewModel.activityState
		val status by viewModel.status
		val attachmentList = viewModel.attachmentList
		val pagerState = activityState.pagerState

		LaunchedEffect(key1 = pagerState.currentPage + attachmentList.size) {
			snapshotFlow { pagerState.currentPage }.collect {
				activityState.listState.animateScrollToItem(it)
			}
		}

		ModalBottomSheetLayout(
			sheetState = activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = { SheetLayout { action, data -> onPerformAction(action, data) } },
		) {
			Scaffold(
				containerColor = MaterialTheme.colorScheme.background,
				topBar = {
					if (status == Status.LOADED) {
						TopBar(
							isSelected = activityState.isSelected.value,
							selectedItemSize = activityState.selectedItemList.size,
							isViewer = activityState.isViewer.value,
							isFromNote = viewModel.isNote == true
						) { action, data -> onPerformAction(action, data) }
					}
				}
			) {
				Box(modifier = Modifier.padding(it)) {
					when (status) {
						Status.INIT -> LoadingView()
						Status.LOADING -> LoadingView()
						Status.LOADED -> {
							Crossfade(targetState = activityState.isViewer.value) {
								if (it) {
									AttachmentViewerScreen(
										attachmentList = attachmentList,
										pagerState = activityState.pagerState,
										listState = activityState.listState
									) { action, data -> onPerformAction(action, data) }
								} else {
									AttachmentScreen(
										attachmentList = attachmentList,
										selectedItemList = activityState.selectedItemList
									) { action, data -> onPerformAction(action, data) }
								}
							}
						}
						Status.ERROR -> null
					}

					DeleteDialog(
						showDeleteDialog = activityState.showDeleteDialog.value,
						selectedItemSize = activityState.selectedItemList.size,
						onDismiss = { activityState.showDeleteDialog.value = false },
						onDelete = {
//						val selectedItemList = activityState.selectedItemList.toList()
//						viewModel.deleteNote(selectedItemList)
//						viewModel.deleteChapter(selectedItemList)
//
//						Toast.makeText(
//							this,
//							"${if (selectedItemList.size == 1) "1 entry" else "${selectedItemList.size} entries"} deleted",
//							Toast.LENGTH_SHORT
//						).show()
//						activityState.selectedItemList.clear()
//						activityState.isSelected.value = false
//						activityState.showDeleteDialog.value = false
						},
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState @OptIn(ExperimentalPagerApi::class) constructor(
		val scope: CoroutineScope,
		val bottomSheetState: ModalBottomSheetState,
		val pagerState: PagerState,
		val listState: LazyListState
	) {
		var bottomSheetType: MutableState<BottomSheetType> =
			mutableStateOf(BottomSheetType.MenuBottomSheet)
		var selectedItemList: SnapshotStateList<String> = mutableStateListOf()
		var isSelected = mutableStateOf(false)
		var isViewer = mutableStateOf(false)
		var showDeleteDialog = mutableStateOf(false)
		var showNotes = mutableStateOf(true)
		var showChapters = mutableStateOf(true)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showLocked = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
	@Composable
	private fun rememberActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		pagerState: PagerState = rememberPagerState(),
		listState: LazyListState = rememberLazyListState()
	) = remember { ActivityState(coroutineScope, bottomSheetState, pagerState, listState) }

	enum class Action {
		BACK,
		MENU,
		OPEN_NOTE,
		SHOW_DELETE,
		CLICK_ATTACHMENT,
		LONG_CLICK_ATTACHMENT,
		CLICK_PREVIEW,
		ON_PREVIOUS,
		ON_NEXT,
		DELETE,
		SHARE
	}
}
