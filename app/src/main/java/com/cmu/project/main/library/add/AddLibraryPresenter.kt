package com.cmu.project.main.library.add

import com.cmu.project.core.Utils.convertBitmapToByteArray
import com.cmu.project.core.models.Library
import com.google.firebase.firestore.DocumentReference
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

    override fun addLibraryToRemoteCollection(library: Library) {
        CoroutineScope(Dispatchers.IO).launch {
            val ref = libraryCollection.add(library).await()
            ref.update("id", ref.id).await()
            ref.update("books", mutableListOf<DocumentReference>()).await()
            storage.child("libraries/" + ref.id)
                .putBytes(convertBitmapToByteArray(view.getLibraryImage()!!)).await()
            view.dismissDialog(library = library)
        }
    }

}