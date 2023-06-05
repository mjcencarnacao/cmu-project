package com.cmu.project.main.maps

import com.cmu.project.core.models.Library
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MapsPresenter(private val view: MapsContract.View) : MapsContract.Presenter {

    private val libraryCollection = Firebase.firestore.collection("libraries")

    override suspend fun retrieveLibrariesFromCloud(): List<Library> {
        val libraries = mutableListOf<Library>()
        val snapshot = libraryCollection.get().await()
        snapshot.forEach { document ->
            val lib = document.toObject(Library::class.java)
            lib.id = document.id
            libraries.add(lib)
        }
        return libraries
    }

}