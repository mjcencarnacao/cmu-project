package com.cmu.project.main.search.book

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmu.project.R
import com.cmu.project.core.models.Book
import com.cmu.project.main.search.BookSearchPresenter

@SuppressLint("NotifyDataSetChanged")
class BookSearchAdapter(private val presenter: BookSearchPresenter) :
    RecyclerView.Adapter<BookSearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookSearchViewHolder {
        return BookSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return presenter.getBookCount()
    }

    override fun onBindViewHolder(holder: BookSearchViewHolder, position: Int) {
        presenter.onBindBookSearchViewHolder(holder, position)
    }

    fun updateList(list: MutableList<Book>) {
        presenter.updateList(list)
        notifyDataSetChanged()
    }

    fun setFilteredList(query: String) {
        presenter.getFilteredBookList(query)
        notifyDataSetChanged()
    }

}