package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip


@Composable
fun MenuBottomSheet(
	closeSheet: () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu,
		)

		Spacer(modifier = Modifier.height(8.dp))



		Spacer(modifier = Modifier.height(32.dp))
	}
}
