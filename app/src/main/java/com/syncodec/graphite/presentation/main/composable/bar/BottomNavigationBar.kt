package com.syncodec.graphite.presentation.main.composable.bar

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnSelected
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.screen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.CalendarScreen
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.main.composable.screen.HomeScreen
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.timestampToCalendarDay
import com.syncodec.graphite.utils.tone
import io.realm.kotlin.types.ObjectId


open class BottomNavigationItem(val route : String, val icon : Int, val title : String) {
	object Home : BottomNavigationItem("home", R.drawable.ic_home, "Home")
	object Calendar : BottomNavigationItem("calendar", R.drawable.ic_calendar, "Calendar")
	object Atlas : BottomNavigationItem("atlas", R.drawable.ic_atlas, "Atlas")
}

@Composable
fun BottomNavigationBar(
	currentRoute : String?,
	onNavigation : (String) -> Unit
) {
	val isSelected = LocalCompositionIsSelected.current

	val screens = listOf(
		BottomNavigationItem.Home,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
	)

	AnimatedVisibility(
		visible = ! isSelected,
		enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
		exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }),
	) {
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
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainNavigation(
	navController : NavHostController,
	componentType : ComponentType,
	viewType : ViewType,
	defaultNotebookId : ObjectId?,
	chapterObject : ChapterObject?,
	notebookList : List<ChapterObject>,
	noteList : List<NoteObjectLite>,
	bucketList : List<BucketObject>,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val openSheet = LocalCompositionOpenBottomSheet.current
	val closeSheet = LocalCompositionCloseBottomSheet.current

	val noteDayMap : MutableMap<Long, MutableList<NoteObjectLite>> = mutableMapOf()
	noteList.forEach { note ->
		val timestamp = timestampToCalendarDay(note.userTimestamp)
		if (noteDayMap.containsKey(timestamp)) noteDayMap[timestamp] !!.add(note)
		else noteDayMap[timestamp] = mutableListOf(note)
	}

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelected.current
	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner" }
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
					noteDayMap = noteDayMap,
					bucketList = bucketList,
					notebookList = notebookList.filter { it.parentChapterId == null },
					viewType = viewType,
					onClickFab = {
						when (componentType) {
							ComponentType.NOTE -> {
								Intent(context, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Constant.IS_NEW.name, true)
//          						TODO    Check if notebookId is not null
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
									putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)
									context.startActivity(this)
								}
							}

							ComponentType.BUCKET -> openSheet(MainBottomSheetType.BUCKET)
							ComponentType.NOTEBOOK -> openSheet(MainBottomSheetType.NOTEBOOK)
						}
					},
					onClickNote = {
						if (isSelected) {
							onSelected(true)
							if (selectedObjectIdList.contains(it)) selectedObjectIdList.remove(it)
							else selectedObjectIdList.add(it)
						} else {
							Intent(context, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.IS_NEW.name, false)
								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
								putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

								context.startActivity(this)
							}
						}
					},
					onLongClickNote = {
						onSelected(true)
						if (selectedObjectIdList.contains(it)) selectedObjectIdList.remove(it)
						else selectedObjectIdList.add(it)
					},
					onClickBucket = {
						Intent(context, BucketActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.BUCKET_ID.name, it.toString())

							context.startActivity(this)
						}
					},
					onLongClickBucket = {},
					onClickNotebook = {
						Intent(context, NotebookActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

							context.startActivity(this)
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
					noteList = noteList,
					selectedItemList = emptyList(),
					onClickNote = {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							context.startActivity(this)
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
					noteList = noteList,
					onClickNote = {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							context.startActivity(this)
						}
					},
					onLongClickNote = {}
				)
			}
		}
	}
}
