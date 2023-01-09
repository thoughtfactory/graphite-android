package com.syncodec.graphite.presentation.note.composable

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.utils.LocationState
import io.realm.kotlin.types.RealmUUID
import java.io.File


val LocalCompositionNoteObject = compositionLocalOf<NoteObject?> { null }
val LocalCompositionNoteId = compositionLocalOf<RealmUUID?> { null }
val LocalCompositionNoteIdList = compositionLocalOf<SnapshotStateList<RealmUUID>> { mutableStateListOf() }
val LocalCompositionIsViewing = compositionLocalOf<Boolean?> { null }
val LocalCompositionIsOperationPending = compositionLocalOf<Boolean> { false }
val LocalCompositionContentThumbnail = compositionLocalOf<String?> { null }
val LocalCompositionContent = compositionLocalOf<String?> { null }
val LocalCompositionCreatedTimestamp = compositionLocalOf<Long?> { null }
val LocalCompositionModifiedTimestamp = compositionLocalOf<Long?> { null }
val LocalCompositionUserTimestamp = compositionLocalOf<Long?> { null }
val LocalCompositionTitle = compositionLocalOf<String?> { null }
val LocalCompositionColor = compositionLocalOf<Int?> { null }
val LocalCompositionLatLng = compositionLocalOf<com.syncodec.graphite.di.model.LatLng?> { null }
val LocalCompositionAddress = compositionLocalOf<String?> { null }
val LocalCompositionIsLocked = compositionLocalOf<Boolean?> { null }
val LocalCompositionIsFavourite = compositionLocalOf<Boolean?> { null }
val LocalCompositionParentChapterId = compositionLocalOf<RealmUUID?> { null }
val LocalCompositionParentChapter = compositionLocalOf<ChapterObject?> { null }
val LocalCompositionAttachmentList = compositionLocalOf< List<Pair<File?, Uri?>>> { listOf() }

val LocalCompositionTagList = compositionLocalOf<SnapshotStateList<TagObject>> { mutableStateListOf() }
val LocalCompositionTagListBuffer = compositionLocalOf<SnapshotStateList<TagObject>> { mutableStateListOf() }

val LocalCompositionSelectChapterList = compositionLocalOf<SnapshotStateList<ChapterObject>> { mutableStateListOf() }
val LocalCompositionSelectChapterPath = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }
val LocalCompositionOnSelectChapter = compositionLocalOf<(RealmUUID?) -> Unit> { {} }
val LocalCompositionOnMoveChapter = compositionLocalOf<(RealmUUID?) -> Unit> { {} }
val LocalCompositionSetUserTimestamp = compositionLocalOf<(Long) -> Unit> { {} }

val LocalCompositionLocationState = compositionLocalOf<LocationState> { LocationState.UNKNOW_ERROR }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(NoteBottomSheetType) -> Unit> { {} }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOpenDialog = compositionLocalOf<(NoteDialogType, Any?) -> Unit> { {_, _ ->} }
val LocalCompositionCloseDialog = compositionLocalOf<(NoteDialogType) -> Unit> { {} }

val LocalCompositionShowDatePickerDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowTimePickerDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowLocationPickerDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowNotificationPermissionDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowChapterSelectionDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowShareDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowDiscardDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { false }

val LocalSaveNote = compositionLocalOf<() -> Unit> { {} }
val LocalGetNote = compositionLocalOf<(RealmUUID) -> Unit> { {} }
val LocalEditNote = compositionLocalOf<() -> Unit> { {} }
val LocalOnClickTag = compositionLocalOf<(TagObject) -> Unit> { {} }
val LocalDiscardChanges = compositionLocalOf<() -> Unit> { {} }
val LocalDeleteNote = compositionLocalOf<() -> Unit> { {} }
val LocalOnShareText = compositionLocalOf<() -> Unit> { {} }
val LocalOnShareAttachment = compositionLocalOf<() -> Unit> { {} }
val LocalOnExportMarkdown = compositionLocalOf<() -> Unit> { {} }
