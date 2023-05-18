package com.cmu.project.core.dialog

import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class CustomDialogPresenter(private val view: CustomDialogContract.View) :
    CustomDialogContract.Presenter {

    private val bookCollection = Firebase.firestore.collection("books")
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint) {
        libraryCollection.add(Library(name, geoPoint))
    }

    override suspend fun retrieveBooksFromLibrary(): MutableList<Book> {
        val collection = mutableListOf<Book>()
        libraryCollection.get().await().forEach { library ->
            if (library.getString("name") == view.getLibraryName()) {
                val booksRefs = library?.get("books") as List<*>?
                booksRefs?.forEach { document ->
                    if (document is DocumentReference)
                        bookCollection.document(document.id).get().await().toObject(Book::class.java)?.let { collection.add(it) }
                }
                return collection
            }
        }
        return collection
    }

}