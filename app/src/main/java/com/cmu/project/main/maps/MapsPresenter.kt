package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.util.Log
import com.cmu.project.core.Collection
import com.cmu.project.core.NetworkManager.getRemoteCollection
import com.cmu.project.core.Utils.libraryEntityToLibraryList
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Library
import com.cmu.project.core.models.toLibraryEntity
import com.cmu.project.main.maps.MapsPresenter.HOLDER.TAG
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    override suspend fun isFavouriteLibrary(library: Library): Boolean {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let { firebaseUser ->
                val userCollection = Firebase.firestore.collection("users").get().await()
                val libraryReference = Firebase.firestore.collection("libraries").document(library.id)
                val userReference = userCollection.first { it.getString("email") == firebaseUser.email }.reference
                val userFavourites = userReference.get().await().get("favourites") as List<DocumentReference>
                if (userFavourites.any { it == libraryReference })
                    return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while if library is in favourites. Error: ${e.message}")
            return false
        }
    }

    override fun cacheAddedLibrary(library: Library?) {
        CoroutineScope(Dispatchers.IO).launch {
            library?.let {
                database.libraryDao().insert(it.toLibraryEntity())
            }
        }
    }
}