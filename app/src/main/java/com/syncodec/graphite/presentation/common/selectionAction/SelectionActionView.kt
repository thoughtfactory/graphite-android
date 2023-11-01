package com.syncodec.graphite.presentation.common.selectionAction

import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.model.local.bucketTypeIconMap
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.ICON_SIZE
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.base.secureComposable.AuthenticationState
import com.syncodec.graphite.presentation.base.secureComposable.LocalAuthenticatorAction
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked


@Preview
@Composable
private fun SelectionActionViewSkeleton(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	selectedItemCount: Int = 0,
	content: @Composable ColumnScope.() -> Unit = {}
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
				.background(Color(ColorUtils.blendARGB(Color.Black.toArgb(), Color.White.toArgb(), 0.071f)), MaterialTheme.shapes.extraLarge)
				.padding(horizontal = 12.dp)
		) {
			Spacer(modifier = Modifier.height(16.dp))
			AnimatedText(
				text = if (selectedItemCount == 0) "No items selected" else "$selectedItemCount items selected",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = Color.White,
				modifier = Modifier.padding(start = 8.dp)
			)
			Spacer(modifier = Modifier.height(16.dp))
			Spacer(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(Color.White.copy(alpha = 0.071f))
			)
			content()
		}
	}
}

@Preview
@Composable
private fun SelectionActionButton(
	modifier: Modifier = Modifier,
	icon: Int = R.drawable.ic_fa_share,
	text: String = "Share",
	contentColor: Color = Color.White,
	iconColor: Color = contentColor,
	onClick: () -> Unit = {},
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = modifier
			.fillMaxSize()
			.clickable { onClick() }
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = text,
			tint = iconColor,
			modifier = Modifier.requiredSize(ICON_SIZE)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = contentColor
		)
	}
}

@Preview
@Composable
private fun RowScope.DeleteButton(onClick: () -> Unit = {}) {
	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = R.drawable.ic_fa_delete,
		text = stringResource(id = R.string.delete),
		contentColor = MaterialTheme.colorScheme.error,
		onClick = onClick,
	)
}

@Preview
@Composable
private fun RowScope.ShareButton(onClick: () -> Unit = {}) {
	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = R.drawable.ic_fa_share,
		text = stringResource(id = R.string.share),
		onClick = onClick,
	)
}

@Preview
@Composable
private fun RowScope.SelectAllButton(onClick: () -> Unit = {}) {
	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = R.drawable.ic_fa_select_all,
		text = stringResource(id = R.string.select_all),
		onClick = onClick,
	)
}

@Preview
@Composable
private fun RowScope.CancelButton() {
	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = R.drawable.ic_fa_x,
		text = stringResource(id = R.string.cancel),
		onClick = { onBackPressedDispatcher?.onBackPressed() },
	)
}

@Preview
@Composable
private fun RowScope.MoveButton(onClick: () -> Unit = {}) {
	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = R.drawable.ic_fa_export,
		text = stringResource(id = R.string.move),
		onClick = onClick,
	)
}

@Preview
@Composable
private fun RowScope.FavouriteButton(
	isChecked: Boolean = false,
	onClick: () -> Unit = {}
) {
	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = if (isChecked) R.drawable.ic_fa_heart_solid else R.drawable.ic_fa_heart,
		text = stringResource(id = R.string.favourite),
		iconColor = if (isChecked) Color.FavouriteContainer else Color.White,
		onClick = onClick,
	)
}

@Preview
@Composable
private fun RowScope.LockButton(
	isChecked: Boolean = false,
	onClick: () -> Unit = {}
) {
	val context = LocalContext.current
	val isRepoUnlocked = LocalIsRepoUnlocked.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	SelectionActionButton(
		modifier = Modifier.weight(1f),
		icon = if (isChecked) R.drawable.ic_fa_lock_close_solid else R.drawable.ic_fa_lock_open,
		text = stringResource(id = R.string.lock),
		iconColor = if (isChecked) Color.LockClosedContainer else Color.White,
		onClick = {
			if (isRepoUnlocked) onClick()
			else {
				Toast.makeText(context, context.getText(R.string.toast_not_authenticated), Toast.LENGTH_SHORT).show()
				onAuthenticationAction(AuthenticationState.Authenticate)
			}
		},
	)
}

@Preview
@Composable
fun ExtendedSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	extraAction: @Composable (RowScope.() -> Unit)? = null,
	onClickShare: () -> Unit = {},
	onClickSelectAll: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickMove: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			ShareButton(onClick = onClickShare)
			SelectAllButton(onClick = onClickSelectAll)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			MoveButton(onClick = onClickMove)
			FavouriteButton(
				isChecked = isAllItemFavourite,
				onClick = onClickFavourite,
			)
			LockButton(
				isChecked = isAllItemLocked,
				onClick = onClickLock,
			)
			extraAction?.let { it() } ?: Spacer(modifier = Modifier.weight(1f))
		}
	}
}

@Preview
@Composable
fun NotebookSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	onClickDelete: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			FavouriteButton(
				isChecked = isAllItemFavourite,
				onClick = onClickFavourite,
			)
			LockButton(
				isChecked = isAllItemLocked,
				onClick = onClickLock,
			)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
	}
}

