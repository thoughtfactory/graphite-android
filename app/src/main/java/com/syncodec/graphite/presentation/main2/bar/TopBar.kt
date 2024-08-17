package com.syncodec.graphite.presentation.main2.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.common.scaffold.LocalIsSelecting


@Composable
fun TopBar(
    onClickMenu: () -> Unit,
    onClickSearchButton: () -> Unit,
) {

    val isSelecting = LocalIsSelecting.current

    Crossfade(
        targetState = isSelecting,
        animationSpec = tween(ANIMATION_DURATION_MILLIS),
        label = "isSelecting_animation",
    ) {
        if (it) SelectingTopBar()
        else PrimaryTopBar(
            onClickMenu = onClickMenu,
            onClickSearchButton = onClickSearchButton
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryTopBar(
    onClickMenu: () -> Unit,
    onClickSearchButton: () -> Unit,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Row(
                modifier = Modifier
            ) {
                MenuButton(onClick = onClickMenu)
                VaultButton()
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
            )
        },
        actions = {
            SearchButton(onClick = onClickSearchButton)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun SelectingTopBar() {

}
