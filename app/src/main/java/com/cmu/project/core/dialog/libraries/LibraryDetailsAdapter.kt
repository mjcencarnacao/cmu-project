package com.cmu.project.core.dialog.libraries

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cmu.project.R
import com.cmu.project.core.dialog.CustomDialogPresenter
import com.cmu.project.core.models.Book
import com.cmu.project.main.search.BookSearchPresenter
import com.cmu.project.main.search.book.BookSearchViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

@SuppressLint("NotifyDataSetChanged")
class LibraryDetailsAdapter(private val presenter: CustomDialogPresenter) :
    RecyclerView.Adapter<LibraryDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryDetailsViewHolder {
        return LibraryDetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return presenter.getBookCount()
    }

    override fun onBindViewHolder(holder: LibraryDetailsViewHolder, position: Int) {
        presenter.onBindBookSearchViewHolder(holder, position)

        // Go to book details
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