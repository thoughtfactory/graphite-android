package com.syncodec.graphite.presentation.common.dialog.whereDialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
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
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar.BottomBar
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar.TopBar
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet.WhereBottomSheetType
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.chapterList
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.LocalIsAuthenticated
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Preview
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun WhereDialog(
	showDialog : Boolean = true,
	parentChapter : ChapterObjectLite? = null,
	onSetChapter : (ChapterObjectLite?) -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val viewModel : WhereDialogViewModel = koinViewModel()
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsAuthenticated.current

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var bottomSheetType by remember { mutableStateOf(WhereBottomSheetType.CHAPTER) }
	fun openSheet(sheetType : WhereBottomSheetType) = scope.launch { bottomSheetType = sheetType; modalBottomSheetState.show() }
	fun closeSheet() = scope.launch { modalBottomSheetState.hide() }

	val exploreParentChapter by viewModel.parentChapter.collectAsState()
	val chapterList by viewModel.visibleChapterList.collectAsState()
	val chapterPath by viewModel.chapterPath.collectAsState()

	BackHandler(enabled = showDialog) {
		onDismiss()
	}

	BackHandler(enabled = showDialog && exploreParentChapter != null) {
		viewModel.exploreChapter(exploreParentChapter?.parentId)
	}

	AnimatedVisibility(
		visible = showDialog,
		modifier = Modifier.fillMaxSize(),
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300)),
	) {
		GenericScaffold(
			topBar = {
				TopBar(
					chapterPath = chapterPath,
					onExploreChapter = viewModel::exploreChapter,
					onDismiss = onDismiss
				)
			},
			bottomBar = {
				BottomBar(
					onClickSelect = {
						onSetChapter(exploreParentChapter?.toLite())
						onDismiss()
					},
					onClickEverywhere = {
						onSetChapter(null)
						onDismiss()
					}
				)
			},
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
			AnimatedContent(
				targetState = chapterList,
				transitionSpec = { scaleIn(tween(300), 0.71f) + fadeIn(tween(300)) with scaleOut(tween(300), 0.71f) + fadeOut(tween(300)) }
			) {
				if (it.isEmpty()) EmptyView(
					image = R.drawable.il_empty_chapter,
					title = "No chapters found"
				)
				else LazyColumn(
					modifier = Modifier.fillMaxSize()
				) {
					chapterList(
						chapterList = it,
						onClick = { viewModel.exploreChapter(it.id) },
					)
				}
			}
		}
	}
}
