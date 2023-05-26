package com.cmu.project.core.dialog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint

interface CustomDialogContract {

    interface View : BaseView<CustomDialogPresenter> {
        fun addLibrary()
        fun setupAddLibraryDialog()
        fun getLibraryName() : String
        fun setLibraryName(name: String)
        fun setLibraryDescription(description: String)
        fun goToBookDetails(bundle: Bundle)
        fun changeFavouriteBtn(isFav: Boolean)
    }

    interface Presenter : BasePresenter {
        suspend fun retrieveBooksFromLibrary() : MutableList<Book>
        suspend fun addLibraryToFavourites(user: FirebaseUser, library: Library)
        fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint)
    }

}