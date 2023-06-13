package com.cmu.project.core.workmanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cmu.project.R
import com.cmu.project.core.Collection
import com.cmu.project.core.NetworkManager
import com.cmu.project.core.NetworkManager.getRemoteCollection
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_CHANNEL_ID
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_CHANNEL_NAME
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_ID
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.TAG
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    object HOLDER {
        const val TAG = "NotificationWorker"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "Notification Channel ID"
        const val NOTIFICATION_CHANNEL_NAME = "Notification Channel Name"
    }

    private val database = CacheDatabase.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting NotificationWorker. Checking for notifications.")
        return try {
            checkForNotifications()
            Result.success()
        } catch (e: Exception) {
            Log.i(TAG, "An error occurred during the NotificationWorker. Message: ${e.message}")
            Result.failure()
        }
    }

    private fun checkForNotifications() = CoroutineScope(Dispatchers.IO).launch {
        val notifications = Gson().fromJson(database.userDao().getCurrentUser().notifications, Array<DocumentReference>::class.java).asList()
        getRemoteCollection(applicationContext, Collection.LIBRARIES)?.documents?.forEach { library ->
            if(library.get("books") != null) {
                val references = library.get("books") as List<*>
                references.forEach { book ->
                    val reference = book as DocumentReference
                    if (notifications.contains(reference))
                        deployNotification()
                }
            }
        }
    }

     private fun deployNotification() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Book Available!")
            .setContentText("A book you requested is now available.")
            .setPriority(NotificationCompat.PRIORITY_MAX).build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}