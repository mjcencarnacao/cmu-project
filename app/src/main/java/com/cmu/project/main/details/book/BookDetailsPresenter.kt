package com.cmu.project.main.details.book

import android.content.Context
import android.location.Geocoder
import com.cmu.project.core.models.Library
import com.cmu.project.main.details.book.libraries.BookDetailsViewHolder
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class BookDetailsPresenter {

    private var storage = FirebaseStorage.getInstance().reference
    private var libraryList = mutableListOf<Library>()

    fun getLibraryCount() = libraryList.size

    fun onBindLibrarySearchViewHolder(holder: BookDetailsViewHolder, position: Int) {
        val item: Library = libraryList[position]
        holder.setLibraryName(item.name)
        holder.setLibraryRating(item.rating)

        CoroutineScope(Dispatchers.Main).launch {
            /* Set Library Image
            holder.setLibraryImage(getCoverImageFromRemote(item))
            */
            holder.setLibraryLocation(getLibraryReadableLocation(holder.itemView.context, item.location))
        }
    }

    // private suspend fun getLibraryImageFromRemote(library: Library) Uri? {}

    private fun getLibraryReadableLocation(context: Context, point: GeoPoint): String {
        val geocoder = Geocoder(context)
        val address = geocoder.getFromLocation(point.latitude, point.longitude, 1)
        val city = address?.firstOrNull()?.locality
        val country = address?.firstOrNull()?.countryName
        val streetName = address?.firstOrNull()?.thoroughfare
        return "$city / $country\n$streetName"
    }

    fun getLibraryAtPosition(position: Int): Library {
        return libraryList[position]
    }

    fun updateList(list: MutableList<Library>) {
        this.libraryList = list
    }

}