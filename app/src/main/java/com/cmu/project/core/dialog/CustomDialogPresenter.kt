package com.cmu.project.core.dialog

import com.cmu.project.core.models.Library
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CustomDialogPresenter(private val view: CustomDialogContract.View) :
    CustomDialogContract.Presenter {

    private val libraryCollection = Firebase.firestore.collection("libraries")

    override fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint) {
        libraryCollection.add(Library(name, geoPoint))
    }

}