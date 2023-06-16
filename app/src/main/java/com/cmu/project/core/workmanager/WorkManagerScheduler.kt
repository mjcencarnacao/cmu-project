package com.cmu.project.core.workmanager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cmu.project.core.workmanager.WorkManagerScheduler.HOLDER.NOTIFICATIONS
import com.cmu.project.core.workmanager.WorkManagerScheduler.HOLDER.SYNCHRONIZATION
import com.cmu.project.core.workmanager.notifications.NotificationWorker
import com.cmu.project.core.workmanager.synchronization.SynchronizationWorker
import java.util.concurrent.TimeUnit

class WorkManagerScheduler(private val context: Context) {

    object HOLDER {
        const val TAG = "WorkManagerScheduler"
        const val NOTIFICATIONS = "Notifications"
        const val SYNCHRONIZATION = "Synchronization"
    }

    init {
        setPeriodicNotificationWorker()
        setPeriodicSynchronizationWorker()
    }

    private fun setPeriodicSynchronizationWorker() {
        val work = PeriodicWorkRequestBuilder<SynchronizationWorker>(2, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(SYNCHRONIZATION, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    private fun setPeriodicNotificationWorker() {
        val work = PeriodicWorkRequestBuilder<NotificationWorker>(2, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NOTIFICATIONS, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

}