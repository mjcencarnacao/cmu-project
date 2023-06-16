package com.cmu.project.main.book.details

import android.net.Uri
import com.cmu.project.core.models.Library
import com.cmu.project.core.mvp.BasePresenter
import com.google.firebase.firestore.GeoPoint

interface BookDetailsContract {

    interface LibraryView {
        fun setLibraryName(name: String)
        fun setLibraryLocation(location: String)
        fun setLibraryRating(rating: Float)
        fun setLibraryImage(url: Uri?)
    }

    interface Presenter : BasePresenter {
        suspend fun getLibraryImageFromRemote(library: Library): Uri?
    }
}