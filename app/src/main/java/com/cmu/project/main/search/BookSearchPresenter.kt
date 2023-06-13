package com.cmu.project.main.search

import android.net.Uri
import android.util.Log
import com.cmu.project.core.models.Book
import com.cmu.project.main.search.book.BookSearchViewHolder
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class BookSearchPresenter {

    private var storage = FirebaseStorage.getInstance().reference
    private var bookList = mutableListOf<Book>()
    private var fullBookList = mutableListOf<Book>()

    fun getBookCount() = bookList.size

    fun onBindBookSearchViewHolder(holder: BookSearchViewHolder, position: Int) {
        val item: Book = bookList[position]
        holder.setBookTitle(item.title)
        holder.setBookAuthor(item.author)
        holder.setBookRating(item.rating)
        CoroutineScope(Dispatchers.Main).launch {  holder.setBookCover(getCoverImageFromRemote(item)) }
    }

    private suspend fun getCoverImageFromRemote(book: Book): Uri? {
        try {
            return storage.child("books/" + book.id).downloadUrl.await()
        }
        catch (e: Exception) {
            return null
        }
    }

    fun getFilteredBookList(query: String) {
        val filtered = mutableListOf<Book>()
        fullBookList.forEach { book ->
            if (book.title.lowercase().contains(query.lowercase())) filtered.add(book)
        }
        this.bookList = filtered
    }

    fun getAlreadyFetchedBooks(): MutableList<Book> {
        return bookList
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