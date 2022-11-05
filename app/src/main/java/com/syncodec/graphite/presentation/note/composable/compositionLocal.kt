package com.syncodec.graphite.presentation.note.composable

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.utils.LocationState
import io.realm.kotlin.types.ObjectId
import java.io.File


val LocalCompositionNoteObject = compositionLocalOf<NoteObject?> { null }
val LocalCompositionNoteId = compositionLocalOf<ObjectId?> { error("No data provided") }
val LocalCompositionNoteIdList = compositionLocalOf<SnapshotStateList<ObjectId>> { error("No data provided") }
val LocalCompositionIsViewing = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionIsOperationPending = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionContentThumbnail = compositionLocalOf<String?> { error("No data provided") }
val LocalCompositionContent = compositionLocalOf<String?> { error("No data provided") }
val LocalCompositionCreatedTimestamp = compositionLocalOf<Long?> { error("No data provided") }
val LocalCompositionModifiedTimestamp = compositionLocalOf<Long?> { error("No data provided") }
val LocalCompositionUserTimestamp = compositionLocalOf<Long?> { error("No data provided") }
val LocalCompositionTitle = compositionLocalOf<String?> { error("No data provided") }
val LocalCompositionColor = compositionLocalOf<Int?> { error("No data provided") }
val LocalCompositionLatLng = compositionLocalOf<com.syncodec.graphite.di.model.LatLng?> { error("No data provided") }
val LocalCompositionAddress = compositionLocalOf<String?> { error("No data provided") }
val LocalCompositionIsLocked = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionIsFavourite = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionParentChapterId = compositionLocalOf<ObjectId?> { error("No data provided") }
val LocalCompositionParentChapter = compositionLocalOf<ChapterObject?> { null }
val LocalCompositionAttachmentList = compositionLocalOf< Map<ObjectId, Triple<AttachmentObject, File?, Uri?>>> { error("No data provided") }

val LocalCompositionTagList = compositionLocalOf<SnapshotStateList<TagObject>> { error("No data provided") }
val LocalCompositionTagListBuffer = compositionLocalOf<SnapshotStateList<TagObject>> { error("No data provided") }

val LocalCompositionSelectChapterList = compositionLocalOf<SnapshotStateList<ChapterObject>> { error("No data provided") }
val LocalCompositionSelectChapterPath = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { error("No data provided") }
val LocalCompositionOnSelectChapter = compositionLocalOf<(ObjectId) -> Unit> { error("No data provided") }
val LocalCompositionOnMoveChapter = compositionLocalOf<() -> Unit> { error("No data provided") }

val LocalCompositionLocationState = compositionLocalOf<LocationState> { LocationState.UNKNOW_ERROR }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(NoteBottomSheetType) -> Unit> { error("No data provided") }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOpenDialog = compositionLocalOf<(NoteDialogType, Any?) -> Unit> { error("No data provided") }
val LocalCompositionCloseDialog = compositionLocalOf<(NoteDialogType) -> Unit> { error("No data provided") }

val LocalCompositionShowLocationPickerDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowNotificationPermissionDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowChapterSelectionDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowDiscardDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { error("No data provided") }

val LocalSaveNote = compositionLocalOf<() -> Unit> { error("No save note provided") }
val LocalGetNote = compositionLocalOf<(ObjectId) -> Unit> { error("No get note provided") }
val LocalEditNote = compositionLocalOf<() -> Unit> { error("No edit note provided") }
val LocalOnClickTag = compositionLocalOf<(TagObject) -> Unit> { error("No click tag provided") }
val LocalDiscardChanges = compositionLocalOf<() -> Unit> { error("No discard changes provided") }
val LocalDeleteNote = compositionLocalOf<() -> Unit> { error("No delete note provided") }
