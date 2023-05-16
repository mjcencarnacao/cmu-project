package com.cmu.project.core.dialog

import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.firestore.GeoPoint

interface CustomDialogContract {

    interface View : BaseView<CustomDialogPresenter> {
        fun addLibrary(geoPoint: GeoPoint)
        fun setupAddLibraryDialog()
        fun setLibraryName(name: String)
        fun setLibraryDescription(description: String)
    }

    interface Presenter : BasePresenter {
        fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint)
    }

}