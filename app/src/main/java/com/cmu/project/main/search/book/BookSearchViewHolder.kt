package com.cmu.project.main.search.book

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmu.project.databinding.ListItemBinding
import com.cmu.project.main.search.BookSearchContract

class BookSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BookSearchContract.BookView {

    private var binding: ListItemBinding = ListItemBinding.bind(itemView)

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
        Log.i("URL", "URL: $url")
        Glide.with(itemView.context).load(url).into(binding.itemBookImg)
    }

}