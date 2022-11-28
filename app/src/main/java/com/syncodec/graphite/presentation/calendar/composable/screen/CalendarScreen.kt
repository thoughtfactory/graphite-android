package com.syncodec.graphite.presentation.calendar.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.calendar.composable.bar.BottomBar
import com.syncodec.graphite.presentation.calendar.composable.bar.TopBar
import com.syncodec.graphite.presentation.calendar.composable.bottomSheet.NoteBottomSheet
import com.syncodec.graphite.presentation.calendar.composable.buildingBlock.CalendarView
import com.syncodec.graphite.presentation.calendar.composable.dialog.CalendarDialog
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.utils.LocalVaultIsOpened
import io.realm.kotlin.types.RealmUUID
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterialApi::class
)
@Composable
fun CalendarScreen(
	parentChapter : ChapterObjectLite?,
	noteList : List<NoteObjectLite>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
	onBackPressed : () -> Unit,
) {

	val isVaultOpened = LocalVaultIsOpened.current

	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	var selectedDate by remember { mutableStateOf(LocalDate.now(Clock.systemDefaultZone())) }
	val noteDayMap = mutableMapOf<LocalDate, MutableList<NoteObjectLite>>()
	noteList.filter { if (it.isLocked) isVaultOpened else true }.forEach { note ->
		val date = LocalDate.now(Clock.fixed(Instant.ofEpochMilli(note.userTimestamp), ZoneId.systemDefault()))
		if (noteDayMap.containsKey(date)) noteDayMap[date]?.add(note) else noteDayMap[date] = mutableListOf(note)
	}

	Scaffold(
		topBar = { TopBar(onBackPressed = onBackPressed) },
		bottomBar = { BottomBar(parentChapter = parentChapter) }
	) {
		BottomSheetScaffold(
			sheetContent = {
				NoteBottomSheet(
					noteList = noteDayMap[selectedDate] ?: listOf(),
					selectedDate = selectedDate,
					selectedItemList = selectedObjectIdList,
					onClickNote = onClickNote,
					onLongClickNote = onLongClickNote,
				)
			},
			backgroundColor = MaterialTheme.colorScheme.background,
			sheetBackgroundColor = MaterialTheme.colorScheme.surface,
			sheetElevation = 32.dp,
			sheetPeekHeight = 64.dp,
			modifier = Modifier
				.fillMaxSize()
				.padding(it),
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				CalendarView(
					noteDayMapSize = noteDayMap.mapValues { it.value.size }
				) { it?.let { selectedDate = it } }
			}
		}
	}

	CalendarDialog(parentChapter = parentChapter)
}
