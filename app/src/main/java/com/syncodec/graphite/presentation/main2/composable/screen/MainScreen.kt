package com.syncodec.graphite.presentation.main2.composable.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.scaffold2.GenericScaffold2
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectionContainerActor
import com.syncodec.graphite.presentation.main2.MainViewModel2
import com.syncodec.graphite.presentation.main2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.main2.composable.bar.HomeScreenData
import com.syncodec.graphite.presentation.main2.composable.bar.MainScreenData
import com.syncodec.graphite.presentation.main2.composable.bar.TopBar
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.NewBucketBottomSheet
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.NewNotebookBottomSheet
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.HomeFloatingActionButton
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel2: MainViewModel2 = koinViewModel()
) {

    val navController: NavHostController = rememberNavController()

    val selectionContainerActor = remember { SelectionContainerActor() }
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val hazeState = remember { HazeState() }

    val newNotebookBottomSheet = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val newBucketListBottomSheet = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)

    val allChapterBoxListFlow: Flow<List<ChapterBox>> = mainViewModel2.allChapterBoxListFlow
    val allBucketBoxListFlow: Flow<List<BucketBox>> = mainViewModel2.allBucketBoxListFlow

    val currentBackStackList by navController.currentBackStack.collectAsState()

    GenericScaffold2(
        topBar = {
            TopBar(
                currentBackStackRoute = currentBackStackList.lastOrNull()?.destination?.route,
                onClickSearch = {},
                onClickMenu = {},
                onClickNavigationButton = { navController.navigate(route = it.route) { launchSingleTop = true; popUpTo("note_screen") { inclusive = false } } }
            )
        },
        bottomBar = {
            BottomBar(
                currentBackStackRoute = currentBackStackList.lastOrNull()?.destination?.route,
                onClickNavigationButton = { navController.navigate(route = it.route) { launchSingleTop = true; popUpTo("note_screen") { inclusive = false } } }
            )
        },
        floatingActionButton = {
            HomeFloatingActionButton(
                currentBackStackRoute = currentBackStackList.lastOrNull()?.destination?.route,
                isExpanded = true,
                onClickNewNote = {},
                onClickNewBucket = { newBucketListBottomSheet.openSheet() },
                onClickNewNotebook = { newNotebookBottomSheet.openSheet() },
            )
        },
        bottomSheetContent = {
            NewBucketBottomSheet(
                bottomSheet2State = newBucketListBottomSheet,
                outerHazeState = hazeState,
                onCreateNewBucket = { mainViewModel2.putBucketBox(bucketBox = it) }
            )
            NewNotebookBottomSheet(
                bottomSheet2State = newNotebookBottomSheet,
                outerHazeState = hazeState,
                onCreateNewNotebook = { mainViewModel2.putChapterBox(chapterBox = it) }
            )
        },
        hazeState = hazeState,
        compositionLocalValues = listOf(
            LocalSelectionContainerActor provides selectionContainerActor
        )
    ) {

        NavHost(
            navController = navController,
            startDestination = MainScreenData.HomeScreenData.route,
            enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = AnimationDefaults.stateAnimationSpec(), initialOffset = { it / 4 }) + AnimationDefaults.FadeEnter },
            exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = AnimationDefaults.stateAnimationSpec(), targetOffset = { it / 4 }) + AnimationDefaults.FadeExit }
        ) {

            navigation(route = MainScreenData.HomeScreenData.route, startDestination = HomeScreenData.NoteScreenData.route) {
                composable(route = HomeScreenData.NoteScreenData.route) {
                    NoteScreen()
                }
                composable(route = HomeScreenData.BucketScreenData.route) {
                    BucketScreen(allBucketBoxListFlow = allBucketBoxListFlow)
                }
                composable(route = HomeScreenData.NotebookScreenData.route) {
                    NotebookScreen(allChapterBoxListFlow = allChapterBoxListFlow)
                }
            }

            composable(route = MainScreenData.CalendarScreenData.route) {
                CalendarScreen()
            }

            composable(route = MainScreenData.AtlasScreenData.route) {
                AtlasScreen()
            }
        }
    }
}
