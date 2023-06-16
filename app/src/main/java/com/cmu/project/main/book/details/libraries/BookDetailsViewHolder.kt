package com.cmu.project.main.book.details.libraries

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmu.project.databinding.ListItemBinding
import com.cmu.project.main.book.details.BookDetailsContract

class BookDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BookDetailsContract.LibraryView {

    private var binding: ListItemBinding = ListItemBinding.bind(itemView)

    override fun setLibraryName(name: String) {
        binding.bookNotification.visibility = View.GONE
        binding.itemBookTitle.text = name
    }

    @SuppressLint("SetTextI18n")
    override fun setLibraryLocation(location: String) {
        binding.itemBookAuthor.text = location
    }

    override fun setLibraryRating(rating: Float) {
        binding.itemBookRatingbar.rating = rating
    }

    override fun setLibraryImage(url: Uri?) {
        Glide.with(itemView.context).load(url).into(binding.itemBookImg)
    }

}