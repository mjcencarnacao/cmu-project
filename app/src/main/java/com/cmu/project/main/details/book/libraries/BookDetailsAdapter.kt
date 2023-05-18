package com.cmu.project.main.details.book.libraries

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmu.project.R
import com.cmu.project.core.models.Library
import com.cmu.project.main.details.book.BookDetailsPresenter
import com.google.gson.Gson

@SuppressLint("NotifyDataSetChanged")
class BookDetailsAdapter(private val presenter: BookDetailsPresenter) : RecyclerView.Adapter<BookDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookDetailsViewHolder {
        return BookDetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_library, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return presenter.getLibraryCount()
    }

    override fun onBindViewHolder(holder: BookDetailsViewHolder, position: Int) {
        presenter.onBindLibrarySearchViewHolder(holder, position)

        // Probably to request directions to the library
        holder.itemView.setOnClickListener {
            val selectedLibrary = Gson().toJson(presenter.getLibraryAtPosition(position))
            Log.i("Adapter", selectedLibrary)
        }
    }

    fun updateList(list: MutableList<Library>) {
        presenter.updateList(list)
        notifyDataSetChanged()
    }

}
