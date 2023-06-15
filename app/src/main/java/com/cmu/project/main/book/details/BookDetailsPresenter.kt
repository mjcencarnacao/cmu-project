package com.cmu.project.main.book.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.main.book.details.libraries.BookDetailsViewHolder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.SortedMap

class BookDetailsPresenter(activity: Activity) : BookDetailsContract.Presenter {

    private var libraryList = mutableListOf<Library>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocation: Location
    private val libraryCollection = Firebase.firestore.collection("libraries")
    private var storage = FirebaseStorage.getInstance().reference

    init {
        setLastLocation(activity)
    }

    @SuppressLint("MissingPermission")
    private fun setLastLocation(activity: Activity) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val locationTask = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener {
            if (it != null) {
                myLocation = it
            }
        }
    }

    fun getLibraryCount() = libraryList.size

    fun onBindLibrarySearchViewHolder(holder: BookDetailsViewHolder, position: Int) {
        val item: Library = libraryList[position]
        holder.setLibraryName(item.name)
        holder.setLibraryRating(item.rating)

        CoroutineScope(Dispatchers.Main).launch {
            holder.setLibraryLocation(getLibraryReadableLocation(holder.itemView.context, item.location))
            holder.setLibraryImage(getLibraryImageFromRemote(item))
        }
    }

    // private suspend fun getLibraryImageFromRemote(library: Library) Uri? {}

    private fun getLibraryReadableLocation(context: Context, point: GeoPoint): String {
        val geocoder = Geocoder(context)
        val address = geocoder.getFromLocation(point.latitude, point.longitude, 1)
        val city = address?.firstOrNull()?.locality
        val country = address?.firstOrNull()?.countryName
        val streetName = address?.firstOrNull()?.thoroughfare

        val distance: String = if (findDistanceToGeoPoint(point) > 1000)
            "${String.format("%.2f", findDistanceToGeoPoint(point) / 1000)} km"
        else
            "${String.format("%.2f", findDistanceToGeoPoint(point))} m"

        return "$city / $country\n$streetName\nDistance: $distance"
    }

    fun getLibraryAtPosition(position: Int): Library {
        return libraryList[position]
    }

    fun updateList(list: MutableList<Library>) {
        this.libraryList = list
    }

    override suspend fun getLibraryImageFromRemote(library: Library): Uri? {
        return storage.child("libraries/" + library.id).downloadUrl.await()
    }

    suspend fun retrieveApplicableLibrariesFromCloud(book: Book): SortedMap<Float, Library> {
        val collection = mutableMapOf<Float, Library>()
        val libs = libraryCollection.get().await()
        // For each library
        for (document in libs) {
            val booksRefs = document.get("books") as List<*>?
            if (booksRefs != null) {
                // Check if the library owns at least this book
                for (ref in booksRefs) {
                    if (ref is DocumentReference) {
                        if (ref.id == book.id) {
                            val lib = document.toObject(Library::class.java)
                            collection[findDistanceToGeoPoint(lib.location)] = lib
                            break
                        }
                    }
                }
            }
        }
        return collection.toSortedMap()
    }

    private fun findDistanceToGeoPoint(point: GeoPoint): Float {
        val loc = Location("")
        loc.latitude = point.latitude
        loc.longitude = point.longitude
        return myLocation.distanceTo(loc)
    }

}