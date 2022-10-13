package com.syncodec.graphite.presentation.main.composable.bar

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.presentation.main.MainViewModel
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.screen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.CalendarScreen
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.main.composable.screen.HomeScreen
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalModalBottomSheetState
import com.syncodec.graphite.utils.LocalModalBottomSheetType
import com.syncodec.graphite.utils.LocalSetModalBottomSheetType
import com.syncodec.graphite.utils.timestampToCalendarDay
import com.syncodec.graphite.utils.tone
import kotlinx.coroutines.launch


open class BottomNavigationItem(var route : String, var icon : Int, var title : String) {
	object Home : BottomNavigationItem("home", R.drawable.ic_home, "Home")
	object Calendar : BottomNavigationItem("calendar", R.drawable.ic_calendar, "Calendar")
	object Atlas : BottomNavigationItem("atlas", R.drawable.ic_atlas, "Atlas")
}

@Composable
fun BottomNavigationBar(
	currentRoute : String?,
	onNavigation : (String) -> Unit
) {
	val screens = listOf(
		BottomNavigationItem.Home,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
	)

	NavigationBar(
		containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		tonalElevation = 0.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		screens.forEach { screen ->
			NavigationBarItem(
				onClick = { onNavigation(screen.route) },
				icon = {
					Icon(
						painter = painterResource(id = screen.icon),
						contentDescription = screen.title,
						modifier = Modifier.requiredSize(20.dp)
					)
				},
				label = {
					Text(
						text = screen.title,
						textAlign = TextAlign.Center,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						lineHeight = 12.sp
					)
				},
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = MaterialTheme.colorScheme.onPrimary,
					unselectedIconColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
					selectedTextColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
					unselectedTextColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
					indicatorColor = MaterialTheme.colorScheme.primary
				),
				selected = currentRoute == screen.route,
				interactionSource = remember { MutableInteractionSource() },
				modifier = Modifier,
			)
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainNavigation(
	navController : NavHostController,
	componentType : ComponentType
) {
	val activity : MainActivity = LocalContext.current as MainActivity
	val scope = rememberCoroutineScope()
	val viewModel : MainViewModel = viewModel()

	val modalBottomSheetState = LocalModalBottomSheetState.current

	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }
	val openSheet = { scope.launch { modalBottomSheetState.show() } }
	val modalBottomSheetType = LocalModalBottomSheetType.current
	val setModalBottomSheetType = LocalSetModalBottomSheetType.current

	val viewType by viewModel.viewType

	val defaultNotebookId by viewModel.defaultNotebookId
	val chapterObject by viewModel.chapterObject
//	val noteObjectList = chapterObject?.noteList?.toList()?.map { it.toLite() } ?: emptyList()
	val notebookList by viewModel.notebookList.collectAsState(initial = listOf())

	val noteObjectList = viewModel.noteList

	val bucketList by viewModel.bucketObjectList.collectAsState(initial = null)


	val noteDayMap : MutableMap<Long, MutableList<NoteObjectLite>> = mutableMapOf()
	noteObjectList.forEach { note ->
		val timestamp = timestampToCalendarDay(note.userTimestamp)
		if (noteDayMap.containsKey(timestamp)) noteDayMap[timestamp] !!.add(note)
		else noteDayMap[timestamp] = mutableListOf(note)
	}

	val viewModelStoreOwner =
		checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner" }
	AnimatedNavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Home.route,
		enterTransition = { fadeIn(tween(600)) },
		exitTransition = { fadeOut(tween(600)) },
	) {
		composable(BottomNavigationItem.Home.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				HomeScreen(
					componentType = componentType,
					notebookId = defaultNotebookId,
					noteDayMap = noteDayMap,
					bucketList = bucketList,
					notebookList = notebookList.filter { it.parentChapterId == null },
					viewType = viewType,
					onClickFab = {
						when (componentType) {
							ComponentType.NOTE -> {
								Intent(activity, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Constant.IS_NEW.name, true)
//          						TODO    Check if notebookId is not null
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
									putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)
									activity.startActivity(this)
								}
							}

							ComponentType.BUCKET -> {
								setModalBottomSheetType(MainBottomSheetType.BUCKET.ordinal)
								openSheet()
							}

							ComponentType.NOTEBOOK -> {
								setModalBottomSheetType(MainBottomSheetType.NOTEBOOK.ordinal)
								openSheet()
							}
						}
					},
					onClickNote = {
						Intent(activity, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

							activity.startActivity(this)
						}
					},
					onLongClickNote = {},
					onClickBucket = {
						Intent(activity, BucketActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.BUCKET_ID.name, it.toString())

							activity.startActivity(this)
						}
					},
					onLongClickBucket = {},
					onClickNotebook = {
						Intent(activity, NotebookActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

							activity.startActivity(this)
						}
					},
					onLongClickNotebook = {}
				)
			}
		}
		composable(BottomNavigationItem.Calendar.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				CalendarScreen(
					noteList = noteObjectList,
					selectedItemList = emptyList(),
					onClickNote = {
						Intent(activity, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							activity.startActivity(this)
						}
					},
					onLongClickNote = {},
				)
			}
		}
		composable(BottomNavigationItem.Atlas.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				AtlasScreen(
					noteList = noteObjectList,
					onClickNote = {
						Intent(activity, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							activity.startActivity(this)
						}
					},
					onLongClickNote = {}
				)
			}
		}
	}
}
