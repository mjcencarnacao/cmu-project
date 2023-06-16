package com.cmu.project.core.workmanager.synchronization

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cmu.project.core.Collection
import com.cmu.project.core.NetworkManager.getRemoteCollection
import com.cmu.project.core.workmanager.synchronization.SynchronizationWorker.HOLDER.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SynchronizationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    object HOLDER {
        const val TAG = "SynchronizationWorker"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting SynchronizationWorker. Fetching Remote data in progress.")
        return try {
            fetchRemoteData()
            Result.success()
        } catch (e: Exception) {
            Log.i(TAG, "An error occurred during the SynchronizationWorker. Message: ${e.message}")
            Result.failure()
        }
    }

    private fun fetchRemoteData() = CoroutineScope(Dispatchers.IO).launch {
        Collection.values().forEach { collection -> getRemoteCollection(applicationContext, collection) }
    }

}