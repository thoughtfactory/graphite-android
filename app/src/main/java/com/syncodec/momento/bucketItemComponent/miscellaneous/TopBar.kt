package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.animation.AnimatedContent
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
import androidx.room.PrimaryKey
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	primaryKey: String?,
	onClickMenu: () -> Unit,
	onClick: () -> Unit,
) {
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
				onClick = { onClick() },
			) {
				AnimatedContent(targetState = primaryKey) {
					if (it == null) {
						Icon(
							imageVector = TablerIcons.Check,
							contentDescription = "Save",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					} else {
						Icon(
							imageVector = TablerIcons.ArrowBack ,
							contentDescription = "Back",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					}
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = { onClickMenu() },
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
