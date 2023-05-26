package com.cmu.project.core.dialog

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.cmu.project.R
import com.cmu.project.core.dialog.libraries.LibraryDetailsViewHolder
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.main.search.book.BookSearchViewHolder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.coroutineContext

class CustomDialogPresenter(private val view: CustomDialogContract.View) :
    CustomDialogContract.Presenter {

    private var storage = FirebaseStorage.getInstance().reference
    private val bookCollection = Firebase.firestore.collection("books")
    private val libraryCollection = Firebase.firestore.collection("libraries")
    private var bookList = mutableListOf<Book>()

    override fun addLibraryToRemoteCollection(name: String, geoPoint: GeoPoint) {
        libraryCollection.add(Library(name = name, location = geoPoint))
    }

    override suspend fun retrieveBooksFromLibrary(): MutableList<Book> {
        val collection = mutableListOf<Book>()
        libraryCollection.get().await().forEach { library ->
            if (library.getString("name") == view.getLibraryName()) {
                val booksRefs = library?.get("books") as List<*>?
                booksRefs?.forEach { document ->
                    if (document is DocumentReference) {
                        val temp = bookCollection.document(document.id).get().await().toObject(Book::class.java)
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
        CoroutineScope(Dispatchers.Main).launch {  holder.setBookCover(getCoverImageFromRemote(item)) }
    }

    fun goToBookDetails(bundle: Bundle) {
        view.goToBookDetails(bundle)
    }

    private suspend fun getCoverImageFromRemote(book: Book): Uri? {
        try {
            return storage.child("books/" + book.id + ".jpg").downloadUrl.await()
        } catch (e : Exception){

        }
        return null
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
        val userFavourites = userRef.get().await().get("favourites") as ArrayList<*>
        Log.i("COMPARE_REFS", "FOUND: ${userFavourites.size}")

        for (fav in userFavourites) {
            if (fav is DocumentReference) {
                if (fav == libRef) {
                    Log.i("COMPARE_REFS", "$fav vs $libRef")
                    toAdd = false
                    break
                }
            }
        }

        if (toAdd) {
            userRef.update("favourites", FieldValue.arrayUnion(libRef))
            Log.i("RESULT", "ADD")
        }
        else {
            userRef.update("favourites", FieldValue.arrayRemove(libRef))
            Log.i("RESULT", "REMOVE")
        }

        view.changeFavouriteBtn(toAdd)
    }



}