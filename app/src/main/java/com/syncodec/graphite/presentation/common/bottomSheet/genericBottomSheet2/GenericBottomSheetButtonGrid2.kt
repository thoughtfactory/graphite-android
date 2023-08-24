package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun GenericBottomSheetButtonGrid2(
	content : LazyGridScope.() -> Unit = {},
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(96.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		content()
	}
}
