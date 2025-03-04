package com.syncodec.graphite.presentation.bucket2.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToAlphaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaSelectedIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToGammaText
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.GraIconButton
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabItem
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults

@Composable
fun TopBar(
    title: String? = null,
    bucketType: BucketBox.BucketType = BucketBox.BucketType.Unknown,
    stateFilterInt: Int = 0,
    isLocked: Boolean = false,
    isFavourite: Boolean = false,
    onUpdateFilterInt: (Int) -> Unit = {},
    onClickFavourite: (Boolean) -> Unit = {},
    onClickLock: (Boolean) -> Unit = {},
) {

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    Column {
        Crossfade(
            targetState = isSelecting,
            animationSpec = AnimationDefaults.stateAnimationSpec()
        ) {
            if (it) SelectingTopBar(selectedSize = selectedItemIdList.size)
            else NormalTopBar(
                title = title,
                isLocked = isLocked,
                isFavourite = isFavourite,
                onClickLock = onClickLock,
                onClickFavourite = onClickFavourite
            )
        }

        Spacer(modifier = Modifier.height(height = 4.dp))

        BucketItemStateFilter(
            visible = !isSelecting,
            bucketType = bucketType,
            stateFilterInt = stateFilterInt,
            onUpdateFilterInt = onUpdateFilterInt
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    title: String? = null,
    isLocked: Boolean = false,
    isFavourite: Boolean = false,
    onClickLock: (Boolean) -> Unit = {},
    onClickFavourite: (Boolean) -> Unit = {},
) {
    TopAppBar(
        navigationIcon = { GraIconButton.BackButton() },
        title = { Text(text = title ?: "") },
        actions = {
            GraIconButton.LockButton(isLocked = isLocked) { onClickLock(!isLocked) }
            GraIconButton.FavouriteButton(isFavourite = isFavourite) { onClickFavourite(!isFavourite) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectingTopBar(
    selectedSize: Int = 0,
) {
    TopAppBar(
        title = {
            AnimatedText(
                text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "$selectedSize items selected",
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        navigationIcon = { GraIconButton.BackButton() },
        actions = {},
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceBright, titleContentColor = MaterialTheme.colorScheme.onSurface),

        )
}

@Composable
private fun BucketItemStateFilter(
    visible: Boolean = true,
    bucketType: BucketBox.BucketType = BucketBox.BucketType.Unknown,
    stateFilterInt: Int = 0,
    onUpdateFilterInt: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val tabItemList: List<TabItem> by remember(key1 = bucketType) {
        derivedStateOf {
            listOf(
                TabItem(text = context.getString(R.string.all), icon = R.drawable.ic_fa_circle_dot, selectedIcon = R.drawable.ic_fa_circle_dot_duotone) { onUpdateFilterInt(0) },
                TabItem(text = context.getString(bucketType.bucketTypeToAlphaText()), icon = R.drawable.ic_fa_clock, selectedIcon = R.drawable.ic_fa_clock_duotone) { onUpdateFilterInt(1) },
                TabItem(text = context.getString(bucketType.bucketTypeToBetaText()), icon = bucketType.bucketTypeToBetaIcon(), selectedIcon = bucketType.bucketTypeToBetaSelectedIcon()) { onUpdateFilterInt(2) },
                TabItem(text = context.getString(bucketType.bucketTypeToGammaText()), icon = R.drawable.ic_fa_circle_check, selectedIcon = R.drawable.ic_fa_circle_check_duotone) { onUpdateFilterInt(3) },
            )
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = AnimationDefaults.ExpandVerticallyEnter,
        exit = AnimationDefaults.ShrinkVerticallyExit
    ) {
        GenericTabRow(
            tabItemList = tabItemList,
            selectedTabIndex = stateFilterInt,
            hideUnselectedIcon = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }
}

