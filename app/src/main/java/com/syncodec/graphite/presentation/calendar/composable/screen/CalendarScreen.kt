package com.syncodec.graphite.presentation.calendar.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
	Scaffold(
		topBar = {

		},
		bottomBar = {

		},
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {

		}
	}
}
