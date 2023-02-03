package com.syncodec.graphite.presentation.notebook.screen.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.main.composable.bottomSheet.FilterBottomSheet
import io.realm.kotlin.types.RealmUUID


enum class NotebookBottomSheetType {
	Menu,
	Chapter,
	Filter,
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : NotebookBottomSheetType = NotebookBottomSheetType.Menu,
	chapterId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	title : String? = null,
	description : String? = null,
	color : Color? = null,
	thumbnail : Bitmap? = null,
	parentId : RealmUUID? = null,
	isDefault : Boolean = false,
	onClickSetDefaultChapter : (RealmUUID) -> Unit = {},
	onClickEdit : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	putChapter : (String?, String?, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
	onCloseSheet : () -> Unit = {},
) {

	when (bottomSheetType) {
		NotebookBottomSheetType.Menu -> MenuBottomSheet(
			chapterId = chapterId,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			title = title,
			description = description,
			color = color,
			thumbnail = thumbnail,
			parentId = parentId,
			isDefault = isDefault,
			onClickSetDefaultChapter = onClickSetDefaultChapter,
			onClickEdit = onClickEdit,
			onClickDelete = onClickDelete,
			onCloseSheet = onCloseSheet,
		)

		NotebookBottomSheetType.Chapter -> ChapterBottomSheet(
			putChapter = putChapter,
			onCloseSheet = onCloseSheet,
		)
		NotebookBottomSheetType.Filter -> FilterBottomSheet(showViewTypeOption = false)
	}

}
