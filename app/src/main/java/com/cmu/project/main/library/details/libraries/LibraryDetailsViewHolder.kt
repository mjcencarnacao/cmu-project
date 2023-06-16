package com.cmu.project.main.library.details.libraries

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmu.project.R
import com.cmu.project.databinding.ListItemBinding
import com.cmu.project.main.search.BookSearchContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LibraryDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), BookSearchContract.BookView {

    private var wasLoaded = false
    private var hasNotification = false
    private var binding: ListItemBinding = ListItemBinding.bind(itemView)

    override fun setBookTitle(title: String) {
        binding.bookNotification.visibility = View.GONE
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

    override fun setImageListener(url: Uri?) {
        binding.itemBookImg.setOnClickListener {
            if(!wasLoaded) {
                Glide.with(itemView.context).load(url).into(binding.itemBookImg)
                wasLoaded = true
            }
        }
    }

    override fun setBookNotification(documentReference: DocumentReference) {

    }

}