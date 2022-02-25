package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.mainComponent.MainViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch


data class MenuBottomSheetButtonData(val title: String, val imageVector: ImageVector, val highlight: Boolean = false, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet() {
	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val mainActivity = LocalContext.current as MainActivity

	var showArchived by viewModel.activityState.showArchived
	var showFavourite by viewModel.activityState.showFavourite
	var showTrash by viewModel.activityState.showTrash
	val isSelected by viewModel.activityState.isSelected
	var showDeleteDialog by viewModel.activityState.showDeleteDialog

	var vaultState by (mainActivity.application as Momento).vaultState
	val vaultKey by viewModel.activityState.vaultKeyFlow.collectAsState(initial = null)

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.activityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.activityState.bottomSheetState.show()
		}
	}

	val hideSheet: () -> Unit = {
		scope.launch {
			viewModel.activityState.bottomSheetState.hide()
		}
	}

	val menuBottomSheetButtonDataLists: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Media", imageVector = TablerIcons.Photo) {},
		MenuBottomSheetButtonData(
			title = "Vault",
			imageVector = TablerIcons.Container,
			highlight = when (vaultState) {
				Momento.Companion.VaultState.NOT_OPENED -> false
				Momento.Companion.VaultState.TRY_OPEN -> false
				Momento.Companion.VaultState.SETUP -> false
				Momento.Companion.VaultState.OPENED -> true
				Momento.Companion.VaultState.CLOSED -> false
				Momento.Companion.VaultState.ERROR -> false
			}
		) {
			when (vaultState) {
				Momento.Companion.VaultState.NOT_OPENED -> vaultState = Momento.Companion.VaultState.TRY_OPEN
				Momento.Companion.VaultState.TRY_OPEN -> {
				}
				Momento.Companion.VaultState.SETUP -> {
				}
				Momento.Companion.VaultState.OPENED -> vaultState = Momento.Companion.VaultState.CLOSED
				Momento.Companion.VaultState.CLOSED -> vaultState = Momento.Companion.VaultState.TRY_OPEN
				Momento.Companion.VaultState.ERROR -> {
				}
			}
			hideSheet()
		},
		MenuBottomSheetButtonData(title = "Life in Weeks", imageVector = TablerIcons.CalendarMinus) {},
		MenuBottomSheetButtonData(title = "Settings", imageVector = TablerIcons.Settings) {},

		MenuBottomSheetButtonData(title = "Archived", imageVector = TablerIcons.Archive, highlight = showArchived) { showArchived = !showArchived },
		MenuBottomSheetButtonData(title = "Favourite", imageVector = TablerIcons.Heart, highlight = showFavourite) { showFavourite = !showFavourite },
//		MenuBottomSheetButtonData(title = "Trash", imageVector = TablerIcons.Trash, highlight = showTrash) { showTrash = !showTrash }
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			imageVector = TablerIcons.Dots
		)

		LazyVerticalGrid(
			cells = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(menuBottomSheetButtonDataLists) { _, menuBottomSheetButtonData ->
				MenuBottomSheetButton(menuBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		Card(
			backgroundColor = MaterialTheme.colorScheme.primaryContainer,
			elevation = 0.dp,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.padding(24.dp, 0.dp),
			onClick = { /*TODO*/ }
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun MenuBottomSheetButton(
	menuBottomSheetButtonData: MenuBottomSheetButtonData?
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (menuBottomSheetButtonData != null) {
			Crossfade(targetState = menuBottomSheetButtonData.highlight) { highlight ->
				if (highlight) {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.onSecondaryContainer,
						shape = RoundedCornerShape(8.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clip(RoundedCornerShape(8.dp))
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.secondaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				} else {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
						shape = RoundedCornerShape(8.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clip(RoundedCornerShape(8.dp))
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				}
			}
			Text(
				text = menuBottomSheetButtonData.title,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				maxLines = 2,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
	}
}
