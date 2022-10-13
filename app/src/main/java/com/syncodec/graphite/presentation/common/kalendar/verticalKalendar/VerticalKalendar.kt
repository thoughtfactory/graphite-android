package com.syncodec.graphite.presentation.common.kalendar.verticalKalendar

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.joda.time.DateTime
import org.joda.time.Years


@Composable
fun VerticalKalendar() {

	val minYear = DateTime(1971, 5, 4, 0, 0)
	val maxYear = DateTime(2071, 5, 4, 0, 0)

//	Years.yearsIn(minYear, maxYear)
//
//	Years.yearsBetween("1971", "2071")
//
//
//
//	Log.i("npr71", "min year : ${minYear}")

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {

	}

}
