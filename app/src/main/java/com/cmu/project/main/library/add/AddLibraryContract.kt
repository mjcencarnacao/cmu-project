package com.cmu.project.main.library.add

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint

interface AddLibraryContract {

    interface View : BaseView<AddLibraryPresenter> {
        fun getLibraryImage() : Bitmap?
    }

    interface Presenter : BasePresenter {
        fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint)
    }

}