package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.button.BottomSheetButton
import com.syncodec.graphite.presentation.custom.button.BottomSheetButtonData
import com.syncodec.graphite.presentation.notebook.NotebookViewModel
import com.syncodec.graphite.presentation.ui.*


@Composable
fun MenuBottomSheet(
	closeSheet: () -> Unit
) {
	val viewModel: NotebookViewModel = viewModel()

	val buttonList: List<BottomSheetButtonData> = remember {
		listOf(
			BottomSheetButtonData(
				title = "Set as Default",
				icon = R.drawable.ic_state,
				onClick = {}
			),
			BottomSheetButtonData(
				title = "Edit",
				icon = R.drawable.ic_pencil,
				onClick = {
					viewModel.showEditChapterDialog.value = true
					closeSheet()
				}
			),
			BottomSheetButtonData(
				title = "Delete",
				icon = R.drawable.ic_delete,
				containerColor = Color.DeleteContainer,
				contentColor = Color.DeleteContent,
				onClick = {}
			),
			BottomSheetButtonData(
				title = "Lock",
				icon = R.drawable.ic_lock_close,
				onClick = {}
			),
			BottomSheetButtonData(
				title = "Favorite",
				icon = R.drawable.ic_favourite,
				onClick = {}
			)
		)
	}

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


		for (i in 0 until (buttonList.size / 3) + 1) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 0.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					for (j in 0 until 3) {
						val buttonData = buttonList.getOrNull(i * 3 + j)
						if (buttonData != null) {
							BottomSheetButton(
								title = buttonData.title,
								icon = buttonData.icon,
								containerColor = buttonData.containerColor,
								contentColor = buttonData.contentColor,
								modifier = Modifier.weight(1f),
								onClick = buttonData.onClick
							)
						} else {
							Box(modifier = Modifier.weight(1f))
						}
						if (j != 2) Spacer(modifier = Modifier.width(8.dp))
					}
				}
				if (i != (buttonList.size / 3)) {
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
