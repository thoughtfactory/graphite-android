package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	isNewItem: Boolean,
	onClickMenu: () -> Unit,
	onClick: () -> Unit,
) {

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
				onClick = { onClick() },
			) {
				Icon(
					imageVector = if (isNewItem) TablerIcons.Check else TablerIcons.ArrowBack,
					contentDescription = if (isNewItem) "Save" else "Back",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = { onClickMenu() },
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
