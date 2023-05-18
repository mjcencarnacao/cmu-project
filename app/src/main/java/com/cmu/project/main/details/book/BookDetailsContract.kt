package com.cmu.project.main.details.book

import android.net.Uri
import com.cmu.project.core.mvp.BasePresenter
import com.google.firebase.firestore.GeoPoint

interface BookDetailsContract {

    interface LibraryView {
        fun setLibraryName(name: String)
        fun setLibraryLocation(location: String)
        fun setLibraryRating(rating: Float)
        fun setLibraryImage(url: Uri?)
        // fun setLibraryDistanceFromUser()
    }

    interface Presenter : BasePresenter {

    }
}