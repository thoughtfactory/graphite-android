package com.syncodec.graphite.presentation.note

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.custom.dialog.DeleteDialog
import com.syncodec.graphite.presentation.custom.dialog.DiscardDialog
import com.syncodec.graphite.presentation.custom.printer.Printer
import com.syncodec.graphite.presentation.custom.richText.RichTextEditor
import com.syncodec.graphite.presentation.custom.richText.rememberRichTextEditor
import com.syncodec.graphite.presentation.note.composable.dialog.LocationPermissionRationaleDialog
import com.syncodec.graphite.presentation.note.composable.dialog.SetLocationDialog
import com.syncodec.graphite.presentation.note.composable.dialog.chapterSelectorDialog.ChapterSelectorDialog
import com.syncodec.graphite.presentation.note.composable.dialog.printDialog.PrintDialog
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalCompositionPremium
import com.syncodec.graphite.utils.LocalRichTextEditor
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)
		val hasFilter = intent.hasExtra(Extra.Companion.Constant.FILTER.name)

		if (hasIsNew && hasChapterId && hasFilter) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, true)
			val chapterId = intent.getStringExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { ObjectId.from(it) }
			val filterId = intent.getIntExtra(Extra.Companion.Constant.FILTER.name, -1)

			if (filterId < 0) {
				Log.i("npr71", "filterId not provided")
				finish()
			} else {
				val filter = Extra.Companion.Filter.values()[filterId]
				if (isNew && hasFilter) {
					if (chapterId != null) {
						viewModel.initNewData(chapterId, filter)
					} else {
						Log.i("npr71", "chapterId not provided")
						finish()
					}
				} else if (hasNoteId && hasFilter) {
					val noteId = intent.getStringExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { ObjectId.from(it) }

					if (filterId == -1 || chapterId == null || noteId == null) {
						Log.i("npr71", "filterId is -1 || chapterId == null || noteId == null")
						finish()
					} else {
						viewModel.loadAndViewData(chapterId, noteId, filter)
					}

				} else {
					Log.i("npr71", "!isNew || !hasNoteId || !hasFilter")
					finish()
				}
			}
		} else {
			Log.i("npr71", "super finish")
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primary)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.primary)

				val richTextEditor = rememberRichTextEditor()

				richTextEditor.setOnSaveData(
					object : RichTextEditor.OnSaveDataListener {
						override fun onSaveData(data: String) {
							when (viewModel.isNew.value) {
								true -> viewModel.putNote(data = data)
								false -> viewModel.updateNote(data = data)
								null -> null
							}
						}
					}
				)

				richTextEditor.setOnPrintData(
					object : RichTextEditor.OnPrintDataListener {
						override fun onPrintData(data: String) {
//							viewModel.printNote(data = data)

							CoroutineScope(Dispatchers.Main).launch {
								val printer = Printer(this@NoteActivity)
								printer.createWebPrintJob(data)
							}

						}
					}
				)

				var showDeleteDialog by viewModel.showDeleteDialog
				var showDiscardDialog by viewModel.showDiscardDialog
				var showChapterSelectorDialog by viewModel.showChapterSelectorDialog
				var showLocationPermissionRationaleDialog by viewModel.showLocationPermissionRationaleDialog
				var showSetLocationDialog by viewModel.showSetLocationDialog
				var showPrintDialog by viewModel.showPrintDialog

				val noteId by viewModel.noteId
				var selectorParentChapterObject by viewModel.selectorChapterObject
				val selectorChapterList by viewModel.selectorChapterList.collectAsState(initial = listOf())

				CompositionLocalProvider(
					LocalCompositionPremium provides true,
					LocalRichTextEditor provides richTextEditor
				) {
					NoteScreen()

					DeleteDialog(
						showDeleteDialog = showDeleteDialog,
						id = noteId,
						onDismiss = { showDeleteDialog = false }
					) { viewModel.deleteNote() }

					DiscardDialog(
						showDiscardDialog = showDiscardDialog,
						onDismiss = { showDiscardDialog = false }
					) {
						showDiscardDialog = false
						when (viewModel.isNew.value) {
							true -> finish()
							false -> viewModel.isViewer.value = true
							else -> finish()
						}
					}

					selectorParentChapterObject?.let {
						ChapterSelectorDialog(
							showDialog = showChapterSelectorDialog,
							onDismiss = { showChapterSelectorDialog = false },
							parentChapter = it,
							chapterList = selectorChapterList,
							onClickChapter = { selectorParentChapterObject = it },
							onSelectChapter = { viewModel.chapterObject.value = it }
						)
					}

					LocationPermissionRationaleDialog(
						showDialog = showLocationPermissionRationaleDialog,
						onRequestPermission = {
							Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
								addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								this.data = Uri.fromParts("package", packageName, null)
								startActivity(this)
							}
						}
					) { showLocationPermissionRationaleDialog = false }

					SetLocationDialog(
						showDialog = showSetLocationDialog,
						latLng = viewModel.latLng.value,
						address = viewModel.address.value,
						reverseGeocode = { latLng, onAddressAvailable ->
							if (latLng.longitude != null && latLng.latitude != null) {
								viewModel.reverseGeocode(
									latitude = latLng.latitude!!,
									longitude = latLng.longitude!!,
									onAddressAvailable = onAddressAvailable,
									onIoException = {
										Toast.makeText(this, "Unable to get address. Please check your internet connection", Toast.LENGTH_SHORT).show()
									},
									onException = {
										Toast.makeText(this, "Unable to get address.", Toast.LENGTH_SHORT).show()
									}
								)
							} else {
								onAddressAvailable(null)
							}
						},
						onDismiss = { showSetLocationDialog = false }
					) { latLng, address ->
						if (latLng.latitude == null || latLng.longitude == null) {
							Toast.makeText(this, "Error setting location", Toast.LENGTH_SHORT).show()
						} else {
							viewModel.onReceiveLocation(latitude = latLng.latitude!!, longitude = latLng.longitude!!)
							viewModel.onReceiveAddress(address = address)
						}
						showSetLocationDialog = false
					}

					PrintDialog(
						showDialog = showPrintDialog
					) {
						showPrintDialog = false
					}
				}
			}
		}
	}

	override fun onBackPressed() {

		if (viewModel.showChapterSelectorDialog.value) {
			viewModel.showChapterSelectorDialog.value = false
		} else if (viewModel.showSetLocationDialog.value) {
			viewModel.showSetLocationDialog.value = false
		} else {
			if (viewModel.isNew.value == true) {
				if (viewModel.isViewer.value) {
					super.onBackPressed()
				} else {
					viewModel.showDiscardDialog.value = true
				}
			} else if (viewModel.isNew.value == false) {
				if (viewModel.isViewer.value) {
					super.onBackPressed()
				} else {
					viewModel.showDiscardDialog.value = true
				}
			} else {
				super.onBackPressed()
			}
		}

//		if (viewModel.isNew.value == true && viewModel.isViewer.value) {
//			super.onBackPressed()
//		} else if(viewModel.isNew.value == true && !viewModel.isViewer.value) {
//			viewModel.showDiscardDialog.value = true
//		}
//		else if (viewModel.isNew.value == false && viewModel.isViewer.value) {
//			super.onBackPressed()
//		} else if (viewModel.isNew.value == false && !viewModel.isViewer.value) {
//			viewModel.showDiscardDialog.value = true
//		} else if (viewModel.isNew.value == null) {
//			super.onBackPressed()
//		}
	}
}
