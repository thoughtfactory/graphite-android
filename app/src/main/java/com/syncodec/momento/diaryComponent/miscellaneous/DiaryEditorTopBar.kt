package com.syncodec.momento.diaryComponent.miscellaneous

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.diaryComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiaryEditorTopBar(
	onSave: () -> Unit
) {
	val activity = LocalContext.current as? Activity
	val viewModel: DiaryViewModel = viewModel()
	val scope = rememberCoroutineScope()

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.primaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(8.dp)
		) {
			IconButton(
				onClick = { onSave() },
			) {
				Icon(
					imageVector = TablerIcons.Check,
					contentDescription = "Save",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(8.dp),
				backgroundColor = Color.Transparent,
				onClick = {}
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "4th May, 2021",
						color = MaterialTheme.colorScheme.onPrimaryContainer,
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier
							.padding(12.dp, 8.dp, 8.dp, 8.dp),
					)

					Icon(
						imageVector = TablerIcons.ChevronDown,
						contentDescription = "Calendar",
						tint = MaterialTheme.colorScheme.onPrimaryContainer,
						modifier = Modifier
							.padding(8.dp, 8.dp, 8.dp, 8.dp),
					)
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = {
					viewModel.diaryActivityState.bottomSheetType.value = BottomSheetType.AttachmentBottomSheet
					scope.launch {
						viewModel.diaryActivityState.bottomSheetState.show()
					}
				},
			) {
				Icon(
					imageVector = TablerIcons.Paperclip,
					contentDescription = "Attachment",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			IconButton(
				onClick = {
					viewModel.diaryActivityState.bottomSheetType.value = BottomSheetType.MetadataBottomSheet
					scope.launch {
						viewModel.diaryActivityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
					}
				},
			) {
				Icon(
					imageVector = TablerIcons.InfoCircle,
					contentDescription = "Metadata",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			IconButton(
				onClick = {
					viewModel.diaryActivityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
					scope.launch {
						viewModel.diaryActivityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
					}
				},
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}
		}
	}
}
