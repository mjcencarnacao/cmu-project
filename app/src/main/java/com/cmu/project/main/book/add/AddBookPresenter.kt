package com.cmu.project.main.book.add

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.cmu.project.core.GoogleBooksAPI
import com.cmu.project.core.ServiceAPI
import com.cmu.project.core.Utils.convertBitmapToByteArray
import com.cmu.project.core.models.Book
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URL

class AddBookPresenter(private val view: AddBookContract.View) : AddBookContract.Presenter {

    private var storage = FirebaseStorage.getInstance().reference
    private val api = ServiceAPI.getInstance().create(GoogleBooksAPI::class.java)
    private val bookCollection = Firebase.firestore.collection("books")
    private val libraryCollection = Firebase.firestore.collection("libraries")

    override fun addBookToLibrary(documentReference: DocumentReference) {
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = libraryCollection.get().await().find { it.getString("name") == view.getLibraryName() }
            snapshot?.reference?.update("books", FieldValue.arrayUnion(documentReference))?.await()
        }
    }

    override fun addBookToFirebaseWithoutCode(name: String, bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val collectionFiltered = bookCollection.get().await().filter { it.getString("title") == name }
            if (collectionFiltered.isEmpty()) {
                val ref = bookCollection.add(Book(title = name)).await()
                ref.update("id", ref.id)
                storage.child("books/" + ref.id).putBytes(convertBitmapToByteArray(view.getBookImage()!!)).await()
                addBookToLibrary(ref)
            } else
                addBookToLibrary(collectionFiltered.first().reference)
            view.dismissDialog()
        }
    }

    override fun addBookToFirebaseWithCode(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val collectionFiltered = bookCollection.get().await().filter { it.get("code") == id.toLong() }
            if (collectionFiltered.isEmpty()) {
                api.getBookDetails(id).body()?.let {
                    if(it.items != null && it.items.isNotEmpty()) {
                        val title = it.items[0].volumeInfo.title
                        val author = it.items[0].volumeInfo.authors[0]
                        val book = Book(id, title, id.toLong(), author, 0.0F)
                        val ref = bookCollection.add(book).await()
                        ref.update("id", ref.id).await()
                        addBookToLibrary(ref)
                        val imageUrl = URL(it.items[0].volumeInfo.imageLinks.thumbnail)
                        val image =
                            BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
                        storage.child("books/" + ref.id).putBytes(convertBitmapToByteArray(image))
                            .await()
                    } else
                        view.alertISBNFailure()
                }
            } else
                addBookToLibrary(collectionFiltered.first().reference)
        }
    }

}