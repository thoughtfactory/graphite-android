package com.syncodec.graphite.presentation.main.composable.bar

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnAddDebugData
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.screen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.CalendarScreen
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.main.composable.screen.HomeScreen
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.RealmUUID


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
					onClick = { if (currentRoute != screen.route) onNavigation(screen.route) },
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

@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainNavigation(
	navController : NavHostController,
	componentType : ComponentType,
	defaultNotebookId : RealmUUID?,
	chapterObject : ChapterObject?,
	notebookList : List<ChapterObject>,
	noteList : List<NoteObjectLite>,
	bucketList : List<BucketObject>,
) {
	val context = LocalContext.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val scope = rememberCoroutineScope()

	val openSheet = LocalCompositionOpenBottomSheet.current
	val closeSheet = LocalCompositionCloseBottomSheet.current

	val isVaultOpened = LocalVaultIsOpened.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelect.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.TIMESTAMP)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.DESCENDING)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.LIST)

	val onDelete = LocalCompositionOnDelete.current

	val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner" }

	val addDebugData = LocalCompositionOnAddDebugData.current

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.let {
				val hasIntentAction = it.hasExtra(Extra.Companion.Constant.INTENT_ACTION.name)
				if (hasIntentAction) {
					val intentAction = it.getStringExtra(Extra.Companion.Constant.INTENT_ACTION.name)?.let { it1 ->
						Extra.Companion.IntentAction.valueOf(it1)
					}
					if (intentAction == Extra.Companion.IntentAction.DELETE) {
						val hasObjectId = it.hasExtra(Extra.Companion.Constant.OBJECT_ID.name)
						if(hasObjectId) {
							val realmUUID = it.getByteArrayExtra(Extra.Companion.Constant.OBJECT_ID.name)?.let { RealmUUID.from(it) }
							if (realmUUID != null) {
								selectedRealmUUIDList.add(realmUUID)
								onDelete()
							}
						}
					}
				}
				Extra.Companion.Constant.INTENT_ACTION.name
				Extra.Companion.Constant.OBJECT_ID.name
			}
		} catch (e: Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	NavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Home.route,
	) {
		composable(BottomNavigationItem.Home.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				HomeScreen(
					componentType = componentType,
					noteList = noteList.filter { if (it.isLocked) isVaultOpened else true },
					bucketList = bucketList.filter { if (it.isLocked) isVaultOpened else true },
					notebookList = notebookList.filter { it.parentChapterId == null },
					sortOn = sortOn,
					sortBy = sortBy,
					viewType = viewType,
					onClickFab = {
						when (componentType) {
							ComponentType.NOTE -> {
								Intent(context, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Constant.IS_NEW.name, true)
//          						TODO    Check if notebookId is not null
									putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId?.bytes)
									putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

									activityLauncher.launch(this)
								}
//								addDebugData()
							}

							ComponentType.BUCKET -> openSheet(MainBottomSheetType.BUCKET)
							ComponentType.NOTEBOOK -> openSheet(MainBottomSheetType.NOTEBOOK)
						}
					},
					onClickNote = {
						if (isSelected) {
							onSelected(true)
							if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
							else selectedRealmUUIDList.add(it)
						} else {
							Intent(context, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.IS_NEW.name, false)
								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId?.bytes)
								putExtra(Extra.Companion.Constant.NOTE_ID.name, it.bytes)
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

								activityLauncher.launch(this)
							}
						}
					},
					onLongClickNote = {
						onSelected(true)
						if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
						else selectedRealmUUIDList.add(it)
					},
					onClickBucket = {
						if (isSelected) {
							onSelected(true)
							if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
							else selectedRealmUUIDList.add(it)
						} else {
							Intent(context, BucketActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.BUCKET_ID.name, it.bytes)
								activityLauncher.launch(this)
							}
						}
					},
					onLongClickBucket = {
						onSelected(true)
						if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
						else selectedRealmUUIDList.add(it)
					},
					onClickNotebook = {
						if (isSelected) {
							onSelected(true)
							if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
							else selectedRealmUUIDList.add(it)
						} else {
							Intent(context, NotebookActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, it.bytes)
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)
								activityLauncher.launch(this)
							}
						}
					},
					onLongClickNotebook = {
						onSelected(true)
						if (selectedRealmUUIDList.contains(it)) selectedRealmUUIDList.remove(it)
						else selectedRealmUUIDList.add(it)
					}
				)
			}
		}
		composable(BottomNavigationItem.Calendar.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				CalendarScreen(
					noteList = noteList.filter { if (it.isLocked) isVaultOpened else true },
					selectedItemList = emptyList(),
					onClickNote = {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId?.bytes)
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.bytes)
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							activityLauncher.launch(this)
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
					noteList = noteList.filter { if (it.isLocked) isVaultOpened else true },
					onClickNote = {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, defaultNotebookId?.bytes)
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it?.bytes)
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

							activityLauncher.launch(this)
						}
					},
					onLongClickNote = {}
				)
			}
		}
	}
}
