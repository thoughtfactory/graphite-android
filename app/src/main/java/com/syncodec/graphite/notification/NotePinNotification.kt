package com.syncodec.graphite.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID


class UnpinNotificationBroadcastReceiver() : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent?) {
		val hasNotificationId = intent?.hasExtra("notificationId")
		if (hasNotificationId == true) {
			val notificationId = intent.getIntExtra("notificationId", 0)
			NotificationManagerCompat.from(context).cancel(notificationId)
			val notificationManager = context.getSystemService(NotificationManager::class.java)
			notificationManager.cancel(notificationId)
		}
	}
}


class NotePinNotification {
	companion object {
		private const val CHANNEL_ID = "note_pin_channel"

		fun pinIt(
			context: Context,
			noteId: RealmUUID,
			title: String? = null,
			content: String,
			priority: Int = NotificationCompat.PRIORITY_DEFAULT,
		) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(context, context.getText(R.string.notification_permission_unavailable), Toast.LENGTH_SHORT).show()
				return
			}

			createNotificationChannel(context)

			val noteActivity = Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, noteId.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

				flags = Intent.FLAG_ACTIVITY_NEW_TASK
			}
			val openActivityPendingIntent = PendingIntent.getActivity(context, noteId.hashCode(), noteActivity, PendingIntent.FLAG_IMMUTABLE)

			val cancelIntent = Intent(context, UnpinNotificationBroadcastReceiver::class.java).apply {
				putExtra("notificationId", noteId.hashCode())
			}
			val cancelPendingIntent = PendingIntent.getBroadcast(context, noteId.hashCode(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

			val builder = Notification.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_note)
				.setContentTitle(title ?: "Untitled")
				.setContentText(content)
				.setActions(
					Notification.Action.Builder(null, "Open", openActivityPendingIntent).build(),
					Notification.Action.Builder(null, "Unpin", cancelPendingIntent).build(),
				)
				.addExtras(
					Bundle().apply {
						putByteArray(Extra.Companion.Extra.NoteId.name, noteId.bytes)
					}
				)
				.setContentIntent(openActivityPendingIntent)
				.setOngoing(true)


			context.getSystemService(NotificationManager::class.java).notify(noteId.hashCode(), builder.build())
		}

		private fun createNotificationChannel(context: Context) {
			val name = "Pinned note"
			val descriptionText = "Channel for note pin notification"
			val importance = NotificationManager.IMPORTANCE_DEFAULT
			val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
				description = descriptionText
			}

			val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
			notificationManager.createNotificationChannel(channel)
		}

		fun isNotificationPinned(context: Context, noteId: RealmUUID?): Boolean {
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			return notificationManager.activeNotifications.any {
				it.notification.extras.getByteArray(Extra.Companion.Extra.NoteId.name).contentEquals(noteId?.bytes)
			}
		}
	}
}
