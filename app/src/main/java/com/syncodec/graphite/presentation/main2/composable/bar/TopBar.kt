package com.syncodec.graphite.presentation.main2.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabItem
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentBackStackRoute: String?,
    onClickSearch: () -> Unit = {},
    onClickMenu: () -> Unit = {},
    onClickFilterAndSort: () -> Unit = {},
    onClickNavigationButton: (HomeScreenData) -> Unit = {}
) {
    val context = LocalContext.current

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val tabItemList = remember {
        listOf(
            TabItem(text = context.getString(R.string.note), icon = R.drawable.ic_fa_note, selectedIcon = R.drawable.ic_fa_note_duotone) { onClickNavigationButton(HomeScreenData.NoteScreenData) },
            TabItem(text = context.getString(R.string.bucket), icon = R.drawable.ic_fa_bucket, selectedIcon = R.drawable.ic_fa_bucket_duotone) { onClickNavigationButton(HomeScreenData.BucketScreenData) },
            TabItem(text = context.getString(R.string.notebook), icon = R.drawable.ic_fa_notebook, selectedIcon = R.drawable.ic_fa_notebook_duotone) { onClickNavigationButton(HomeScreenData.NotebookScreenData) },
        )
    }

    Column {
        Crossfade(
            targetState = isSelecting,
            animationSpec = AnimationDefaults.stateAnimationSpec()
        ) {
            if (it) SelectingTopBar(selectedSize = selectedItemIdList.size)
            else NormalTopBar(
                onClickSearch = onClickSearch,
                onClickMenu = onClickMenu
            )
        }

        AnimatedVisibility(
            visible = currentBackStackRoute != MainScreenData.CalendarScreenData.route && currentBackStackRoute != MainScreenData.AtlasScreenData.route,
            enter = AnimationDefaults.ExpandVerticallyEnter,
            exit = AnimationDefaults.ShrinkVerticallyExit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {

                val selectedTabIndex by remember(key1 = currentBackStackRoute) {
                    derivedStateOf {
                        when (currentBackStackRoute) {
                            HomeScreenData.NoteScreenData.route -> 0
                            HomeScreenData.BucketScreenData.route -> 1
                            HomeScreenData.NotebookScreenData.route -> 2
                            else -> 0
                        }
                    }
                }

                GenericTabRow(
                    tabItemList = tabItemList,
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.weight(weight = 1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                GraIconButton.FilterAndSortTextButton(onClick = onClickFilterAndSort)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    onClickSearch: () -> Unit = {},
    onClickMenu: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Row(
                modifier = Modifier
            ) {
                GraIconButton.MenuButton(onClick = onClickMenu)
                GraIconButton.VaultButton()
            }
        },
        title = {
            Text(
                text = "GRAPHITE",
                modifier = Modifier,
                fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            GraIconButton.SearchButton(onClick = onClickSearch)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground),
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
        modifier = Modifier.fillMaxWidth(),
        actions = {},
    )
}
