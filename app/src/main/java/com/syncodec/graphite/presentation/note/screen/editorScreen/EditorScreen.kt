package com.syncodec.graphite.presentation.note.screen.editorScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDir
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.TopBar
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.BottomBar
import com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet.EditorBottomSheetType
import com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.screen.editorScreen.dialog.Dialog
import com.syncodec.graphite.presentation.note.screen.editorScreen.dialog.EditorDialogType
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.FilePreview.Companion.preview
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.reverseGeocode
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun EditorScreen(
	editor : RichTextEditor,
	afterNoteSaved : (RealmUUID) -> Unit = {},
	onClickBack : (Boolean) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : EditorScreenViewModel = koinViewModel()

	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val isGeolocationEnabled by dataStoreInstance.getGeolocation.collectAsState(initial = null)

	val isOperationPending by viewModel.isOperationPending.collectAsState()

	val textFormat by editor.textFormat.collectAsState()

	val isNewNote by viewModel.isNewNote.collectAsState()

	val noteId by viewModel.noteId.collectAsState()
	val createdTimestamp by viewModel.createdTimestamp.collectAsState()
	val modifiedTimestamp by viewModel.modifiedTimestamp.collectAsState()
	val userTimestamp by viewModel.userTimestamp.collectAsState()
	val title by viewModel.title.collectAsState()
	val content by viewModel.content.collectAsState()
	val parentChapterObject by viewModel.parentChapter.collectAsState()

	var attachmentListSaved by remember { mutableStateOf<List<File>>(listOf()) }
	var attachmentListToAdd by remember { mutableStateOf<List<Uri>>(listOf()) }
	var attachmentListToRemove by remember { mutableStateOf<List<File>>(listOf()) }

	val tagList by viewModel.tagList.collectAsState()
	val tagListSaved by viewModel.tagListSaved.collectAsState()
	val tagListToAdd by viewModel.tagListToAdd.collectAsState()
	val tagListToRemove by viewModel.tagListToRemove.collectAsState()

	val onNoteSaved by viewModel.onNoteSaved.collectAsState()

	val locationData by viewModel.locationDataState.collectAsState()

	val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	var sheetType by remember { mutableStateOf(EditorBottomSheetType.Metadata) }
	fun openSheet(type : EditorBottomSheetType) {
		scope.launch { sheetType = type; modalBottomSheetState.show() }
	}

	var isDatePickerDialogVisible by remember { mutableStateOf(false) }
	var isTimePickerDialogVisible by remember { mutableStateOf(false) }
	var isLocationPermissionDialogVisible by remember { mutableStateOf(false) }
	var isLocationPickerDialogVisible by remember { mutableStateOf(false) }
	var isWhereDialogVisible by remember { mutableStateOf(false) }
	var isDiscardChangesDialogVisible by remember { mutableStateOf(false) }

	fun openDialog(editorDialogType : EditorDialogType) = when (editorDialogType) {
		EditorDialogType.DatePicker -> isDatePickerDialogVisible = true
		EditorDialogType.TimePicker -> isTimePickerDialogVisible = true
		EditorDialogType.LocationPermission -> isLocationPermissionDialogVisible = true
		EditorDialogType.LocationPicker -> isLocationPickerDialogVisible = true
		EditorDialogType.Where -> isWhereDialogVisible = true
		EditorDialogType.DiscardChanges -> isDiscardChangesDialogVisible = true
	}

	fun closeDialog(editorDialogType : EditorDialogType) = when (editorDialogType) {
		EditorDialogType.DatePicker -> isDatePickerDialogVisible = false
		EditorDialogType.TimePicker -> isTimePickerDialogVisible = false
		EditorDialogType.LocationPermission -> isLocationPermissionDialogVisible = false
		EditorDialogType.LocationPicker -> isLocationPickerDialogVisible = false
		EditorDialogType.Where -> isWhereDialogVisible = false
		EditorDialogType.DiscardChanges -> isDiscardChangesDialogVisible = false
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
				viewModel.setLocationPermissionUnavailable()
				if (! silent) openDialog(EditorDialogType.LocationPermission)
				return@launch
			}
			fusedLocationClient
				.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource?.token)
				.addOnSuccessListener { location ->
					try {
						LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
						context.reverseGeocode(
							latitude = location.latitude,
							longitude = location.longitude,
							onAddressAvailable = { address ->
								address?.let { address1 ->
									locationAddressFilter(address = address1).let { receivedAddress ->
										LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = receivedAddress) }
									}
								} ?: scope.launch(Dispatchers.Main) { Toast.makeText(context, "Error getting address", Toast.LENGTH_SHORT).show() }
							},
							onIoException = {
								LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
							},
							onException = {
								LatLng(location.latitude, location.longitude).let { viewModel.setLocation(latLng = it, address = null) }
							}
						)
					} catch (e : Exception) {
						viewModel.setLocation(null, null)
					}
				}
		}
	}

	fun saveAttachments(noteId : RealmUUID, attachmentListToAdd : List<Uri>, attachmentListToRemove : List<File>) {
		scope.launch(Dispatchers.IO) {
			val attachmentDir = context.attachmentDir(parentId = noteId, true)
			viewModel.putAttachment(noteId, attachmentListToAdd, attachmentListToRemove)
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
			val attachmentDir = context.attachmentDir(parentId = noteId)
			if (attachmentDir.exists()) attachmentDir.listFiles().let { attachmentListSaved = it?.toList() ?: listOf() }
		}
	}

	val getTextListener = remember {
		object : RichTextEditor.GetTextListener {
			override fun onGetData(
				requestData : RichTextEditor.Companion.RequestData,
				dataObject : JSONObject,
				dataJson : JSONObject?,
				dataText : String?,
				dataHtml : String?,
				dataMarkdown : String?,
				dataTitle : String?
			) {
				when (requestData) {
					RichTextEditor.Companion.RequestData.Save -> {
						viewModel.setContent(dataJson = dataJson, dataText = dataText, title = dataTitle).let {
							if (! it) scope.launch(Dispatchers.Main) { Toast.makeText(context, "Error saving content", Toast.LENGTH_SHORT).show() }
							else viewModel.saveNote()
						}
					}

					else -> null
				}
			}
		}
	}

	LaunchedEffect(key1 = title, key2 = content) {
		editor.setGetTextListener(getTextListener)
		editor.setData(title, content)
	}

	LaunchedEffect(key1 = isNewNote, key2 = isGeolocationEnabled) {
		if (isNewNote && isGeolocationEnabled != false) getLocationFromHardware(silent = true)
		else viewModel.setLocation(null, null)
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

	BackHandler(enabled = true) {
		if (isOperationPending) Toast.makeText(context, "Operation pending", Toast.LENGTH_SHORT).show()
		else openDialog(EditorDialogType.DiscardChanges)
	}

	GenericScaffold(
		topBar = {
			TopBar(
				isOperationPending = false,
				onSave = { editor.save() },
				onClickBack = {
					if (isOperationPending) Toast.makeText(context, "Operation pending", Toast.LENGTH_SHORT).show()
					else onBackPressedDispatcher?.onBackPressed()
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
				parentChapterObject = parentChapterObject,
				locationData = locationData,
				attachmentListSaved = attachmentListSaved,
				attachmentListToAdd = attachmentListToAdd,
				attachmentListToRemove = attachmentListToRemove,
				tagList = tagList,
				tagListSaved = tagListSaved,
				tagListToAdd = tagListToAdd,
				tagListToRemove = tagListToRemove,
				onClickSelectParentChapter = { openDialog(EditorDialogType.Where) },
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
				onSetLocationManually = { openDialog(EditorDialogType.LocationPicker) },
			)
		},
		dialogContent = {
			Dialog(
				userTimestamp = userTimestamp,
				isDatePickerDialogVisible = isDatePickerDialogVisible,
				isTimePickerDialogVisible = isTimePickerDialogVisible,
				isLocationPermissionDialogVisible = isLocationPermissionDialogVisible,
				isLocationPickerDialogVisible = isLocationPickerDialogVisible,
				isWhereDialogVisible = isWhereDialogVisible,
				isDiscardChangesDialogVisible = isDiscardChangesDialogVisible,
				setUserTimestamp = viewModel::setUserTimestamp,
				setParentChapter = {
					it?.id?.let { viewModel.setParentChapter(it) } ?: Toast.makeText(context, "Parent chapter cannot be empty", Toast.LENGTH_SHORT).show()
				},
				setLocation = { latLng, address ->
					locationCancellationSource?.cancel()
					locationCoroutine?.cancel()
					viewModel.setLocation(latLng, address)
					closeDialog(EditorDialogType.LocationPicker)
				},
				onDiscardChanges = { onClickBack(isNewNote) },
				openDialog = ::openDialog,
				closeDialog = ::closeDialog,
			)
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
		Column(
			modifier = Modifier.fillMaxSize()
		) {
//		    !!! AndroidView is wrapped in a box because of a bug in Compose or maybe defined height is set in TipTap and is using same height for the view.
//		    !!! Modifier.weight(1f) is not working on correctly for AndroidView and is filling available height.
			Box(
				modifier = Modifier.weight(1f)
			) {
				AndroidView(
					factory = { editor },
					modifier = Modifier.fillMaxSize()
				)
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.background(
						MaterialTheme.colorScheme
							.surfaceColorAtElevation(8.dp)
							.copy(alpha = 0.47f)
					)
			) {
				AnimatedText(
//					text = if (isGeolocationEnabled != false) address ?: latLng?.toString() ?: "No location" else "Location disabled",
					text = locationData.let {
						when (it) {
							is LocationData.Init -> "Initiating location"
							is LocationData.Loading -> "Loading location"
							is LocationData.SuccessOnlyLatLng -> it.latLng.toString()
							is LocationData.SuccessOnlyAddress -> it.address
							is LocationData.Success -> it.address
							is LocationData.SuccessNoData -> "No location"
							is LocationData.NoPermission -> "Location permission not granted"
							is LocationData.Error -> "Error loading location"
						}
					},
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(12.dp, 8.dp),
				)
			}
			BottomBar(
				textFormat = textFormat,
				userTimestamp = userTimestamp,
				onClickTimePicker = { openDialog(EditorDialogType.DatePicker) },
				onClickMetadata = { openSheet(EditorBottomSheetType.Metadata) },
				onClickLocation = { openSheet(EditorBottomSheetType.Location) },
				onClickAttachment = { openSheet(EditorBottomSheetType.Attachment) },
				onClickTag = { openSheet(EditorBottomSheetType.Tag) },
			) { editorAction -> editor.onEditorAction(editorAction = editorAction) }
		}
	}
}
