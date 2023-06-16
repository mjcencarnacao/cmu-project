package com.cmu.project.core.workmanager.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cmu.project.R
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_CHANNEL_ID
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_CHANNEL_NAME
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.NOTIFICATION_ID
import com.cmu.project.core.workmanager.notifications.NotificationWorker.HOLDER.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
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
        val ref = Firebase.firestore.collection("users").get().await().filter { it.getString("id") == FirebaseAuth.getInstance().currentUser?.uid }
        Firebase.firestore.collection("libraries").get().await().forEach { book ->
            if ((ref.first().reference.get().await().get("notifications") as List<DocumentReference>).any { it in (book.reference.get().await().get("books") as List<DocumentReference>).toSet() }) {
                withContext(Dispatchers.Main){
                    deployNotification()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun deployNotification() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.drawable.notification_icons)
            .setContentTitle("Book Available!")
            .setContentText("A book you requested is now available.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).setChannelId(NOTIFICATION_CHANNEL_ID).build()
        notificationManager.notify(0, notification)
    }

}