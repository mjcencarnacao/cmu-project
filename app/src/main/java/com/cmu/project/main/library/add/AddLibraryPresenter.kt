package com.cmu.project.main.library.add

import android.util.Log
import com.cmu.project.core.Utils.convertBitmapToByteArray
import com.cmu.project.core.models.Library
import com.cmu.project.main.library.add.AddLibraryPresenter.HOLDER.TAG
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddLibraryPresenter(private val view: AddLibraryContract.View) : AddLibraryContract.Presenter {

    object HOLDER {
        const val TAG = "AddLibraryPresenter"
    }

    private val storage = FirebaseStorage.getInstance().reference
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override fun addLibraryToRemoteCollection(library: Library) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val ref = libraryCollection.add(library).await()
                ref.update("id", ref.id).await()
                ref.update("books", mutableListOf<DocumentReference>()).await()
                storage.child("libraries/" + ref.id).putBytes(convertBitmapToByteArray(view.getLibraryImage()!!)).await()
                view.dismissDialog(library = library)
                Log.i(TAG, "Added library to remote collection.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while adding library to remote collection. Error: ${e.message}")
            view.dismiss()
        }
    }

}