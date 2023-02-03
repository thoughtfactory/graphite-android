package com.syncodec.graphite.presentation.note.screen.viewerScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
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
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.screen.viewerScreen.composable.ViewerComponent
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bar.TopBar
import com.syncodec.graphite.presentation.note.screen.viewerScreen.bar.BottomBar
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun ViewerScreen(
	isOperationPending : Boolean = false,
	onClickMenu : () -> Unit = {},
	onClickEditNote : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : ViewerScreenViewModel = koinViewModel()

	val isOperationPending by viewModel.isOperationPending.collectAsState()

	val noteId by viewModel.noteId.collectAsState()
	val createdTimestamp by viewModel.createdTimestamp.collectAsState()
	val modifiedTimestamp by viewModel.modifiedTimestamp.collectAsState()
	val userTimestamp by viewModel.userTimestamp.collectAsState()
	val title by viewModel.title.collectAsState()
	val color by viewModel.color.collectAsState()
	val latLng by viewModel.latLng.collectAsState()
	val address by viewModel.address.collectAsState()
	val contentThumbnail by viewModel.contentThumbnail.collectAsState()
	val content by viewModel.content.collectAsState()
	val thumbnail by viewModel.thumbnail.collectAsState()
	val thumbnailType by viewModel.thumbnailType.collectAsState()
	val isFavourite by viewModel.isFavourite.collectAsState()
	val isLocked by viewModel.isLocked.collectAsState()
	val parentId by viewModel.parentId.collectAsState()
	val parentChapter by viewModel.parentChapter.collectAsState()

	val tagList by viewModel.tagList.collectAsState()

	var attachmentList by remember { mutableStateOf<List<File>>(listOf()) }

//	TODO: Observe changes in attachment directory
	fun getAttachments(noteId : RealmUUID) {
		scope.launch(Dispatchers.IO) {
			val attachmentDir = File(context.attachmentDirPath(noteId = noteId))
			withContext(Dispatchers.Main) {
				attachmentList = attachmentDir.listFiles()?.toList() ?: listOf()
			}
		}
	}

	LaunchedEffect(key1 = noteId) { noteId?.let { getAttachments(it) } }

	GenericScaffold(
		topBar = {
			TopBar(
				isOperationPending = isOperationPending,
				isFavourite = isFavourite ?: false,
				isLocked = isLocked ?: false,
				onClickFavourite = viewModel::toggleFavourite,
				onClickLock = viewModel::toggleLock,
				onClickMenu = onClickMenu,
				onClickBack = onClickBack,
			)
		},
		bottomBar = {
			BottomBar(
				isOperationPending = isOperationPending,
				noteId = noteId,
				onClickMetadata = {},
				onClickPin = {},
				onClickShare = {},
				onClickEditNote = onClickEditNote,
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
					tagList = tagList
				) {}
			} ?: LoadingView()
		}
	}
}
