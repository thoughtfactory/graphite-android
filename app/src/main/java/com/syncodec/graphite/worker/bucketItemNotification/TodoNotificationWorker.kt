package com.syncodec.graphite.worker.bucketItemNotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.syncodec.graphite.R
import kotlin.random.Random

class TodoNotificationWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
	override fun doWork(): Result {

		// Do the work here--in this case, upload the images.

		// Indicate whether the work finished successfully with the Result

		val notificationManager =
			applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val name = "Notification Channel Name"
		val descriptionText = "Notification Channel Description"
		val importance = NotificationManager.IMPORTANCE_DEFAULT
		val channel = NotificationChannel("channel_id", name, importance).apply {
			description = descriptionText
		}
		notificationManager.createNotificationChannel(channel)

		val notification = NotificationCompat.Builder(applicationContext, "channel_id")
			.setContentTitle(inputData.getString("title"))
			.setContentText("Notification Content")
			.setSmallIcon(R.drawable.ic_graphene)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.build()

		notificationManager.notify(Random.nextInt(), notification)


		return Result.success()
	}
}
