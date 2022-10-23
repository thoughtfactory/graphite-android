package com.syncodec.graphite.presentation.notebook.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.notebook.composable.dialog.EditChapterDialog
import com.syncodec.graphite.presentation.notebook.composable.dialog.ManageTagDialog
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NotebookScreen(
	rootChapterId: ObjectId?,
	chapterObject : ChapterObject?,
	rootColor : Color?,
	tagList : List<TagObject>,
	showEditChapterDialog: Boolean,
	showManageTagDialog: Boolean,

) {
	val activity: NotebookActivity = LocalContext.current as NotebookActivity
	val scope = rememberCoroutineScope()

	val softwareKeyboardController = LocalSoftwareKeyboardController.current

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

	var bottomSheetType by remember { mutableStateOf(BucketBottomSheetType.MENU) }

	val openSheet = {
		scope.launch {
			softwareKeyboardController?.hide()
			modalBottomSheetState.show()
		}
	}
	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	var bottomBarSpacingPx by remember { mutableStateOf(0) }

	ModalBottomSheetLayout(
		modifier = Modifier.fillMaxSize(),
		sheetState = modalBottomSheetState,
		sheetElevation = 0.dp,
		sheetBackgroundColor = Color.Transparent,
		sheetContent = {
			SheetLayout(
				chapterObject = chapterObject,
				tagList = tagList,
				bottomSheetType = bottomSheetType,
			) {
				closeSheet()
			}
		}
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				TopBar(
					title = chapterObject?.title,
					rootChapterId = rootChapterId,
					color = rootColor ?: MaterialTheme.colorScheme.surface,
					isLocked = chapterObject?.isLocked ?: false,
					isFavourite = chapterObject?.isFavourite ?: false,
//					chapterObjectLiteList = chapterObject?.getPath() ?: listOf(),
					chapterObjectLiteList = listOf(),
					onClickLock = {  },
					onClickFavourite = {  },
					onClickMenu = {
						bottomSheetType = BucketBottomSheetType.MENU
						openSheet()
					},
					onClickNavigator = {
						TODO()
//						viewModel.loadChapter(it)
					}
				)
			},
		) {
			Crossfade(
				targetState = chapterObject,
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				if (it == null) {
					LoadingView()
				} else {
					Box(
						modifier = Modifier.fillMaxSize()
					) {
						Column(
							modifier = Modifier.fillMaxSize()
						) {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.weight(1f)
							) {
								ExplorerScreen(
									chapterObject = chapterObject,
									tagList = tagList
								)
							}
							if (chapterObject != null) {
								BottomBar(
									modifier = Modifier
										.onGloballyPositioned {
											bottomBarSpacingPx = it.positionInParent().y.toInt()
										}
								) {
									bottomSheetType = it
									openSheet()
								}
							}
						}

						PrimaryButton(
							primaryText = "Add Note",
							primaryIcon = R.drawable.ic_pencil,
							primaryDescription = "Add a new note",
							secondaryIcon = R.drawable.ic_notebook,
							secondaryDescription = "Add a new chapter",
							bottomBarSpacingPx = bottomBarSpacingPx,
							onClickPrimary = {
								Intent(activity, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Constant.IS_NEW.name, true)
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterObject?.id.toString())
									putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

									activity.startActivity(this)
								}
							},
							onClickSecondary = {
								bottomSheetType = BucketBottomSheetType.CHAPTER
								openSheet()

							}
						)

						if (chapterObject != null) {
							EditChapterDialog(
								title = chapterObject?.title ?: "",
								description = chapterObject?.description,
								color = chapterObject?.color?.let { it1 -> Color(it1) } ?: MaterialTheme.colorScheme.primary,
								thumbnail = chapterObject?.thumbnail?.decodeBase64ToBitmap(),
								showDialog = showEditChapterDialog,
								onSave = { title, description, color, thumbnail ->
									val isFavourite = chapterObject?.isFavourite
									val isLocked = chapterObject?.isLocked

									when {
										isFavourite == null || isLocked == null -> Toast.makeText(activity, "Error updating chapter", Toast.LENGTH_SHORT).show()
										title.isBlank() -> Toast.makeText(activity, "Title cannot be empty", Toast.LENGTH_SHORT).show()
										else -> {
											TODO()
//											viewModel.updateChapter(
//												title = title,
//												description = description,
//												color = color,
//												thumbnail = thumbnail,
//												isFavourite = isFavourite,
//												isLocked = isLocked
//											)
//
//											showEditChapterDialog = false
										}
									}
								}
							) {
								TODO()
//								showEditChapterDialog = false
							}

							ManageTagDialog(
								chapterObject = chapterObject!!,
								tagList = tagList,
								showDialog = showManageTagDialog,
								onClick = {
									TODO()
//									viewModel.updateTagConnection(it.id)
								}
							) {
								TODO()
//								showManageTagDialog = false
							}
						}
					}
				}
			}
		}
	}
}
