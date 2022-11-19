package com.syncodec.graphite.presentation.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.search.composable.dialog.SearchDialogType
import com.syncodec.graphite.presentation.search.composable.screen.SearchScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID


@AndroidEntryPoint
class SearchActivity : ComponentActivity() {

	private val viewModel by viewModels<SearchViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			setContent {
				BaseContent {

					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onBackground)
					systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

					val showResultScreen by viewModel.showResultScreen
					val tagList = viewModel.tagList
					val visibleNote = viewModel.visibleNoteList
					val tag by viewModel.showTag.collectAsState(initial = null)
					val query by viewModel.searchQuery.collectAsState(initial = null)

					val parentChapterId by viewModel.parentChapter
					val chapterList = viewModel.chapterList
					val chapterPath = viewModel.chapterPath

					var showWhereDialog by remember { mutableStateOf(false) }

					var isSelected by viewModel.isSelected
					val selectedRealmUUIDList = viewModel.selectedRealmUUIDList

					fun openDialog(dialogType : SearchDialogType) {
						when (dialogType) {
							SearchDialogType.WHERE -> showWhereDialog = true
						}
					}

					fun closeDialog(dialogType : SearchDialogType) {
						when (dialogType) {
							SearchDialogType.WHERE -> showWhereDialog = false
						}
					}

					this.onBackPressedDispatcher.addCallback(
						this, object : OnBackPressedCallback(true) {
							override fun handleOnBackPressed() {
								if (isSelected) {
									isSelected = false
									selectedRealmUUIDList.clear()
								} else {
									if (showWhereDialog) {
										if (parentChapterId != null) {
											viewModel.onWhere(chapterPath.getOrNull(1)?.id)
										} else {
											closeDialog(SearchDialogType.WHERE)
										}
									} else if (viewModel.showResultScreen.value) {
										viewModel.showResultScreen.value = false
										viewModel.showTag.value = null
										viewModel.searchQuery.value = null
									} else {
										finish()
									}
								}
							}
						}
					)

					CompositionLocalProvider(
						onShowFavourite provides viewModel::showFavourite,
						onShowWithAttachment provides viewModel::showWithAttachment,
						onShowLocked provides {},
						onShowTag provides viewModel::showTag,
						onShowQuery provides viewModel::searchInNotes,
						Companion.parentChapter provides parentChapterId,
						Companion.chapterList provides chapterList,
						Companion.chapterPath provides chapterPath,
						Companion.showWhereDialog provides showWhereDialog,
						openDialog provides ::openDialog,
						closeDialog provides ::closeDialog,
						LocalCompositionIsSelected provides isSelected,
						LocalCompositionSelectedRealmUUIDList provides selectedRealmUUIDList,
						LocalCompositionOnSelect provides { isSelected = it },
						onWhere provides viewModel::onWhere,
						setOnWhere provides viewModel::setOnWhere,
					) {
						SearchScreen(
							showResultScreen = showResultScreen,
							tagList = tagList,
							visibleNote = visibleNote,
							tag = tag,
							query = query,
						) { this.onBackPressedDispatcher.onBackPressed() }
					}
				}
			}
		}
	}

	companion object {
		val onShowFavourite = compositionLocalOf<() -> Unit> { {} }
		val onShowWithAttachment = compositionLocalOf<() -> Unit> { {} }
		val onShowLocked = compositionLocalOf<() -> Unit> { {} }
		val onShowTag = compositionLocalOf<(TagObject) -> Unit> { {} }
		val onShowQuery = compositionLocalOf<(String) -> Unit> { {} }

		val parentChapter = compositionLocalOf<ChapterObjectLite?> { null }
		val chapterList = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }
		val chapterPath = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }

		val showWhereDialog = compositionLocalOf { false }

		val openDialog = compositionLocalOf<(SearchDialogType) -> Unit> { {} }
		val closeDialog = compositionLocalOf<(SearchDialogType) -> Unit> { {} }

		val onWhere = compositionLocalOf<(RealmUUID?) -> Unit> { {} }
		val setOnWhere = compositionLocalOf<(ChapterObjectLite?) -> Unit> { {} }
	}
}
