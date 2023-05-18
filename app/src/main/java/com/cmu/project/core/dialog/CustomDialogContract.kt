package com.cmu.project.core.dialog

import com.cmu.project.core.models.Book
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.firestore.GeoPoint

interface CustomDialogContract {

    interface View : BaseView<CustomDialogPresenter> {
        fun addLibrary()
        fun setupAddLibraryDialog()
        fun getLibraryName() : String
        fun setLibraryName(name: String)
        fun setLibraryDescription(description: String)
    }

    interface Presenter : BasePresenter {
        suspend fun retrieveBooksFromLibrary() : MutableList<Book>
        fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint)
    }

}