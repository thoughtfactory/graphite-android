package com.syncodec.graphite.widget.home

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID
import java.io.File


object CustomGlanceStateDefinition : GlanceStateDefinition<Preferences> {
	override suspend fun getDataStore(context : Context, fileKey : String) : DataStore<Preferences> {
		return context.dataStore
	}

	override fun getLocation(context : Context, fileKey : String) : File {
		// Note: The Datastore Preference file resides is in the context.applicationContext.filesDir + "datastore/"
		return File(context.applicationContext.filesDir, "datastore/$fileName")
	}

	private const val fileName = "home_widget.preferences_pb"
	val Context.dataStore : DataStore<Preferences>
			by preferencesDataStore(name = fileName)
}

class HomeWidget() : GlanceAppWidget() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule().addDeserializer(
				RealmUUID::class.java,
				RealmUUIDDeserializer()
			)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	override val stateDefinition = CustomGlanceStateDefinition

	@Composable
	override fun Content() {
		val state = currentState<Preferences>()
		val noteList = state.asMap()

		Box(
			modifier = GlanceModifier
				.fillMaxSize()
				.background(day = Color(0x71ECF1F4), night = Color(0x7102060A))
		) {
			Column(
				modifier = GlanceModifier,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				LazyColumn(
					modifier = GlanceModifier
						.defaultWeight()
						.padding(8.dp)
				) {
					noteList.forEach { (key, value) ->
						var noteObjectLite : NoteObjectLite? = null
						try {
							noteObjectLite = objectMapper.readValue(value as String)
						} catch (e : Exception) {
						}

						noteObjectLite?.let {
							item {
								NoteItem(noteObjectLite = it)
							}
						}
					}
				}

				Row(
					modifier = GlanceModifier
						.fillMaxWidth()
				) {
					Spacer(modifier = GlanceModifier.width(8.dp))
					Spacer(
						modifier = GlanceModifier
							.defaultWeight()
							.height(2.dp)
							.background(Color(0xFFCEDBE6))
					)
					Spacer(modifier = GlanceModifier.width(8.dp))
				}

				Row(
					modifier = GlanceModifier
						.fillMaxWidth()
						.height(40.dp)
				) {

				}
			}
		}
	}
}

@Composable
private fun NoteItem(
	noteObjectLite : NoteObjectLite
) {
	Box(
		modifier = GlanceModifier
			.padding(2.dp)
			.clickable(
				onClick = actionRunCallback<AddWaterClickAction>(actionParametersOf(ActionParameters.Key<ByteArray>("noteId") to noteObjectLite.id.bytes))
			)
	) {
		Box(
			modifier = GlanceModifier
				.fillMaxWidth()
				.background(day = Color(0x71ECF1F4), night = Color(0x7102060A))
				.cornerRadius(12.dp)
		) {
			Column(
				modifier = GlanceModifier
					.fillMaxWidth()
					.padding(6.dp)
			) {
				Row(
					modifier = GlanceModifier.fillMaxWidth()
				) {
					Text(
						text = noteObjectLite.userTimestamp.timeStampToPrettyFull(),
						style = TextStyle(
							fontWeight = FontWeight.Bold,
							fontSize = 12.sp,
							color = ColorProvider(day = Color(0xFF02060A), night = Color(0xFFCEDBE6))
						)
					)

					val title = noteObjectLite.title
					if (!title.isNullOrBlank()) {
						Spacer(modifier = GlanceModifier.width(4.dp))
						Text(
							text = "·",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 12.sp,
								color = ColorProvider(day = Color(0xFF02060A), night = Color(0xFFCEDBE6))
							)
						)
						Spacer(modifier = GlanceModifier.width(4.dp))
						Text(
							text = title ?: "",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 12.sp,
								color = ColorProvider(day = Color(0xFF02060A), night = Color(0xFFCEDBE6))
							)
						)
					}
				}
				Spacer(modifier = GlanceModifier.height(2.dp))
				Text(
					text = noteObjectLite.contentThumbnail ?: "",
					style = TextStyle(
						fontWeight = FontWeight.Normal,
						fontSize = 12.sp,
						color = ColorProvider(day = Color(0xFF02060A), night = Color(0xFFCEDBE6))
					)
				)
			}
		}
	}
}

class AddWaterClickAction : ActionCallback {
	override suspend fun onAction(context : Context, glanceId : GlanceId, parameters : ActionParameters) {
		val noteId = parameters.get(ActionParameters.Key<ByteArray>("noteId"))
		Intent(context, NoteActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.IsNew.name, false)
			if (noteId != null) {
				putExtra(Extra.Companion.Extra.NoteId.name, noteId)
			}
			putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

			context.startActivity(this)
		}
	}
}
