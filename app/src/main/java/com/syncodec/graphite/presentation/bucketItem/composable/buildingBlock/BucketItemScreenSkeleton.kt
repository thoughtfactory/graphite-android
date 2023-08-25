package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.presentation.bucketItem.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.composable.MetadataBottomSheet
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketItemScreenSkeleton(
	isNew: Boolean = false,
	bucketItemObject: BucketItemObject? = null,
	onClickSave: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	content: @Composable ColumnScope.() -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val bottomSheetState = rememberModalBottomSheetState()
	var isMetadataBottomSheetVisible by remember { mutableStateOf(false) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	val isFavourite by remember(bucketItemObject?.isFavourite) { derivedStateOf { bucketItemObject?.isFavourite == true } }
	val isLocked by remember(bucketItemObject?.isLocked) { derivedStateOf { bucketItemObject?.isLocked == true } }

	GenericScaffold2(
		topBar = {
			TopBar(
				isNew = isNew,
				isFavourite = isFavourite,
				isLocked = isLocked,
				onClickSave = onClickSave,
				onClickDelete = { isDeleteDialogVisible = true },
				onClickFavourite = onClickFavourite,
				onClickLock = onClickLock,
				onClickBack = {},
			)
		},
		bottomBar = {
			BottomBar(
				onClickMetadata = { isMetadataBottomSheetVisible = true },
				onClickShare = {},
			)
		}
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
				.verticalScroll(rememberScrollState()),
			content = content
		)
	}

	MetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMetadataBottomSheetVisible = false } },
		id = bucketItemObject?.id,
		createdTimestamp = bucketItemObject?.createdTimestamp,
		modifiedTimestamp = bucketItemObject?.modifiedTimestamp
	)

	DeleteDialog(
		isDialogVisible = isDeleteDialogVisible,
		onDismissRequest = { isDeleteDialogVisible = false },
		title = stringResource(id = R.string.delete_item),
		contentText = stringResource(id = R.string.are_you_sure_delete),
		onConfirmDelete = {}
	)
}
