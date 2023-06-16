package com.cmu.project.main.search.book

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cmu.project.R
import com.cmu.project.core.models.Book
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

class BookSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    BookSearchContract.BookView {

    private var wasLoaded = false
    private var hasNotification = false
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

    override fun setBookCover(url: Uri?, cached: Boolean) {
        if (url != null && !cached)
            Glide.with(itemView.context).load(url).into(binding.itemBookImg)
        else if(url != null)
            Glide.with(itemView.context).load(url).onlyRetrieveFromCache(true).into(binding.itemBookImg)
    }

    override fun setImageListener(url: Uri?) {
        binding.itemBookImg.setOnClickListener {
            if (!wasLoaded) {
                Glide.with(itemView.context).load(url).into(binding.itemBookImg)
                wasLoaded = true
            }
        }
    }

    override fun setBookNotification(documentReference: DocumentReference) {
        CoroutineScope(Dispatchers.IO).launch {
            val ref = Firebase.firestore.collection("users").get().await().filter { it.getString("id") == FirebaseAuth.getInstance().currentUser?.uid }
            withContext(Dispatchers.Main){
                if((ref.first().reference.get().await().get("notifications") as List<DocumentReference>).contains(documentReference)) {
                    hasNotification = true
                    binding.bookNotification.setImageResource(R.drawable.ic_notification_on)
                }
                binding.bookNotification.setOnClickListener {
                    if (hasNotification) {
                        hasNotification = false
                        binding.bookNotification.setImageResource(R.drawable.ic_notification)
                        ref.first().reference.update(
                            "notifications",
                            FieldValue.arrayRemove(documentReference)
                        )
                    } else {
                        hasNotification = true
                        binding.bookNotification.setImageResource(R.drawable.ic_notification_on)
                        ref.first().reference.update(
                            "notifications",
                            FieldValue.arrayUnion(documentReference)
                        )
                    }
                }
            }

        }
    }

}