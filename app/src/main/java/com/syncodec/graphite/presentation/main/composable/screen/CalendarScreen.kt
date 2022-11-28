package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.calendar.composable.bottomSheet.NoteBottomSheet
import com.syncodec.graphite.presentation.calendar.composable.buildingBlock.CalendarView
import com.syncodec.graphite.utils.LocalVaultIsOpened
import io.realm.kotlin.types.RealmUUID
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
	noteList : List<NoteObjectLite>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
) {

	val isVaultOpened = LocalVaultIsOpened.current

	var selectedDate by remember { mutableStateOf(LocalDate.now(Clock.systemDefaultZone())) }

	val noteDayMap = mutableMapOf<LocalDate, MutableList<NoteObjectLite>>()
	noteList.filter { if (it.isLocked) isVaultOpened else true }.forEach { note ->
		val date = LocalDate.now(Clock.fixed(Instant.ofEpochMilli(note.userTimestamp), ZoneId.systemDefault()))
		if (noteDayMap.containsKey(date)) noteDayMap[date]?.add(note) else noteDayMap[date] = mutableListOf(note)
	}

	BottomSheetScaffold(
		sheetContent = {
			NoteBottomSheet(
				noteList = noteDayMap[selectedDate] ?: listOf(),
				selectedDate = selectedDate,
				selectedItemList = emptyList(),
				onClickNote = onClickNote,
				onLongClickNote = onLongClickNote,
			)
		},
		backgroundColor = MaterialTheme.colorScheme.background,
		sheetBackgroundColor = MaterialTheme.colorScheme.surface,
		sheetElevation = 32.dp,
		sheetPeekHeight = 64.dp,
		modifier = Modifier.fillMaxSize()
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
