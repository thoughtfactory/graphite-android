package com.syncodec.graphite.presentation.main

import android.util.Log
import com.syncodec.graphite.R


sealed class MainComponent(val index: Int, val route: String, val icon: Int, val iconFilled: Int, val title: Int) {
	data object Home : MainComponent(index = 0, route = "home", icon = R.drawable.ic_fa_home, iconFilled = R.drawable.ic_fa_home_solid, title = R.string.home)
	data object Calendar : MainComponent(index = 1, route = "calendar", icon = R.drawable.ic_fa_calendar, iconFilled = R.drawable.ic_fa_calendar_solid, title = R.string.calendar)
	data object Atlas : MainComponent(index = 2, route = "atlas", icon = R.drawable.ic_fa_atlas, iconFilled = R.drawable.ic_fa_atlas_solid, title = R.string.atlas)
}

sealed class HomeComponent(val index: Int, val route: String, val icon: Int, val iconFilled: Int, val title: Int) {

	data object Note : HomeComponent(index = 0, route = "home/note", icon = R.drawable.ic_fa_note_duotone, iconFilled = R.drawable.ic_fa_note_duotone, title = R.string.note)
	data object Bucket : HomeComponent(index = 1, route = "home/bucket", icon = R.drawable.ic_fa_bucket_list_duotone, iconFilled = R.drawable.ic_fa_bucket_list_duotone, title = R.string.bucket)
	data object Notebook : HomeComponent(index = 2, route = "home/notebook", icon = R.drawable.ic_fa_notebook_duotone, iconFilled = R.drawable.ic_fa_notebook_duotone, title = R.string.notebook)

	companion object {
		fun fromRoute(route: String?): HomeComponent? = when (route) {
			Note.route -> Note
			Bucket.route -> Bucket
			Notebook.route -> Notebook
			else -> null
		}
	}
}
