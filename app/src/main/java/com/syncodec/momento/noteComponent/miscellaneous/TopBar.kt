package com.syncodec.momento.noteComponent.miscellaneous

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.noteComponent.NoteViewModel
import com.syncodec.momento.noteComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Dots
import compose.icons.tablericons.InfoCircle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar() {
	val noteViewModel: NoteViewModel = viewModel()
	val scope = rememberCoroutineScope()

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(8.dp)
		) {
			IconButton(
				onClick = { noteViewModel.activityState.richTextEditor.exec("editor.getData();") },
			) {
				Icon(
					imageVector = TablerIcons.Check,
					contentDescription = "Save",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = {
					noteViewModel.activityState.bottomSheetType.value = BottomSheetType.MetadataBottomSheet
					scope.launch {
						noteViewModel.activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
					}
				},
			) {
				Icon(
					imageVector = TablerIcons.InfoCircle,
					contentDescription = "Metadata",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			IconButton(
				onClick = {
					noteViewModel.activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
					scope.launch {
						noteViewModel.activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
					}
				},
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
		}
	}
}
