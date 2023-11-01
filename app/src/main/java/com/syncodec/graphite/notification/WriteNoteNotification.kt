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
import android.graphics.Color
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.utils.dataStore.DataStoreInstance


class WriteNoteBroadcastReceiver : BroadcastReceiver() {

	override fun onReceive(context : Context, intent : Intent) {
		val remoteInput = RemoteInput.getResultsFromIntent(intent)

		if (remoteInput != null) {
			val lockableRepo = LockableRepo(context = context, dataStoreInstance = DataStoreInstance(context = context))
			lockableRepo.decryptRepository()?.let { repo ->
				val content = remoteInput.getCharSequence("KEY_TEXT_REPLY").toString()
				NoteObject().apply {
					this.content = "<p>$content</p>"
					repo.putNoteInDefaultSuspended(noteObject = this)
				}
				WriteNoteNotification.pinIt(context = context)
			}
		}
	}
}


class WriteNoteNotification {
	companion object {
		private const val CHANNEL_ID = "write_note_channel"
		private const val NOTIFICATION_ID = 2

		fun pinIt(context: Context) {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(context, context.getText(R.string.notification_permission_unavailable), Toast.LENGTH_SHORT).show()
				return
			}

			createNotificationChannel(context)

			val resultIntent = Intent(context, WriteNoteBroadcastReceiver::class.java)
			val resultPendingIntent = PendingIntent.getBroadcast(context, 0, resultIntent, PendingIntent.FLAG_MUTABLE)

			val resKey = "KEY_TEXT_REPLY"
			val remoteInput = RemoteInput
				.Builder(resKey)
				.build()

			val replyAction = NotificationCompat
				.Action
				.Builder(R.drawable.ic_add, "Add", resultPendingIntent)
				.addRemoteInput(remoteInput)
				.build()

			val notification = NotificationCompat
				.Builder(context, CHANNEL_ID)
				.setDefaults(Notification.DEFAULT_ALL)
				.setSmallIcon(R.drawable.ic_note)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentTitle("Got new ideas? Add Here!!")
				.setColor(Color.BLACK)
				.setAutoCancel(false)
				.setColorized(true)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.addAction(replyAction)
				.build()

			context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
		}

		private fun createNotificationChannel(context : Context) {
			val name = "Add note"
			val descriptionText = "Channel for add note notification"
			val importance = NotificationManager.IMPORTANCE_DEFAULT
			val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
				description = descriptionText
			}

			val notificationManager : NotificationManager = context.getSystemService(NotificationManager::class.java)
			notificationManager.createNotificationChannel(channel)
		}

		fun cancelNotification(context : Context) {
			context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
			val dataStoreInstance = DataStoreInstance(context)
			dataStoreInstance.putNoteFromNotification(false)
		}
	}
}
