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

class BookSearchPresenter {

    private var storage = FirebaseStorage.getInstance().reference
    private var bookList = mutableListOf<Book>()

    fun getBookCount() = bookList.size

    fun onBindBookSearchViewHolder(holder: BookSearchViewHolder, position: Int) {
        val item: Book = bookList[position]
        holder.setBookTitle(item.title)
        holder.setBookAuthor(item.author)
        holder.setBookRating(item.rating)
        CoroutineScope(Dispatchers.Main).launch {  holder.setBookCover(getCoverImageFromRemote(item)) }
    }

    private suspend fun getCoverImageFromRemote(book: Book): Uri? {
        Log.e("Nigaa", book.id)
        val url = storage.child("books/" + book.id + ".jpg").downloadUrl.await()
        Log.e("Nigaa", url.toString())
        return url
    }

    fun getFilteredBookList(query: String) {
        val filtered = mutableListOf<Book>()
        bookList.forEach { book -> if (book.title.contains(query)) filtered.add(book) }
        this.bookList = filtered
    }

    fun updateList(list: MutableList<Book>) {
        this.bookList = list
    }

}