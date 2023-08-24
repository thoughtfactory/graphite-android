package com.syncodec.graphite.presentation.common.selectionAction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer


@Preview
@Composable
fun SelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	selectedItemCount: Int = 0,
	onClickShare: () -> Unit = {},
	onClickSelectAll: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickCancel: () -> Unit = {},
) {
	AnimatedVisibility(
		visible = isSelecting,
		enter = expandVertically(tween(170)),
		exit = shrinkVertically(tween(170)),
		label = "selectionActionView_animation",
		modifier = modifier
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.shadow(
					elevation = 4.dp,
					shape = MaterialTheme.shapes.extraLarge,
				)
				.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
				.padding(horizontal = 12.dp)
		) {
			Spacer(modifier = Modifier.height(16.dp))
			AnimatedText(
				text = if (selectedItemCount == 0) "No items selected" else "$selectedItemCount items selected",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				modifier = Modifier.padding(start = 8.dp)
			)
			Spacer(modifier = Modifier.height(16.dp))
			Spacer(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.071f))
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.height(72.dp)
			) {
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_share,
					text = "Share",
					onClick = onClickShare,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_select_all,
					text = "Select all",
					onClick = onClickSelectAll,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_delete,
					text = "Delete",
					contentColor = MaterialTheme.colorScheme.error,
					onClick = onClickDelete,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_x,
					text = "Cancel",
					onClick = onClickCancel,
				)
			}
		}
	}
}

@Preview
@Composable
fun ExtendedSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	extraAction: (@Composable RowScope.() -> Unit)? = null,
	onClickShare: () -> Unit = {},
	onClickSelectAll: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickCancel: () -> Unit = {},
	onClickMove: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	AnimatedVisibility(
		visible = isSelecting,
		enter = expandVertically(tween(170)),
		exit = shrinkVertically(tween(170)),
		label = "selectionActionView_animation",
		modifier = modifier
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.shadow(
					elevation = 4.dp,
					shape = MaterialTheme.shapes.extraLarge,
				)
				.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
				.padding(horizontal = 12.dp)
		) {
			Spacer(modifier = Modifier.height(16.dp))
			AnimatedText(
				text = if (selectedItemCount == 0) "No items selected" else "$selectedItemCount items selected",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				modifier = Modifier.padding(start = 8.dp)
			)
			Spacer(modifier = Modifier.height(16.dp))
			Spacer(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.071f))
			)

			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.height(72.dp)
			) {
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_share,
					text = "Share",
					onClick = onClickShare,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_select_all,
					text = "Select all",
					onClick = onClickSelectAll,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_delete,
					text = "Delete",
					contentColor = MaterialTheme.colorScheme.error,
					onClick = onClickDelete,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_x,
					text = "Cancel",
					onClick = onClickCancel,
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.height(72.dp)
			) {
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = R.drawable.ic_fa_export,
					text = "Move",
					onClick = onClickMove,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = if (isAllItemFavourite) R.drawable.ic_fa_heart_solid else R.drawable.ic_fa_heart,
					text = "Favourite",
					iconColor = if (isAllItemFavourite) Color.FavouriteContainer else MaterialTheme.colorScheme.onPrimaryContainer,
					onClick = onClickFavourite,
				)
				SelectionActionButton(
					modifier = Modifier.weight(1f),
					icon = if (isAllItemLocked) R.drawable.ic_fa_lock_close_solid else R.drawable.ic_fa_lock_open,
					text = "Lock",
					iconColor = if (isAllItemLocked) Color.LockClosedContainer else MaterialTheme.colorScheme.onPrimaryContainer,
					onClick = onClickLock,
				)
				extraAction?.let { it() } ?: Spacer(modifier = Modifier.weight(1f))
			}
		}
	}
}

@Preview
@Composable
fun BucketSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	onClickShare: () -> Unit = {},
	onClickSelectAll: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickCancel: () -> Unit = {},
	onClickMove: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickSetAs : () -> Unit = {},
) {
	ExtendedSelectionActionView(
		modifier = modifier,
		isSelecting = isSelecting,
		isAllItemFavourite = isAllItemFavourite,
		isAllItemLocked = isAllItemLocked,
		selectedItemCount = selectedItemCount,
		extraAction = {
			SelectionActionButton(
				modifier = Modifier.weight(1f),
				icon = R.drawable.ic_fa_circle_dot_duotone,
				text = "Set as",
				onClick = onClickSetAs,
			)
		},
		onClickShare = onClickShare,
		onClickSelectAll = onClickSelectAll,
		onClickDelete = onClickDelete,
		onClickCancel = onClickCancel,
		onClickMove = onClickMove,
		onClickFavourite = onClickFavourite,
		onClickLock = onClickLock,
	)
}

@Preview
@Composable
private fun SelectionActionButton(
	modifier: Modifier = Modifier,
	icon: Int = R.drawable.ic_fa_share,
	text: String = "Share",
	contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
	iconColor: Color = contentColor,
	onClick: () -> Unit = {},
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = modifier
			.fillMaxHeight()
			.clickable { onClick() }
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = text,
			tint = iconColor,
			modifier = Modifier.requiredSize(IconButtonSize)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = contentColor
		)
	}
}
