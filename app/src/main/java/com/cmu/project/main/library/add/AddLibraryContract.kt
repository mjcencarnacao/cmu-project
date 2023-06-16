package com.cmu.project.main.library.add

import android.content.Context
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
        fun dismiss()
        fun getLibraryImage() : Bitmap?
        fun dismissDialog(library: Library)
    }

    interface Presenter : BasePresenter {
        fun addLibraryToRemoteCollection(library: Library)
    }

}