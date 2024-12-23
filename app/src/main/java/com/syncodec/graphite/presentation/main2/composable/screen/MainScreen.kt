package com.syncodec.graphite.presentation.main2.composable.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.scaffold2.GenericScaffold2
import com.syncodec.graphite.presentation.main2.MainViewModel2
import com.syncodec.graphite.presentation.main2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.main2.composable.bar.HomeScreenData
import com.syncodec.graphite.presentation.main2.composable.bar.MainScreenData
import com.syncodec.graphite.presentation.main2.composable.bar.TopBar
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.NewNotebookBottomSheet
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.HomeFloatingActionButton
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel2: MainViewModel2 = koinViewModel()
) {

    var mainScreenData: MainScreenData by remember { mutableStateOf(MainScreenData.HomeScreenData) }
    var homeScreenData: HomeScreenData by remember { mutableStateOf(HomeScreenData.NoteScreenData) }

    val navController: NavHostController = rememberNavController()

    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val newNotebookBottomSheet = GenericBottomSheet2State.initialize()
    val newBucketListBottomSheet = GenericBottomSheet2State.initialize()

    val allChapterBoxListFlow = mainViewModel2.allChapterBoxListFlow

    GenericScaffold2(
        topBar = {
            TopBar(
                homeScreenData = homeScreenData,
                selecting = false,
                selectedSize = 0,
                onClickSearch = {},
                onClickMenu = {},
                onClickNavigationButton = { homeScreenData = it; navController.navigate(it.route) }
            )
        },
        bottomBar = {
            BottomBar(
                mainScreenData = mainScreenData,
                onClickNavigationButton = { mainScreenData = it; navController.navigate(it.route) }
            )
        },
        floatingActionButton = {
            HomeFloatingActionButton(
                homeScreenData = homeScreenData,
                isExpanded = true,
                onClickNewNote = {},
                onClickNewBucket = {},
                onClickNewNotebook = { newNotebookBottomSheet.openSheet() },
            )
        },
        bottomSheetContent = {
            NewNotebookBottomSheet(
                bottomSheet2State = newNotebookBottomSheet,
                onCreateNewNotebook = { mainViewModel2.putChapter(it) }
            )
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = MainScreenData.HomeScreenData.route
        ) {

            navigation(route = MainScreenData.HomeScreenData.route, startDestination = HomeScreenData.NoteScreenData.route) {
                composable(route = HomeScreenData.NoteScreenData.route) {
                    NoteScreen()
                }
                composable(route = HomeScreenData.BucketScreenData.route) {
                    BucketScreen()
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
