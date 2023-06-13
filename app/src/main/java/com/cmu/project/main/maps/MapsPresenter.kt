package com.cmu.project.main.maps

import com.cmu.project.core.Utils.libraryEntityToLibraryList
import com.cmu.project.core.Utils.libraryListFromSnapshot
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Library
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MapsPresenter(view: MapsContract.View) : MapsContract.Presenter {

    private val database = CacheDatabase.getInstance(view.provideContext())
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override suspend fun retrieveLibrariesFromCloud(): List<Library> {
        if (database.libraryDao().isEmpty())
            return libraryListFromSnapshot(libraryCollection.get().await(), database)
        return libraryEntityToLibraryList(database.libraryDao().getAll())
    }

}