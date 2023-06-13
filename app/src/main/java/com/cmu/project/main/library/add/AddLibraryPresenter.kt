package com.cmu.project.main.library.add

import com.cmu.project.core.Utils.convertBitmapToByteArray
import com.cmu.project.core.models.Library
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddLibraryPresenter(private val view: AddLibraryContract.View) : AddLibraryContract.Presenter {

    private val storage = FirebaseStorage.getInstance().reference
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint) {
        CoroutineScope(Dispatchers.IO).launch {
            val ref = libraryCollection.add(Library(name = name, location = geoPoint)).await()
            ref.update("id", ref.id).await()
            storage.child("libraries/" + ref.id).putBytes(convertBitmapToByteArray(view.getLibraryImage()!!)).await()
        }
    }

}