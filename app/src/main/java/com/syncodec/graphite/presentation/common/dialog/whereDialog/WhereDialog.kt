package com.syncodec.graphite.presentation.common.dialog.whereDialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar.BottomBar
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar.TopBar
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet.WhereBottomSheetType
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.buildingBlock.ChapterItem
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.LocalVaultIsOpened
import kotlinx.coroutines.launch


@Preview
@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalMaterialApi::class
)
@Composable
fun WhereDialog(
	showDialog : Boolean = true,
	parentChapter : ChapterObjectLite? = null,
	onSetChapter : (ChapterObjectLite?) -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val viewModel : WhereDialogViewModel = hiltViewModel()
	val scope = rememberCoroutineScope()

	val isVaultOpen = LocalVaultIsOpened.current

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var bottomSheetType by remember { mutableStateOf(WhereBottomSheetType.CHAPTER) }
	fun openSheet(_bottomSheetType : WhereBottomSheetType) {
		scope.launch {
			bottomSheetType = _bottomSheetType
			modalBottomSheetState.show()
		}
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	val exploreParentChapter by viewModel.parentChapter.collectAsState()
	val chapterList = viewModel.visibleChapterList
	val chapterPath = viewModel.chapterPath

	BackHandler(enabled = showDialog) {
		if (modalBottomSheetState.isVisible) {
			closeSheet()
		} else {
			if (chapterPath.isNotEmpty()) {
				viewModel.onExploreChapter(chapterPath.lastOrNull()?.id)
			} else {
				onDismiss()
			}
		}
	}

	LaunchedEffect(key1 = parentChapter) {
		viewModel.onExploreChapter(parentChapter?.id)
	}

	AnimatedVisibility(
		visible = showDialog,
		modifier = Modifier.fillMaxSize(),
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300))
	) {
		GenericScaffold(
			topBar = {
				TopBar(
					chapterPath = chapterPath,
					onExploreChapter = viewModel::onExploreChapter,
					onDismiss = onDismiss
				)
			},
			bottomBar = { BottomBar { onSetChapter(exploreParentChapter) } },
			floatingActionButton = {
				FloatingActionButton(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
					onClick = { openSheet(WhereBottomSheetType.CHAPTER) },
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_notebook),
						contentDescription = "New Notebook",
						modifier = Modifier.requiredSize(IconButtonSize)
					)
				}
			},
			modalBottomSheetState = modalBottomSheetState,
			sheetContent = {
				SheetLayout(
					bottomSheetType = bottomSheetType,
					putChapter = { title, description, color, bitmap ->
						viewModel.putChapter(
							parentChapterId = exploreParentChapter?.id,
							title = title,
							description = description,
							color = color,
							bitmap = bitmap
						)
						closeSheet()
					},
				)
			},
		) {
			if (chapterList.isEmpty()) {
				EmptyView(
					image = R.drawable.il_empty_chapter,
					title = "No chapters found"
				)
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize()
				) {
					item { Spacer(modifier = Modifier.height(8.dp)) }
					chapterList.filter { if (it.isLocked) isVaultOpen else true }.forEach {
						item(
							key = it.id.toString(),
							contentType = ChapterObjectLite
						) {
							Box(
								modifier = Modifier.animateItemPlacement()
							) {
								ChapterItem(chapterObject = it) {
									viewModel.onExploreChapter(it.id)
								}
							}
						}
					}
					item { Spacer(modifier = Modifier.height(32.dp)) }
				}
			}
		}
	}
}
