package com.syncodec.graphite.presentation.note.screen.editorScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.filePreview.FilePreview.Companion.preview
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.richText.rememberRichTextEditor
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.TopBar
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.BottomBar
import com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet.EditorBottomSheetType
import com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.screen.editorScreen.composable.LocationSnackbarHost
import com.syncodec.graphite.presentation.note.screen.editorScreen.dialog.Dialog
import com.syncodec.graphite.presentation.note.screen.editorScreen.dialog.EditorDialogType
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.getFileName
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.reverseGeocode
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun EditorScreen(
	afterNoteSaved : (RealmUUID) -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : EditorScreenViewModel = koinViewModel()

	val richTextEditor = rememberRichTextEditor()
	val isReady by richTextEditor.isReady.collectAsState()

	val textFormat by richTextEditor.textFormat.collectAsState()

	val isOperationPending by viewModel.isOperationPending.collectAsState()

	val isNewNote by viewModel.isNewNote.collectAsState()

	val noteId by viewModel.noteId.collectAsState()
	val createdTimestamp by viewModel.createdTimestamp.collectAsState()
	val modifiedTimestamp by viewModel.modifiedTimestamp.collectAsState()
	val userTimestamp by viewModel.userTimestamp.collectAsState()
	val title by viewModel.title.collectAsState()
	val content by viewModel.content.collectAsState()
	val latLng by viewModel.latLng.collectAsState()
	val address by viewModel.address.collectAsState()
	val parentChapterObject by viewModel.parentChapter.collectAsState()

	var attachmentListSaved by remember { mutableStateOf<List<File>>(listOf()) }
	var attachmentListToAdd by remember { mutableStateOf<List<Uri>>(listOf()) }
	var attachmentListToRemove by remember { mutableStateOf<List<File>>(listOf()) }

	val tagList by viewModel.tagList.collectAsState()
	val tagListSaved by viewModel.tagListSaved.collectAsState()
	val tagListToAdd by viewModel.tagListToAdd.collectAsState()
	val tagListToRemove by viewModel.tagListToRemove.collectAsState()

	val onNoteSaved by viewModel.onNoteSaved.collectAsState()

	val locationDataState by viewModel.locationDataState.collectAsState()

	val locationSnackbarHostState = SnackbarHostState()

	val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	var sheetType by remember { mutableStateOf(EditorBottomSheetType.METADATA) }
	fun openSheet(type : EditorBottomSheetType) {
		scope.launch { sheetType = type; modalBottomSheetState.show() }
	}

	var isDatePickerDialogVisible by remember { mutableStateOf(false) }
	var isTimePickerDialogVisible by remember { mutableStateOf(false) }
	var isLocationPermissionDialogVisible by remember { mutableStateOf(false) }

	fun openDialog(editorDialogType : EditorDialogType) = when (editorDialogType) {
		EditorDialogType.DatePicker -> isDatePickerDialogVisible = true
		EditorDialogType.TimePicker -> isTimePickerDialogVisible = true
		EditorDialogType.LocationPermission -> isLocationPermissionDialogVisible = true
	}

	fun closeDialog(editorDialogType : EditorDialogType) = when (editorDialogType) {
		EditorDialogType.DatePicker -> isDatePickerDialogVisible = false
		EditorDialogType.TimePicker -> isTimePickerDialogVisible = false
		EditorDialogType.LocationPermission -> isLocationPermissionDialogVisible = false
	}

	var locationCoroutine : CoroutineScope? = null
	var locationCancellationSource : CancellationTokenSource? = null
	fun getLocationFromHardware(silent : Boolean = false) {
		scope.launch(Dispatchers.IO) {
			locationCancellationSource?.cancel()
			locationCoroutine?.cancel()
			locationCancellationSource = CancellationTokenSource()
			locationCoroutine = this

			val fusedLocationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
			) {
				if (! silent) openDialog(EditorDialogType.LocationPermission)
				return@launch
			}
			fusedLocationClient
				.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource?.token)
				.addOnSuccessListener { location ->
					LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
					context.reverseGeocode(
						latitude = location.latitude,
						longitude = location.longitude,
						onAddressAvailable = { address ->
							address?.let { address1 ->
								locationAddressFilter(address = address1).let { receivedAddress ->
									scope.launch(Dispatchers.Main) {
										val message = if (receivedAddress.isNullOrEmpty()) "Lat : ${location.latitude}\nLng : ${location.longitude}"
										else "${receivedAddress}\nLat : ${location.latitude}\nLng : ${location.longitude}"
										locationSnackbarHostState.showSnackbar(
											message = message,
											duration = SnackbarDuration.Short
										)
									}

									LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = receivedAddress) }
								}
							} ?: scope.launch(Dispatchers.Main) { Toast.makeText(context, "Error getting address", Toast.LENGTH_SHORT).show() }
						},
						onIoException = {
							LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
							scope.launch(Dispatchers.Main) {
								locationSnackbarHostState.showSnackbar(
									message = "Lat : ${location?.latitude}\nLng : ${location?.longitude}",
									duration = SnackbarDuration.Short
								)
							}
						},
						onException = {
							LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
							scope.launch(Dispatchers.Main) {
								locationSnackbarHostState.showSnackbar(
									message = "Lat : ${location?.latitude}\nLng : ${location?.longitude}",
									duration = SnackbarDuration.Short
								)
							}
						}
					)
				}
		}
	}

	fun saveAttachments(noteId : RealmUUID, attachmentListToAdd : List<Uri>, attachmentListToRemove : List<File>) {
		scope.launch(Dispatchers.IO) {
			val attachmentDir = File(context.attachmentDirPath(noteId = noteId)).also { it.mkdirs() }
			attachmentListToRemove.forEach { file -> if (file.exists()) file.delete() }
			attachmentListToAdd.forEach { uri ->
				val inputStream = context.contentResolver.openInputStream(uri)?.also { inputStream ->
					val fileName = uri.getFileName(context) ?: RealmUUID.random().toString()
					val file = File(attachmentDir, fileName).also { it.createNewFile() }
					context.contentResolver.openOutputStream(Uri.fromFile(file))
						?.use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
				}
				inputStream?.close()
			}
			attachmentDir.listFiles()?.forEach { file ->
				val previewBitmap = file.preview(context = context).first
				previewBitmap?.let {
					viewModel.putAttachmentThumbnail(noteId = noteId, thumbnail = it)
					return@forEach
				}
			}
		}
	}

	LaunchedEffect(key1 = noteId) {
		noteId?.let { noteId ->
			val attachmentDir = File(context.attachmentDirPath(noteId = noteId))
			if (attachmentDir.exists()) attachmentDir.listFiles().let { attachmentListSaved = it?.toList() ?: listOf() }
		}
	}

	LaunchedEffect(key1 = isReady) {
		if (isReady) {
			richTextEditor.setGetTextListener(
				object : RichTextEditor.GetTextListener {
					override fun onGetData(requestData : RichTextEditor.Companion.RequestData, data : String?) {
						when (requestData) {
							RichTextEditor.Companion.RequestData.Save -> {
								viewModel.setContent(data = data).let {
									if (! it) scope.launch(Dispatchers.Main) { Toast.makeText(context, "Error saving content", Toast.LENGTH_SHORT).show() }
									else viewModel.saveNote()
								}
							}

							RichTextEditor.Companion.RequestData.Share -> null
							RichTextEditor.Companion.RequestData.ExportText -> null
							RichTextEditor.Companion.RequestData.ExportPdf -> null
							RichTextEditor.Companion.RequestData.ExportHtml -> null
							RichTextEditor.Companion.RequestData.ExportMarkdown -> null
						}
					}
				}
			)
			richTextEditor.setData(title = title, content = content)
		}
	}

	LaunchedEffect(key1 = isNewNote) {
		if (isNewNote) getLocationFromHardware(silent = true)
	}

	LaunchedEffect(key1 = onNoteSaved.hashCode(), key2 = noteId) {
		noteId?.let {
			if (onNoteSaved) {
				saveAttachments(noteId = it, attachmentListToAdd = attachmentListToAdd, attachmentListToRemove = attachmentListToRemove)
				if (isNewNote) viewModel.isNewNote.tryEmit(false)
				viewModel.onNoteSaved.tryEmit(false)
				afterNoteSaved(it)
				viewModel.isOperationPending.tryEmit(false)
			}
		}
	}

	GenericScaffold(
		topBar = {
			TopBar(
				isOperationPending = false,
				onSave = richTextEditor::save,
				onClickBack = {
					if (isOperationPending) Toast.makeText(context, "Operation pending", Toast.LENGTH_SHORT).show()
					else onClickBack()
				},
			)
		},
		modalBottomSheetState = modalBottomSheetState,
		sheetContent = {
			SheetLayout(
				bottomSheetType = sheetType,
				noteId = noteId,
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp,
				latLng = latLng,
				address = address,
				parentChapterObject = parentChapterObject,
				locationDataState = locationDataState,
				attachmentListSaved = attachmentListSaved,
				attachmentListToAdd = attachmentListToAdd,
				attachmentListToRemove = attachmentListToRemove,
				tagList = tagList,
				tagListSaved = tagListSaved,
				tagListToAdd = tagListToAdd,
				tagListToRemove = tagListToRemove,
				onClickSelectParentChapter = {},
				onAddAttachmentToBuffer = { attachmentListToAdd.toMutableList().apply { addAll(it);attachmentListToAdd = this } },
				onRemoveBufferedAttachment = { attachmentListToAdd.toMutableList().apply { remove(it);attachmentListToAdd = this } },
				onRemoveSavedAttachment = {
					if (it in attachmentListToRemove) attachmentListToRemove.toMutableList().apply { remove(it);attachmentListToRemove = this }
					else attachmentListToRemove.toMutableList().apply { add(it);attachmentListToRemove = this }
				},
				onAddTagToBuffer = {
					tagListToAdd.toMutableList().apply {
						add(it)
						viewModel.tagListToAdd.tryEmit(this)
					}
				},
				onRemoveBufferedTag = {
					tagListToAdd.toMutableList().apply {
						remove(it)
						viewModel.tagListToAdd.tryEmit(this)
					}
				},
				onRemoveSavedTag = {
					if (it in tagListToRemove) tagListToRemove.toMutableList().apply {
						remove(it)
						viewModel.tagListToRemove.tryEmit(this)
					}
					else tagListToRemove.toMutableList().apply {
						add(it)
						viewModel.tagListToRemove.tryEmit(this)
					}
				},
				onRemoveLocation = viewModel::removeLocation,
				onReloadLocation = ::getLocationFromHardware,
			)
		},
		dialogContent = {
			Dialog(
				userTimestamp = userTimestamp,
				isDatePickerDialogVisible = isDatePickerDialogVisible,
				isTimePickerDialogVisible = isTimePickerDialogVisible,
				isLocationPermissionDialogVisible = isLocationPermissionDialogVisible,
				setUserTimestamp = viewModel::setUserTimestamp,
				openDialog = ::openDialog,
				closeDialog = ::closeDialog,
			)
		},
		snackbarHost = {
			SnackbarHost(hostState = locationSnackbarHostState) {
				LocationSnackbarHost(snackbarData = it)
			}
		},
		overlayContent = {
			AnimatedVisibility(
				visible = isOperationPending,
				enter = fadeIn(tween(300)),
				exit = fadeOut(tween(300)),
				modifier = Modifier
					.fillMaxSize()
					.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
			) {
				LoadingView()
			}
		}
	) {
		Crossfade(
			targetState = isReady,
			animationSpec = tween(300),
			modifier = Modifier.fillMaxSize()
		) {
			if (it) {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
//			    	!!! AndroidView is wrapped in a box because of a bug in Compose or maybe defined height is set in TipTap and is using same height for the view.
//				    !!! Modifier.weight(1f) is not working on correctly for AndroidView and is filling available height.
					Box(
						modifier = Modifier.weight(1f)
					) {
						AndroidView(
							factory = { richTextEditor },
							modifier = Modifier.fillMaxSize()
						)
					}
					BottomBar(
						textFormat = textFormat,
						userTimestamp = userTimestamp,
						onClickTimePicker = { openDialog(EditorDialogType.DatePicker) },
						onClickMetadata = { openSheet(EditorBottomSheetType.METADATA) },
						onClickLocation = { openSheet(EditorBottomSheetType.LOCATION) },
						onClickAttachment = { openSheet(EditorBottomSheetType.ATTACHMENT) },
						onClickTag = { openSheet(EditorBottomSheetType.TAG) },
					) { editorAction -> richTextEditor.onEditorAction(editorAction = editorAction) }
				}
			} else LoadingView()
		}
	}
}
