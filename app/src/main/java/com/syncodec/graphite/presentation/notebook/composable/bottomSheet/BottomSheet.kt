package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.sheets.MetadataBottomSheet
import com.syncodec.graphite.presentation.explorer.atlas.AtlasActivity
import com.syncodec.graphite.presentation.explorer.calendar.CalendarActivity
import com.syncodec.graphite.presentation.main.composable.bottomSheet.ChapterBottomSheet
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID


enum class NotebookBottomSheet {
	Menu,
	Metadata,
	NewChapter,
	EditChapter,
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	editChapterBottomSheetState: SheetState = rememberModalBottomSheetState(),
	isMenuBottomSheetVisible: Boolean = false,
	isMetadataBottomSheetVisible: Boolean = false,
	isNewChapterBottomSheetVisible: Boolean = false,
	isEditChapterBottomSheetVisible: Boolean = false,
	chapterObject: ChapterObject? = null,
	defaultChapterId: RealmUUID? = null,
	putChapter: (RealmUUID?, String, String, Color?, Bitmap?) -> Unit = { _, _, _, _, _ -> },
	onClickSetDefault: (RealmUUID) -> Unit = {},
	onClickEditChapter: () -> Unit = {},
	onDismissRequest: (NotebookBottomSheet) -> Unit = {},
) {
	val context = LocalContext.current

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { onDismissRequest(NotebookBottomSheet.Menu) },
		isChapterDefault = chapterObject?.id == defaultChapterId,
		onClickAttachments = {
			Intent(context, AttachmentActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickAtlas = {
			Intent(context, AtlasActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickCalendar = {
			Intent(context, CalendarActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Calendar.name)
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickSetAsDefault = { chapterObject?.id?.let(onClickSetDefault) },
		onClickEdit = { onDismissRequest(NotebookBottomSheet.Menu); onClickEditChapter() },
		onClickDelete = { },
	)

	MetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMetadataBottomSheetVisible,
		onDismissRequest = { onDismissRequest(NotebookBottomSheet.Metadata) },
		id = chapterObject?.id,
		createdTimestamp = chapterObject?.createdTimestamp,
		modifiedTimestamp = chapterObject?.modifiedTimestamp,
		extraContent = {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.parent_id),
				value = chapterObject?.parentId?.toString() ?: stringResource(id = R.string.root_element),
			)
		}
	)

//	New chapter
	ChapterBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isNewChapterBottomSheetVisible,
		onDismissRequest = { onDismissRequest(NotebookBottomSheet.NewChapter) },
		title = stringResource(id = R.string.new_chapter),
		putChapter = putChapter,
	)

//	Edit chapter
	ChapterBottomSheet(
		bottomSheetState = editChapterBottomSheetState,
		isBottomSheetVisible = isEditChapterBottomSheetVisible,
		onDismissRequest = { onDismissRequest(NotebookBottomSheet.EditChapter) },
		title = stringResource(id = R.string.edit_chapter),
		chapterObject = chapterObject,
		putChapter = putChapter,
	)
}
