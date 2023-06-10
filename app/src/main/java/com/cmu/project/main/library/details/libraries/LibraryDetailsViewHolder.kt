package com.cmu.project.main.library.details.libraries

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmu.project.databinding.ItemBookBinding
import com.cmu.project.main.search.BookSearchContract

class LibraryDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BookSearchContract.BookView {

    private var binding: ItemBookBinding = ItemBookBinding.bind(itemView)

    override fun setBookTitle(title: String) {
        binding.itemBookTitle.text = title
    }

    override fun setBookAuthor(author: String) {
        binding.itemBookAuthor.text = author
    }

    override fun setBookRating(rating: Float) {
        binding.itemBookRatingbar.rating = rating
    }

    override fun setBookCover(url: Uri?) {
        Glide.with(itemView.context).load(url).into(binding.itemBookImg)
    }

}