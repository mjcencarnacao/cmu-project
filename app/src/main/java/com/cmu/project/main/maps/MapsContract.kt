package com.cmu.project.main.maps

import android.content.Context
import com.cmu.project.core.models.Library
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.android.gms.maps.GoogleMap

interface MapsContract {

    interface View : BaseView<MapsPresenter> {
        fun startStartupActivity()
        fun provideContext(): Context
        fun setupListeners(googleMap: GoogleMap)
        fun setupLibraryMarkers(googleMap: GoogleMap)
    }

    interface Presenter : BasePresenter {
        suspend fun retrieveLibrariesFromCloud(): List<Library>
    }

}