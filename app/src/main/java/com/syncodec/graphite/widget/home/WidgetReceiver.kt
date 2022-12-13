package com.syncodec.graphite.widget.home

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.Repository2
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest


class WidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

@HiltWorker
internal class DailyReadWorkerTask @AssistedInject constructor(
	@Assisted private val context: Context,
	@Assisted workParams: WorkerParameters,
	private val repository2 : Repository2
) : CoroutineWorker(context, workParams) {

	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	init {
		Log.i("npr71", "DailyReadWorkerTask init")
	}

	override suspend fun doWork(): Result {
		Log.i("npr71", "widget: doWork")
		repository2.isAuthenticated.value = true
		repository2.getDefaultChapterId().collectLatest {
			repository2.getChapterFromIdAsFlow(it).collect { notebook ->
				updateWidget(notebook?.noteList?.map { it.toLite() } ?: listOf())
				Result.success()
			}
		}
		return try {
			// Update the widget's text content
			Result.success()
		} catch(e: Exception) {
			Result.retry()
		}
	}

	suspend fun updateWidget(noteList: List<NoteObjectLite>) {
		// Iterate through all the available glance id's.
		GlanceAppWidgetManager(context).getGlanceIds(HomeWidget::class.java).forEach { glanceId ->
			updateAppWidgetState(context, glanceId) { prefs ->
				prefs.clear()
				noteList.forEach {
					prefs[stringPreferencesKey(it.id.toString())] = objectMapper.writeValueAsString(it)
				}
			}
		}
		HomeWidget().updateAll(context)
	}
}
