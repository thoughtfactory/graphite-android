package com.syncodec.graphite.presentation.common.v2.selectable2

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.FavouriteContent
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.presentation.ui.authenticator2.AuthState
import com.syncodec.graphite.presentation.ui.authenticator2.LocalAuthController


object SelectionActionCard {

    @Composable
    fun Composable(
        visible: Boolean = false,
        content: @Composable BoxScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = AnimationDefaults.ScaleAndFadeEnter,
            exit = AnimationDefaults.ScaleAndFadeExit,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge)
                    .clip(shape = MaterialTheme.shapes.extraLarge),
                content = content
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.SelectionActionButton(
        modifier: Modifier = Modifier,
        icon: Int,
        text: String,
        onClick: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .clickable(enabled = true, onClick = onClick)
                .weight(weight = 1f)
                .padding(vertical = 20.dp, horizontal = 4.dp)
        ) {
            AnimatedContent(
                targetState = icon,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = text,
                    modifier = Modifier.size(size = ICON_SIZE)
                )
            }

            Spacer(modifier = Modifier.height(height = 8.dp))

            AnimatedContent(
                targetState = text,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.SelectionActionButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit,
        icon: @Composable () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .clickable(enabled = true, onClick = onClick)
                .weight(weight = 1f)
                .padding(vertical = 20.dp, horizontal = 4.dp)
        ) {
            icon()

            Spacer(modifier = Modifier.height(height = 8.dp))

            AnimatedContent(
                targetState = text,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.SelectAllButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_select_all, text = stringResource(id = R.string.select_all), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.MoveButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_move, text = stringResource(id = R.string.move), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.ShareButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_share, text = stringResource(id = R.string.share), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.DeleteButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_delete, text = stringResource(id = R.string.delete), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.CancelButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_x, text = stringResource(id = R.string.cancel), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.FavouriteButton(areAllSelectedFavourite: Boolean, onClick: () -> Unit) {
        SelectionActionButton(
            text = stringResource(id = R.string.favourite),
            onClick = onClick
        ) {
            AnimatedContent(
                targetState = areAllSelectedFavourite,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                if (it) Icon(
                    painter = painterResource(id = R.drawable.ic_fa_heart_solid),
                    contentDescription = stringResource(id = R.string.favourite),
                    tint = Color.FavouriteContainer,
                    modifier = Modifier.size(size = ICON_SIZE)
                ) else Icon(
                    painter = painterResource(id = R.drawable.ic_fa_heart),
                    contentDescription = stringResource(id = R.string.favourite),
                    modifier = Modifier.size(size = ICON_SIZE)
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.LockButton(areAllSelectedLocked: Boolean, onClick: () -> Unit) {
        val authController = LocalAuthController.current
        val authState by authController.authStateFlow.collectAsState()

        SelectionActionButton(
            text = if (areAllSelectedLocked) stringResource(id = R.string.entry_locked) else stringResource(id = R.string.entry_not_locked),
            onClick = { if (authState == AuthState.Authenticated) onClick() else authController.authenticate() }
        ) {
            AnimatedContent(
                targetState = areAllSelectedLocked,
                transitionSpec = { AnimationDefaults.ScaleAndFade }
            ) {
                if (it) Icon(
                    painter = painterResource(id = R.drawable.ic_fa_lock_closed_duotone),
                    contentDescription = stringResource(id = R.string.entry_locked),
                    tint = Color.LockClosedContainer,
                    modifier = Modifier.size(size = ICON_SIZE)
                ) else Icon(
                    painter = painterResource(id = R.drawable.ic_fa_lock_opened),
                    contentDescription = stringResource(id = R.string.entry_not_locked),
                    modifier = Modifier.size(size = ICON_SIZE)
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowScope.CopyButton(onClick: () -> Unit) {
        SelectionActionButton(icon = R.drawable.ic_fa_copy, text = stringResource(id = R.string.copy), onClick = onClick)
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun FlowRowScope.BucketItemState(onClick: (BucketItemBoxDecrypted.State) -> Unit) {

        var isSetStateAsMenuVisible by remember { mutableStateOf(value = false) }

        ExposedDropdownMenuBox(
            expanded = isSetStateAsMenuVisible,
            onExpandedChange = { isSetStateAsMenuVisible = false },
            modifier = Modifier.weight(weight = 1f)
        ) {

            SelectionActionButton(
                icon = R.drawable.ic_fa_circle_dot_duotone,
                text = stringResource(id = R.string.set_state),
                onClick = { isSetStateAsMenuVisible = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = isSetStateAsMenuVisible,
                onDismissRequest = { isSetStateAsMenuVisible = false },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(min = 144.dp)
            ) {
                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.todo)) }, onClick = { onClick(BucketItemBoxDecrypted.State.Alpha) })
                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.doing)) }, onClick = { onClick(BucketItemBoxDecrypted.State.Beta) })
                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.done)) }, onClick = { onClick(BucketItemBoxDecrypted.State.Gamma) })
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun TodoBucketScreenSelectionActionCard(
        visible: Boolean = false,
        areAllSelectedFavourite: Boolean = false,
        areAllSelectedLocked: Boolean = false,
        onClickSelectAll: () -> Unit = {},
        onClickMove: () -> Unit = {},
        onClickFavourite: () -> Unit = {},
        onClickLock: () -> Unit = {},
        onClickShare: () -> Unit = {},
        onClickSetState: (BucketItemBoxDecrypted.State) -> Unit,
        onClickDelete: () -> Unit = {},
        onClickCancel: () -> Unit = {},
    ) {
        Composable(
            visible = visible
        ) {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    maxItemsInEachRow = 4
                ) {
                    SelectAllButton(onClick = onClickSelectAll)
                    FavouriteButton(areAllSelectedFavourite = areAllSelectedFavourite, onClick = onClickFavourite)
                    LockButton(areAllSelectedLocked = areAllSelectedLocked, onClick = onClickLock)
                    BucketItemState(onClick = onClickSetState)
                    MoveButton(onClick = onClickMove)
                    ShareButton(onClick = onClickShare)
                    DeleteButton(onClick = onClickDelete)
                    CancelButton(onClick = onClickCancel)
                    Spacer(modifier = Modifier.weight(weight = 1f))
                }
            }
        }
    }
}
