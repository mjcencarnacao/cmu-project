package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.cmu.project.core.Utils.libraryEntityToLibraryList
import com.cmu.project.core.Utils.libraryListFromSnapshot
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Library
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MapsPresenter(view: MapsContract.View) : MapsContract.Presenter {

    private val database = CacheDatabase.getInstance(view.provideContext())
    private val libraryCollection = Firebase.firestore.collection("libraries")
    private val locationProvider = LocationServices.getFusedLocationProviderClient(view.provideContext())
    private val maxDistance = 0.1f // Distance in degrees, approx 10km

    @SuppressLint("MissingPermission")
    override suspend fun retrieveLibrariesFromCloud(refresh: Boolean): List<Library> {
        if (database.libraryDao().isEmpty() || refresh) {
            val userLoc = locationProvider.lastLocation.await()
            return libraryListFromSnapshot(
                getLibrariesWithinDistance(userLoc, maxDistance),
                database
            )
        }
        return libraryEntityToLibraryList(database.libraryDao().getAll())
    }

    private suspend fun getLibrariesWithinDistance(userLoc: Location?, maxDistance: Float): QuerySnapshot {
        if (userLoc == null) {
            return libraryCollection.get().await()
        }

        val southPoint = GeoPoint(userLoc.latitude - maxDistance, userLoc.longitude - maxDistance)
        val northPoint = GeoPoint(userLoc.latitude + maxDistance, userLoc.longitude + maxDistance)

        return libraryCollection
            .whereGreaterThanOrEqualTo("location", southPoint)
            .whereLessThanOrEqualTo("location", northPoint)
            .get()
            .await()
    }

}