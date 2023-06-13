package com.cmu.project.main.library.details

import android.net.Uri
import android.os.Bundle
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

interface LibraryDetailsContract {

    interface View : BaseView<LibraryDetailsPresenter> {
        fun setLibraryImage()
        fun getLibraryName() : String
        fun setLibraryName(name: String)
        fun goToBookDetails(bundle: Bundle)
        fun changeFavouriteButton(isFav: Boolean)
    }

    interface Presenter : BasePresenter {
        suspend fun sendRating(float: Float)
        suspend fun getLibraryImage(library: Library) : Uri?
        fun removeBookFromLibrary(id: String)
        suspend fun retrieveBooksFromLibrary() : MutableList<Book>
        suspend fun addLibraryToFavourites(user: FirebaseUser, library: Library)
    }

}