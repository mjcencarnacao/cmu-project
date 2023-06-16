package com.cmu.project.main.library.details

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.cmu.project.core.NetworkManager
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.main.library.details.libraries.LibraryDetailsViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LibraryDetailsPresenter(private val view: LibraryDetailsContract.View) :
    LibraryDetailsContract.Presenter {

    private var bookList = mutableListOf<Book>()
    private var storage = FirebaseStorage.getInstance().reference

    private val bookCollection = Firebase.firestore.collection("books")
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override suspend fun sendRating(float: Float) {
        val snapshot =
            libraryCollection.get().await().find { it.getString("name") == view.getLibraryName() }
        FirebaseAuth.getInstance().currentUser?.let {
            val reviews =
                snapshot?.reference!!.get().await().get("reviews") as HashMap<String, Float>
            reviews[it.uid] = float
            snapshot.reference!!.update("reviews", reviews)
        }
    }

    override suspend fun getRating(): Float {
        var totalRating = 0
        var reviewCount = 0
        val snapshot =
            libraryCollection.get().await().find { it.getString("name") == view.getLibraryName() }
        val reviewsRef = snapshot?.reference!!.get().await().get("reviews")
        if (reviewsRef != null) {
            val reviews = snapshot.reference!!.get().await().get("reviews") as HashMap<String, Int>
            reviewCount = reviews.values.size
            reviews.forEach { (t, u) -> totalRating += u }
            if (reviewCount != 0)
                return (totalRating / reviewCount).toFloat()
        }
        return 0f
    }

    override suspend fun getLibraryImage(library: Library): Uri? {
        return storage.child("libraries/" + library.id.trim()).downloadUrl.await()
    }

    override fun removeBookFromLibrary(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = libraryCollection.get().await()
                .find { it.getString("name") == view.getLibraryName() }
            val documentReference =
                bookCollection.get().await().find { it.getLong("code") == id.toLong() }?.reference
            snapshot?.reference?.update("books", FieldValue.arrayRemove(documentReference))?.await()
        }
    }

    override suspend fun retrieveBooksFromLibrary(): MutableList<Book> {
        val collection = mutableListOf<Book>()
        libraryCollection.get().await().forEach { library ->
            if (library.getString("name") == view.getLibraryName()) {
                val booksRefs = library?.get("books") as List<*>?
                booksRefs?.forEach { document ->
                    if (document is DocumentReference) {
                        val temp = bookCollection.document(document.id).get().await()
                            .toObject(Book::class.java)
                        if (temp != null) {
                            temp.id = document.id
                            collection.add(temp)
                        }
                    }
                }
                return collection
            }
        }
        return collection
    }

    fun getBookCount() = bookList.size

    fun onBindBookSearchViewHolder(holder: LibraryDetailsViewHolder, position: Int) {
        val item: Book = bookList[position]
        holder.setBookTitle(item.title)
        holder.setBookAuthor(item.author)
        holder.setBookRating(item.rating)
        CoroutineScope(Dispatchers.Main).launch {
            holder.setImageListener(getCoverImageFromRemote(item))
            if (NetworkManager.checkWifiStatus(view.provideContext()))
                holder.setBookCover(getCoverImageFromRemote(item))
        }
    }

    fun goToBookDetails(bundle: Bundle) {
        view.goToBookDetails(bundle)
    }

    private suspend fun getCoverImageFromRemote(book: Book): Uri? {
        return storage.child("books/" + book.id).downloadUrl.await()
    }

    fun getBookAtPosition(position: Int): Book {
        return bookList[position]
    }

    fun updateList(list: MutableList<Book>) {
        bookList = list
    }

    override suspend fun addLibraryToFavourites(user: FirebaseUser, library: Library) {

        val userRef = Firebase.firestore.collection("users")
            .whereEqualTo("email", user.email)
            .get()
            .await().documents[0].reference

        val libRef = Firebase.firestore.collection("libraries").document(library.id)

        var toAdd = true
        val userFavourites = userRef.get().await().get("favourites") as List<*>

        for (fav in userFavourites) {
            if (fav is DocumentReference) {
                if (fav == libRef) {
                    Log.i("COMPARE_REFS", "$fav vs $libRef")
                    toAdd = false
                    break
                }
            }
        }

        if (toAdd)
            userRef.update("favourites", FieldValue.arrayUnion(libRef))
        else
            userRef.update("favourites", FieldValue.arrayRemove(libRef))

        view.changeFavouriteButton(toAdd)
    }


}