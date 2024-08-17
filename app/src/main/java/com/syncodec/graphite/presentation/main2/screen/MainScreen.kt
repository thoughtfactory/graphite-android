package com.syncodec.graphite.presentation.main2.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.SCALE_AND_FADE_TRANSFORM
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main2.HomeComponent
import com.syncodec.graphite.presentation.main2.MainComponent
import com.syncodec.graphite.presentation.main2.bar.BottomBar
import com.syncodec.graphite.presentation.main2.bar.HomeTabNavigator
import com.syncodec.graphite.presentation.main2.bar.TopBar
import com.syncodec.graphite.presentation.main2.buildingBlock.HomeScreenFloatingButton
import com.syncodec.graphite.presentation.main2.model.NoteScreenViewModel2
import com.syncodec.graphite.presentation.main2.screen.note.NoteScreen
import com.syncodec.graphite.utils.ActivityUtil
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current

    val noteScreenViewModel2: NoteScreenViewModel2 = koinViewModel()

    val defaultChapterId by noteScreenViewModel2.defaultChapterIdFlow.collectAsState(null)
    val noteList by noteScreenViewModel2.noteListFlow.collectAsState(listOf())

    var currentMainRoute by remember { mutableStateOf<MainComponent>(MainComponent.Home) }
    var currentHomeRoute by remember { mutableStateOf<HomeComponent>(HomeComponent.Note) }

    GenericScaffold2(
        topBar = {
            TopBar(
                onClickMenu = {},
                onClickSearchButton = {}
            )
        },
        bottomBar = {
            BottomBar(
                currentRoute = currentMainRoute,
                onNavigation = { currentMainRoute = it }
            )
        },
        floatingActionButton = {
            HomeScreenFloatingButton(
                currentHomeRoute = currentHomeRoute,
                currentMainRoute = currentMainRoute,
                isExpanded = true,
                onClickNewNote = {
                    defaultChapterId?.let { parentId -> ActivityUtil.launchNewNoteActivity(context = context, parentId = parentId) }
                        ?: Toast.makeText(context, context.getString(R.string.toast_parent_id_not_available), Toast.LENGTH_SHORT).show()
                },
                onClickNewBucket = {},
                onClickNewNotebook = {},
            )
        }
    ) {
        AnimatedContent(
            targetState = currentMainRoute,
            transitionSpec = { SCALE_AND_FADE_TRANSFORM },
            label = "currentMainRoute_animation"
        ) { currentMainRoute1 ->
            when (currentMainRoute1) {
                is MainComponent.Home -> Column {

                    HomeTabNavigator(
                        currentRoute = currentHomeRoute,
                        onNavigate = {
                            currentHomeRoute = it
                            Log.d("HomeTabNavigator", "$it")
                        }
                    )

                    AnimatedContent(
                        targetState = currentHomeRoute,
                        transitionSpec = { SCALE_AND_FADE_TRANSFORM },
                        label = "currentHomeRoute_animation"
                    ) { currentHomeRoute1 ->
                        when (currentHomeRoute1) {
                            is HomeComponent.Note -> NoteScreen(noteList)
                            is HomeComponent.Bucket -> NoteScreen(noteList)
                            is HomeComponent.Notebook -> NoteScreen(noteList)
                        }
                    }
                }

                is MainComponent.Calendar -> Unit

                is MainComponent.Atlas -> Unit
            }
        }
    }
}
