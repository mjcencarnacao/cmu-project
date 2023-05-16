package com.cmu.project.main.maps

import androidx.lifecycle.lifecycleScope
import com.cmu.project.core.models.Library
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MapsPresenter(private val view: MapsContract.View) : MapsContract.Presenter {

    private val libraryCollection = Firebase.firestore.collection("libraries")

    override suspend fun retrieveLibrariesFromCloud(): List<Library> {
        val libraries = mutableListOf<Library>()
        val snapshot = libraryCollection.get().await()
        snapshot.forEach { document -> libraries.add(document.toObject(Library::class.java)) }
        return libraries
    }

}