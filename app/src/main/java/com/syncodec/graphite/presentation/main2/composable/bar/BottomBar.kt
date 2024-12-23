package com.syncodec.graphite.presentation.main2.composable.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


sealed class MainScreenData {
    abstract val index: Int
    abstract val icon: Int
    abstract val iconFilled: Int
    abstract val label: Int
    abstract val route: String

    data object HomeScreenData : MainScreenData() {
        override val index: Int = 0
        override val icon: Int = R.drawable.ic_fa_home
        override val iconFilled: Int = R.drawable.ic_fa_home_solid
        override val label: Int = R.string.home
        override val route: String = "home_screen"
    }

    data object CalendarScreenData : MainScreenData() {
        override val index: Int = 1
        override val icon: Int = R.drawable.ic_fa_calendar
        override val iconFilled: Int = R.drawable.ic_fa_calendar_solid
        override val label: Int = R.string.calendar
        override val route: String = "calendar_screen"
    }

    data object AtlasScreenData : MainScreenData() {
        override val index: Int = 2
        override val icon: Int = R.drawable.ic_fa_atlas
        override val iconFilled: Int = R.drawable.ic_fa_atlas_solid
        override val label: Int = R.string.atlas
        override val route: String = "atlas_screen"
    }
}

sealed class HomeScreenData {
    abstract val index: Int
    abstract val icon: Int
    abstract val iconFilled: Int
    abstract val label: Int
    abstract val route: String

    data object NoteScreenData : HomeScreenData() {
        override val index: Int = 0
        override val icon: Int = R.drawable.ic_fa_note
        override val iconFilled: Int = R.drawable.ic_fa_note_solid
        override val label: Int = R.string.note
        override val route: String = "note_screen"
    }

    data object BucketScreenData : HomeScreenData() {
        override val index: Int = 1
        override val icon: Int = R.drawable.ic_fa_bucket
        override val iconFilled: Int = R.drawable.ic_fa_bucket_solid
        override val label: Int = R.string.note
        override val route: String = "bucket_screen"
    }

    data object NotebookScreenData : HomeScreenData() {
        override val index: Int = 2
        override val icon: Int = R.drawable.ic_fa_notebook
        override val iconFilled: Int = R.drawable.ic_fa_notebook_solid
        override val label: Int = R.string.notebook
        override val route: String = "notebook_screen"
    }
}

@Composable
fun BottomBar(
    mainScreenData: MainScreenData,
    onClickNavigationButton: (MainScreenData) -> Unit
) {

    val screenList = remember { listOf(MainScreenData.HomeScreenData, MainScreenData.CalendarScreenData, MainScreenData.AtlasScreenData) }

    Column {
        HorizontalDivider()

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            screenList.forEach { screenData ->
                val selected by remember(mainScreenData) { derivedStateOf { mainScreenData == screenData } }
                NavigationBarItem(
                    selected = selected,
                    icon = { Icon(painter = painterResource(if (selected) screenData.iconFilled else screenData.icon), contentDescription = null, modifier = Modifier.size(18.dp)) },
                    label = { Text(text = stringResource(screenData.label)) },
                    onClick = { onClickNavigationButton(screenData) }
                )
            }
        }
    }
}
