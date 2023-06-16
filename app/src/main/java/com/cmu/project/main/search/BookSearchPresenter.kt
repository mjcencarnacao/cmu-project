package com.cmu.project.main.search

import android.net.Uri
import android.util.Log
import com.cmu.project.core.NetworkManager
import com.cmu.project.core.models.Book
import com.cmu.project.main.search.book.BookSearchViewHolder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class BookSearchPresenter(private val view : BookSearchContract.View) : BookSearchContract {


    private var bookList = mutableListOf<Book>()
    private var fullBookList = mutableListOf<Book>()
    private var storage = FirebaseStorage.getInstance().reference

    fun getBookCount() = bookList.size

    fun onBindBookSearchViewHolder(holder: BookSearchViewHolder, position: Int) {
        val item: Book = bookList[position]
        holder.setBookTitle(item.title)
        holder.setBookAuthor(item.author)
        holder.setBookRating(item.rating)
        CoroutineScope(Dispatchers.IO).launch {
            val book = Firebase.firestore.collection("books").get().await().filter { item.title == it.getString("title") }.first()
            holder.setBookNotification(book.reference)
        }
        CoroutineScope(Dispatchers.Main).launch {
            holder.setImageListener(getCoverImageFromRemote(item))
            if (NetworkManager.checkWifiStatus(view.provideContext()))
                holder.setBookCover(getCoverImageFromRemote(item))
        }
    }

    private suspend fun getCoverImageFromRemote(book: Book): Uri? {
        return try {
            storage.child("books/" + book.id.trim()).downloadUrl.await()
        } catch (e: Exception) {
            null
        }
    }

    fun getAlreadyFetchedBooks(): MutableList<Book> {
        return bookList
    }

    fun getFilteredBookList(query: String) {
        val filtered = mutableListOf<Book>()
        fullBookList.forEach { book ->
            if (book.title.lowercase().contains(query.lowercase())) filtered.add(book)
        }
        this.bookList = filtered
    }

    fun updateList(list: MutableList<Book>) {
        this.bookList = list

        if (fullBookList.isEmpty())
            fullBookList = list
    }

    fun getBookAtPosition(position: Int): Book {
        return bookList[position]
    }

}