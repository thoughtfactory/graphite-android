package com.syncodec.graphite.presentation.bucketItem2.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBoxDecrypted>>,
    isDataSaved: Boolean = false,
    isEditing: Boolean = false,
    onClickEdit: () -> Unit = {},
    onToggleLock: (Boolean) -> Unit = {},
    onToggleFavourite: (Boolean) -> Unit = {},
) {
    Crossfade(
        targetState = false,
        animationSpec = AnimationDefaults.stateAnimationSpec()
    ) {
        if (it) EditingTopBar()
        else NormalTopBar(
            bucketItemBoxDataFlow = bucketItemBoxDataFlow,
            isDataSaved = isDataSaved,
            onClickEdit = onClickEdit,
            onToggleLock = onToggleLock,
            onToggleFavourite = onToggleFavourite,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBoxDecrypted>>,
    isDataSaved: Boolean = false,
    onClickEdit: () -> Unit = {},
    onToggleLock: (Boolean) -> Unit = {},
    onToggleFavourite: (Boolean) -> Unit = {},
) {

    val bucketItemBoxData by bucketItemBoxDataFlow.collectAsState()

    TopAppBar(
        navigationIcon = { GraIconButton.BackButton() },
        title = { },
        actions = {
            val bucketItemBox by remember(key1 = bucketItemBoxData) { derivedStateOf { (bucketItemBoxData as? DataLoader.Loaded)?.data } }
            val isLocked by remember(key1 = bucketItemBox) { derivedStateOf { bucketItemBox?.isLocked } }
            val isFavourite by remember(key1 = bucketItemBox) { derivedStateOf { bucketItemBox?.isFavourite } }

//            AnimatedVisibility(
//                visible = isDataSaved,
//                enter = AnimationDefaults.ScaleEnter,
//                exit = AnimationDefaults.ScaleExit,
//            ) {
//                GraIconButton.EditButton(onClick = onClickEdit)
//            }
            isLocked?.let { GraIconButton.LockButton(isLocked = it) { onToggleLock(!it) } }
            isFavourite?.let { GraIconButton.FavouriteButton(isFavourite = it) { onToggleFavourite(!it) } }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingTopBar() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    TopAppBar(
        navigationIcon = { GraIconButton.BackButton() },
        title = {
            Text(
                text = stringResource(id = R.string.select_data_to_edit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Button(
                shape = MaterialTheme.shapes.medium,
                onClick = { onBackPressedDispatcher?.onBackPressed() }
            ) {
                Text(text = stringResource(id = R.string.done))
            }
            Spacer(modifier = Modifier.width(width = 6.dp))
        },
        modifier = Modifier.fillMaxWidth()
    )
}
