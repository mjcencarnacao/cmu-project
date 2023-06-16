package com.cmu.project.main.library.details.libraries

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmu.project.R
import com.cmu.project.main.library.details.LibraryDetailsPresenter
import com.cmu.project.core.models.Book
import com.google.gson.Gson

@SuppressLint("NotifyDataSetChanged")
class LibraryDetailsAdapter(private val presenter: LibraryDetailsPresenter) :
    RecyclerView.Adapter<LibraryDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryDetailsViewHolder {
        return LibraryDetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return presenter.getBookCount()
    }

    override fun onBindViewHolder(holder: LibraryDetailsViewHolder, position: Int) {
        presenter.onBindBookSearchViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            val selectedBook = Gson().toJson(presenter.getBookAtPosition(position))
            val bundle = Bundle().apply {  putString("book", selectedBook) }
            presenter.goToBookDetails(bundle)
        }
    }

    fun updateList(list: MutableList<Book>) {
        presenter.updateList(list)
        notifyDataSetChanged()
    }

}