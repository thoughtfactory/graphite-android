package com.syncodec.graphite.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WriteNoteNotificationService : Service() {

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

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

	override fun onReceive(context : Context, intent : Intent) {
		val remoteInput = RemoteInput.getResultsFromIntent(intent)

		if (remoteInput != null) {
			val repository2 = Repository2(context)
			repository2.isAuthenticated.value = true
			val content = remoteInput.getCharSequence("KEY_TEXT_REPLY").toString()

			putNote(repository2, content)
		}
	}

	private fun putNote(
		repository2 : Repository2,
		content: String
	) {
		CoroutineScope(Dispatchers.Default).launch {
			repository2.repositoryState.collect {
				if(it == RepositoryState.SUCCESS) {
					repository2.getDefaultChapterId().collect {
						NoteObject().apply {
							this.content = "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"$content\"}]}]}"
							this.parentId = it
							this.contentThumbnail = content.substring(0, minOf(256, content.length))

							repository2.putNote(this) { _, _ ->
								WriteNoteNotification.showSimpleNotification(context = repository2.context)
							}
						}
					}
				}
			}
		}
	}
}


class WriteNoteNotification {
	companion object {
		const val CHANNEL_ID = "write_note_channel"
		const val NOTIFICATION_ID = 2

		fun showSimpleNotification(context : Context) {
			createNotificationChannel(context)

			val resKey = "KEY_TEXT_REPLY"

			val remoteInput = RemoteInput.Builder(resKey).build()

			val resultIntent = Intent(context, NotificationReceiver::class.java)

			val resultPendingIntent = PendingIntent
				.getBroadcast(
					context,
					0,
					resultIntent,
					PendingIntent.FLAG_MUTABLE
				)

			val replyAction = NotificationCompat
				.Action
				.Builder(android.R.drawable.ic_input_add, "Add", resultPendingIntent)
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

			with(NotificationManagerCompat.from(context)) {
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//					onRequestPermission()
					return
				}

				notify(NOTIFICATION_ID, notification)
			}
		}

		private fun createNotificationChannel(context : Context) {
			val name = "Add note"
			val descriptionText = "Channel for add note notification"
			val importance = NotificationManager.IMPORTANCE_DEFAULT
			val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
				description = descriptionText
			}

			val notificationManager : NotificationManager = with(context) { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
			notificationManager.createNotificationChannel(channel)
		}

		fun cancelNotification(context : Context) {
			with(NotificationManagerCompat.from(context)) {
				this.cancel(NOTIFICATION_ID)
			}
		}
	}
}
