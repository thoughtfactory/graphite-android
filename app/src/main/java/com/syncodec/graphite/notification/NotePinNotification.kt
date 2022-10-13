package com.syncodec.graphite.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId
import kotlin.random.Random


class NotePinNotificationService : Service() {

	override fun onStartCommand(intent : Intent?, flags : Int, startId : Int) : Int {

		val hasNotificationId = intent?.hasExtra("notificationId")

		if (hasNotificationId == true) {
			val notificationId = intent.getIntExtra("notificationId", 0)

			NotificationManagerCompat.from(this).cancel(notificationId)

			val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.cancel(notificationId)
		}

		super.stopSelf()

		return super.onStartCommand(intent, flags, startId)
	}

	override fun onBind(intent : Intent?) : IBinder? {
		return null
	}
}


class NotePinNotification {
	fun showSimpleNotification(
		context : Context,
		noteId:ObjectId,
		chapterId : ObjectId,
		title : String,
		content : String,
		notificationId : Int = Random.nextInt(),
		priority : Int = NotificationCompat.PRIORITY_DEFAULT,
		onRequestPermission : () -> Unit
	) {
		createNotificationChannel(context)

		val openActivityActionIntent = PendingIntent.getActivity(
			context,
			Random.nextInt(),
			Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Constant.IS_NEW.name, false)
				putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId.toString())
				putExtra(Extra.Companion.Constant.NOTE_ID.name, noteId.toString())
				putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

				flags = Intent.FLAG_ACTIVITY_NEW_TASK
			},
			PendingIntent.FLAG_IMMUTABLE
		)

		val unpinActionIntent = PendingIntent.getService(
			context,
			Random.nextInt(),
			Intent(context, NotePinNotificationService::class.java).apply {
				flags = Intent.FLAG_ACTIVITY_NEW_TASK
				putExtra("notificationId", notificationId)
			},
			PendingIntent.FLAG_IMMUTABLE
		)


		val builder = Notification.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_note)
			.setContentTitle(title)
			.setContentText(content)
			.setActions(
				Notification.Action.Builder(null, "Open", openActivityActionIntent).build(),
				Notification.Action.Builder(null, "Unpin", unpinActionIntent).build(),
			)
			.setContentIntent(openActivityActionIntent)
			.setOngoing(true)

		with(NotificationManagerCompat.from(context)) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
				onRequestPermission()
				return
			}
			notify(notificationId, builder.build())
		}
	}

	private fun createNotificationChannel(context : Context) {
		val name = "Pinned note"
		val descriptionText = "Channel for note pin notification"
		val importance = NotificationManager.IMPORTANCE_DEFAULT
		val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
			description = descriptionText
		}

		val notificationManager : NotificationManager = with(context) { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
		notificationManager.createNotificationChannel(channel)
	}

	companion object {
		const val CHANNEL_ID = "note_pin_channel"
		const val NOTIFICATION_ID = 1
		const val NOTIFICATION_TITLE = "Pinned note"

	}
}
