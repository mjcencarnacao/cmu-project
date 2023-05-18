package com.cmu.project.main.details.book

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cmu.project.R
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentBookDetailsBinding
import com.cmu.project.main.details.book.libraries.BookDetailsAdapter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.nio.file.attribute.AclEntry.Builder

class BookDetailsFragment : Fragment(R.layout.fragment_book_details) {

    private lateinit var binding: FragmentBookDetailsBinding
    private var adapter: BookDetailsAdapter = BookDetailsAdapter(BookDetailsPresenter())
    private var storage = FirebaseStorage.getInstance().reference
    private val libraryCollection = Firebase.firestore.collection("libraries")

    // Safe Args
    private val args: BookDetailsFragmentArgs by navArgs()
    private lateinit var book: Book

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        book = Gson().fromJson(args.book, Book::class.java)

        binding = FragmentBookDetailsBinding.bind(view)
        binding.rvLibrary.layoutManager = LinearLayoutManager(activity)
        binding.rvLibrary.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) {
            retrieveApplicableLibrariesFromCloud()
        }

        setBookInformation()
    }

    private fun setBookInformation() {
        binding.bookDetail.itemBookAuthor.text = book.author
        binding.bookDetail.itemBookRatingbar.rating = book.rating
        binding.bookDetail.itemBookTitle.text = book.title

        // ??
        lifecycleScope.launch {
            val url = storage.child("books/" + book.id + ".jpg").downloadUrl.await()
            Glide.with(this@BookDetailsFragment)
                .load(url)
                .into(binding.bookDetail.itemBookImg)
        }
    }

    private suspend fun retrieveApplicableLibrariesFromCloud() {
        val collection = mutableListOf<Library>()
        val libs = libraryCollection.get().await()
        // For each library
        for (document in libs) {
            val booksRefs = document.get("books") as List<*>?
            if (booksRefs != null) {
                // Check if the library owns at least this book
                for (ref in booksRefs) {
                    if (ref is DocumentReference) {
                        if (ref.id == book.id) {
                            collection.add(document.toObject(Library::class.java))
                            break
                        }
                    }
                }
            }
        }
        withContext(Dispatchers.Main) { adapter.updateList(collection) }
    }

}