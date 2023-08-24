package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericBottomSheet2(
	modifier: Modifier = Modifier,
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	content: @Composable ColumnScope.() -> Unit = { },
) {
	if (isBottomSheetVisible) {
		ModalBottomSheet(
			sheetState = bottomSheetState,
			onDismissRequest = onDismissRequest,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
			modifier = modifier,
			content = content,
		)
	}
}
