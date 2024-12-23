package com.syncodec.graphite.presentation.main2.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.syncodec.graphite.presentation.common.button.GraIconButton
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabItem
import com.syncodec.graphite.presentation.ui.ANIMATION_TIME


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    homeScreenData: HomeScreenData = HomeScreenData.NoteScreenData,
    selecting: Boolean = false,
    selectedSize: Int = 0,
    onClickSearch: () -> Unit = {},
    onClickMenu: () -> Unit = {},
    onClickNavigationButton: (HomeScreenData) -> Unit = {}
) {
    val context = LocalContext.current

    val tabItemList = remember {
        listOf(
            TabItem(text = context.getString(R.string.note), icon = R.drawable.ic_fa_note) { onClickNavigationButton(HomeScreenData.NoteScreenData) },
            TabItem(text = context.getString(R.string.bucket), icon = R.drawable.ic_fa_bucket) { onClickNavigationButton(HomeScreenData.BucketScreenData) },
            TabItem(text = context.getString(R.string.notebook), icon = R.drawable.ic_fa_notebook) { onClickNavigationButton(HomeScreenData.NotebookScreenData) },
        )
    }

    Column {
        Crossfade(
            targetState = selecting,
            animationSpec = tween(ANIMATION_TIME)
        ) {
            if (it) SelectingTopBar(selectedSize = selectedSize)
            else NormalTopBar(
                onClickSearch = onClickSearch,
                onClickMenu = onClickMenu
            )
        }

        GenericTabRow(
            tabItemList = tabItemList,
            selectedTabIndex = homeScreenData.index,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
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
                GraIconButton.VaultButton(checked = false, onClick = {})
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
