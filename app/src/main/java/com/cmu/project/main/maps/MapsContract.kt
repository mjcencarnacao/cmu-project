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
        fun setupLibraryMarkers(googleMap: GoogleMap, refresh: Boolean = false)
        fun setupMarker(googleMap: GoogleMap, library: Library, isFavourite: Boolean)
    }

    interface Presenter : BasePresenter {
        fun cacheAddedLibrary(library: Library?)
        suspend fun isFavouriteLibrary(library: Library): Boolean
        suspend fun retrieveLibrariesFromCloud(refresh: Boolean = false): List<Library>
    }

}