package com.syncodec.graphite.presentation.note.screen.viewerScreen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
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
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDir
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bar.BottomBar
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bar.TopBar
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bottomSheet.ViewerBottomSheetType
import com.syncodec.graphite.presentation.note.screen.viewerScreen.composable.ViewerComponent
import com.syncodec.graphite.presentation.note.screen.viewerScreen.dialog.Dialog
import com.syncodec.graphite.presentation.note.screen.viewerScreen.dialog.ViewerDialogType
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.share
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun ViewerScreen(
	onClickEditNote : () -> Unit = {},
	onNoteDeleted : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : ViewerScreenViewModel = koinViewModel()

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val isOperationPending by viewModel.isOperationPending.collectAsState()

	val noteId by viewModel.noteId.collectAsState()
	val createdTimestamp by viewModel.createdTimestamp.collectAsState()
	val modifiedTimestamp by viewModel.modifiedTimestamp.collectAsState()
	val userTimestamp by viewModel.userTimestamp.collectAsState()
	val title by viewModel.title.collectAsState()
	val latLng by viewModel.latLng.collectAsState()
	val address by viewModel.address.collectAsState()
	val content by viewModel.content.collectAsState()
	val contentThumbnail by viewModel.contentThumbnail.collectAsState()
	val isFavourite by viewModel.isFavourite.collectAsState()
	val isLocked by viewModel.isLocked.collectAsState()
	val parentChapter by viewModel.parentChapter.collectAsState()

	val tagList by viewModel.tagList.collectAsState()

	var attachmentList by remember { mutableStateOf<List<File>>(listOf()) }

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
					RichTextEditor.Companion.RequestData.Save -> null
					RichTextEditor.Companion.RequestData.Share -> null
					RichTextEditor.Companion.RequestData.ExportText -> {
						File(File(context.cacheDir, "export"), "$noteId.txt").let {
							it.mkdirs()
							it.delete()
							it.createNewFile()
							it.writeText(dataText ?: "")
							it.share(context = context)
						}
					}

					RichTextEditor.Companion.RequestData.ExportPdf -> null
					RichTextEditor.Companion.RequestData.ExportHtml -> {
						File(File(context.cacheDir, "export"), "$noteId.html").let {
							it.mkdirs()
							it.delete()
							it.createNewFile()
							it.writeText(dataHtml ?: "")
							it.share(context = context)
						}
					}

					RichTextEditor.Companion.RequestData.ExportMarkdown -> {
						File(File(context.cacheDir, "export"), "$noteId.md").let {
							it.mkdirs()
							it.delete()
							it.createNewFile()
							it.writeText(dataMarkdown ?: "")
							it.share(context = context)
						}
					}

					RichTextEditor.Companion.RequestData.ExportJson -> {
						File(File(context.cacheDir, "export"), "$noteId.json").let {
							it.mkdirs()
							it.delete()
							it.createNewFile()
							it.writeText(dataJson?.toString() ?: "")
							it.share(context = context)
						}
					}

					RichTextEditor.Companion.RequestData.ImportJourney -> null
				}
			}
		}
	}

	val richTextEditor = remember {
		RichTextEditor.headlessInstance(context).apply {
			setGetTextListener(getTextListener)
		}
	}

	//	TODO: Observe changes in attachment directory
	fun getAttachments(noteId : RealmUUID) {
		scope.launch(Dispatchers.IO) {
			withContext(Dispatchers.Main) {
				attachmentList = context.attachmentDir(parentId = noteId).listFiles()?.toList() ?: listOf()
			}
		}
	}

	LaunchedEffect(key1 = noteId) { noteId?.let { getAttachments(it) } }

	val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	var sheetType by remember { mutableStateOf(ViewerBottomSheetType.Metadata) }
	fun openSheet(type : ViewerBottomSheetType) {
		scope.launch { sheetType = type; modalBottomSheetState.show() }
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	var isDatePickerDialogVisible by remember { mutableStateOf(false) }
	var isTimePickerDialogVisible by remember { mutableStateOf(false) }
	var isWhereDialogVisible by remember { mutableStateOf(false) }
	var isDeleteDialogVisible by remember { mutableStateOf(false) }
	var isNotificationPermissionDialogVisible by remember { mutableStateOf(false) }
	fun openDialog(dialogType : ViewerDialogType) = when (dialogType) {
		ViewerDialogType.DatePicker -> isDatePickerDialogVisible = true
		ViewerDialogType.TimePicker -> isTimePickerDialogVisible = true
		ViewerDialogType.Where -> isWhereDialogVisible = true
		ViewerDialogType.Delete -> isDeleteDialogVisible = true
		ViewerDialogType.NotificationPermission -> isNotificationPermissionDialogVisible = true
	}

	fun closeDialog(dialogType : ViewerDialogType) = when (dialogType) {
		ViewerDialogType.DatePicker -> isDatePickerDialogVisible = false
		ViewerDialogType.TimePicker -> isTimePickerDialogVisible = false
		ViewerDialogType.Where -> isWhereDialogVisible = false
		ViewerDialogType.Delete -> isDeleteDialogVisible = false
		ViewerDialogType.NotificationPermission -> isNotificationPermissionDialogVisible = false
	}

	fun blockOnOperationPending(block : () -> Unit) {
		if (isOperationPending) Toast.makeText(context, "Please wait for the operation to complete", Toast.LENGTH_SHORT).show()
		else block()
	}

	fun pinToNotification() {
		noteId?.let {
			NotePinNotification.pinToNotification(
				context = context,
				noteId = it,
				title = title,
				content = contentThumbnail ?: "No content",
			) { openDialog(ViewerDialogType.NotificationPermission) }
		}
	}

	GenericScaffold(
		topBar = {
			TopBar(
				isOperationPending = isOperationPending,
				isFavourite = isFavourite ?: false,
				isLocked = isLocked ?: false,
				onClickFavourite = viewModel::toggleFavourite,
				onClickLock = {
					if (isAuthenticated) viewModel.toggleLock()
					else onAuthenticationAction(AuthenticatorScreen.Authenticate)
				},
				onClickPin = ::pinToNotification,
				onClickDelete = { blockOnOperationPending { openDialog(ViewerDialogType.Delete) } },
				onClickBack = onClickBack,
			)
		},
		bottomBar = {
			BottomBar(
				onClickExportAsTxt = { richTextEditor.setAndGetData(title ?: "", content ?: "", RichTextEditor.Companion.RequestData.ExportText.name) },
				onClickExportAsPdf = { richTextEditor.setAndGetData(title ?: "", content ?: "", RichTextEditor.Companion.RequestData.ExportPdf.name) },
				onClickExportAsHtml = { richTextEditor.setAndGetData(title ?: "", content ?: "", RichTextEditor.Companion.RequestData.ExportHtml.name) },
				onClickExportAsJson = { richTextEditor.setAndGetData(title ?: "", content ?: "", RichTextEditor.Companion.RequestData.ExportJson.name) },
				onClickExportAsMarkdown = {
					richTextEditor.setAndGetData(
						title ?: "",
						content ?: "",
						RichTextEditor.Companion.RequestData.ExportMarkdown.name
					)
				},
				onClickExportAttachments = { attachmentList.share(context = context) },
				onClickMetadata = { openSheet(ViewerBottomSheetType.Metadata) },
				onClickEditNote = { blockOnOperationPending { onClickEditNote() } },
			)
		},
		modalBottomSheetState = modalBottomSheetState,
		sheetContent = {
			SheetLayout(
				bottomSheetType = sheetType,
				noteId = noteId,
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp,
			)
		},
		dialogContent = {
			Dialog(
				userTimestamp = userTimestamp,
				isDatePickerDialogVisible = isDatePickerDialogVisible,
				isTimePickerDialogVisible = isTimePickerDialogVisible,
				isWhereDialogVisible = isWhereDialogVisible,
				isDeleteDialogVisible = isDeleteDialogVisible,
				isNotificationPermissionDialogVisible = isNotificationPermissionDialogVisible,
				parentChapter = parentChapter?.toLite(),
				setUserTimestamp = viewModel::setUserTimestamp,
				setParentChapter = {
					blockOnOperationPending {
						it?.let { viewModel.setParentChapter(it) } ?: Toast.makeText(context, "Parent chapter cannot be null", Toast.LENGTH_SHORT).show()
					}
				},
				onDelete = {
					blockOnOperationPending {
						noteId?.let {
							viewModel.deleteNote(id = it) {
								withContext(Dispatchers.Main) { Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show() }
								onNoteDeleted()
							}
						}
					}
				},
				onNotificationPermissionAvailable = { pinToNotification() },
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
		Crossfade(
			targetState = noteId,
			animationSpec = tween(300)
		) {
			it?.let {
				ViewerComponent(
					noteId = it,
					content = content,
					title = title,
					userTimestamp = userTimestamp ?: 0,
					latLng = latLng,
					address = address,
					parentChapter = parentChapter,
					attachmentList = attachmentList,
					tagList = tagList,
					onClickTimestamp = { blockOnOperationPending { openDialog(ViewerDialogType.DatePicker) } },
					onClickChapter = { blockOnOperationPending { openDialog(ViewerDialogType.Where) } },
				)
			} ?: LoadingView()
		}
	}
}
