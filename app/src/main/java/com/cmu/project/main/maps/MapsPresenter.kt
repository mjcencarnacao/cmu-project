package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.util.Log
import com.cmu.project.core.Collection
import com.cmu.project.core.NetworkManager.getRemoteCollection
import com.cmu.project.core.Utils.libraryEntityToLibraryList
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Library
import com.cmu.project.main.maps.MapsPresenter.HOLDER.TAG
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class MapsPresenter(private val view: MapsContract.View) : MapsContract.Presenter {

    object HOLDER {
        const val TAG = "MapsPresenter"
    }

    private val database = CacheDatabase.getInstance(view.provideContext())
    private val locationProvider = LocationServices.getFusedLocationProviderClient(view.provideContext())

    @SuppressLint("MissingPermission")
    override suspend fun retrieveLibrariesFromCloud(refresh: Boolean): List<Library> {
        try {
            if (database.libraryDao().isEmpty() || refresh) {
                val location = locationProvider.lastLocation.await()
                getRemoteCollection(view.provideContext(), Collection.LIBRARIES, location)
                Log.i(TAG, "Retrieved remote libraries successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while retrieving remote libraries. Error: ${e.message}")
        }
        return libraryEntityToLibraryList(database.libraryDao().getAll())
    }
}