@Preview
@Composable
fun MainSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	onClickDelete: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			FavouriteButton(
				isChecked = isAllItemFavourite,
				onClick = onClickFavourite,
			)
			LockButton(
				isChecked = isAllItemLocked,
				onClick = onClickLock,
			)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketSelectionActionView(
	modifier: Modifier = Modifier,
	bucketType: BucketType? = BucketType.BOOK,
	isSelecting: Boolean = true,
	selectedItemCount: Int = 0,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	onClickShare: () -> Unit = {},
	onClickSelectAll: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickMove: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickSetAs: (Int) -> Unit = {},
) {
	val context = LocalContext.current

	var isSetAsMenuVisible by remember { mutableStateOf(false) }

	val stateTodo = when (bucketType) {
		BucketType.TODO -> stringResource(id = R.string.to_do)
		BucketType.BOOK -> stringResource(id = R.string.to_read)
		BucketType.SHOW -> stringResource(id = R.string.to_watch)
		BucketType.LINK -> stringResource(id = R.string.to_do)
		BucketType.UNKNOWN -> stringResource(id = R.string.to_do)
		null -> stringResource(id = R.string.to_do)
	}

	val stateDoing = when (bucketType) {
		BucketType.TODO -> stringResource(id = R.string.doing)
		BucketType.BOOK -> stringResource(id = R.string.reading)
		BucketType.SHOW -> stringResource(id = R.string.watching)
		BucketType.LINK -> stringResource(id = R.string.to_do)
		BucketType.UNKNOWN -> stringResource(id = R.string.to_do)
		null -> stringResource(id = R.string.to_do)
	}

	val stateDone = when (bucketType) {
		BucketType.TODO -> stringResource(id = R.string.done)
		BucketType.BOOK -> stringResource(id = R.string.read)
		BucketType.SHOW -> stringResource(id = R.string.watched)
		BucketType.LINK -> stringResource(id = R.string.done)
		BucketType.UNKNOWN -> stringResource(id = R.string.done)
		null -> stringResource(id = R.string.done)
	}

	fun onSet() {
		isSetAsMenuVisible = false
		Toast.makeText(context, context.getText(R.string.toast_items_updated), Toast.LENGTH_SHORT).show()
	}

	ExtendedSelectionActionView(
		modifier = modifier,
		isSelecting = isSelecting,
		isAllItemFavourite = isAllItemFavourite,
		isAllItemLocked = isAllItemLocked,
		selectedItemCount = selectedItemCount,
		extraAction = {
			ExposedDropdownMenuBox(
				expanded = isSetAsMenuVisible,
				onExpandedChange = { isSetAsMenuVisible = false },
				modifier = Modifier.weight(1f)
			){
				SelectionActionButton(
					icon = R.drawable.ic_fa_circle_dot_duotone,
					text = stringResource(id = R.string.set_as),
					onClick = { isSetAsMenuVisible = true },
					modifier = Modifier
						.fillMaxSize()
						.menuAnchor()
				)

				ExposedDropdownMenu(
					expanded = isSetAsMenuVisible,
					onDismissRequest = { isSetAsMenuVisible = false },
					modifier = Modifier
						.widthIn(144.dp)
						.background(MaterialTheme.colorScheme.background)
				) {
					DropdownMenuItem(
						leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_clock), contentDescription = stateTodo, modifier = Modifier.requiredSize(ICON_SIZE)) },
						text = { Text(text = stateTodo) },
						onClick = { onClickSetAs(0); onSet() }
					)
					DropdownMenuItem(
						leadingIcon = { Icon(painter = painterResource(id = bucketTypeIconMap[bucketType] ?: R.drawable.ic_fa_question), contentDescription = stateDoing, modifier = Modifier.requiredSize(ICON_SIZE)) },
						text = { Text(text = stateDoing) },
						onClick = { onClickSetAs(1); onSet() }
					)
					DropdownMenuItem(
						leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_circle_check), contentDescription = stateDone, modifier = Modifier.requiredSize(ICON_SIZE)) },
						text = { Text(text = stateDone) },
						onClick = { onClickSetAs(2); onSet() }
					)
				}

			}
		},
		onClickShare = onClickShare,
		onClickSelectAll = onClickSelectAll,
		onClickDelete = onClickDelete,
		onClickMove = onClickMove,
		onClickFavourite = onClickFavourite,
		onClickLock = onClickLock,
	)
}

@Preview
@Composable
fun ExplorerSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	onClickDelete: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			FavouriteButton(
				isChecked = isAllItemFavourite,
				onClick = onClickFavourite,
			)
			LockButton(
				isChecked = isAllItemLocked,
				onClick = onClickLock,
			)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
	}
}

@Preview
@Composable
fun SearchSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	isAllItemFavourite: Boolean = false,
	isAllItemLocked: Boolean = false,
	selectedItemCount: Int = 0,
	onClickDelete: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			FavouriteButton(
				isChecked = isAllItemFavourite,
				onClick = onClickFavourite,
			)
			LockButton(
				isChecked = isAllItemLocked,
				onClick = onClickLock,
			)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
	}
}

@Preview
@Composable
fun AttachmentSelectionActionView(
	modifier: Modifier = Modifier,
	isSelecting: Boolean = true,
	selectedItemCount: Int = 0,
	onClickShare: () -> Unit = {},
	onClickDelete: () -> Unit = {},
) {
	SelectionActionViewSkeleton(
		modifier = modifier,
		isSelecting = isSelecting,
		selectedItemCount = selectedItemCount,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp)
		) {
			ShareButton(onClick = onClickShare)
			DeleteButton(onClick = onClickDelete)
			CancelButton()
		}
	}
}
