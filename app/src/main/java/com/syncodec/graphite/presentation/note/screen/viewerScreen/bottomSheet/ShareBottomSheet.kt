package com.syncodec.graphite.presentation.note.screen.viewerScreen.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet


@Preview
@Composable
fun ShareBottomSheet() {
	GenericBottomSheet(
		title = "Share",
		icon = R.drawable.ic_share,
	) {
	}
}
