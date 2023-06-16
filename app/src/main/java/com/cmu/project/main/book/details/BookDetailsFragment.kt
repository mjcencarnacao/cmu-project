package com.cmu.project.main.book.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cmu.project.R
import com.cmu.project.core.models.Book
import com.cmu.project.databinding.FragmentBookDetailsBinding
import com.cmu.project.main.book.details.libraries.BookDetailsAdapter
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookDetailsFragment : Fragment(R.layout.fragment_book_details) {

    private lateinit var binding: FragmentBookDetailsBinding
    private lateinit var adapter: BookDetailsAdapter
    private lateinit var presenter: BookDetailsPresenter
    private var storage = FirebaseStorage.getInstance().reference

    // Safe Args
    private val args: BookDetailsFragmentArgs by navArgs()
    private lateinit var book: Book

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        book = Gson().fromJson(args.book, Book::class.java)
        adapter = BookDetailsAdapter(BookDetailsPresenter(requireActivity()))
        binding = FragmentBookDetailsBinding.bind(view)
        binding.rvLibrary.layoutManager = LinearLayoutManager(activity)
        binding.rvLibrary.adapter = adapter
        presenter = BookDetailsPresenter(requireActivity())

        CoroutineScope(Dispatchers.Main).launch {
            val libraries = presenter.retrieveApplicableLibrariesFromCloud(book)
            adapter.updateList(libraries.values.toMutableList())
        }

        setBookInformation()
    }

    private fun setBookInformation() {
        binding.bookDetail.bookNotification.visibility = View.GONE
        binding.bookDetail.itemBookAuthor.text = book.author
        binding.bookDetail.itemBookRatingbar.rating = book.rating
        binding.bookDetail.itemBookTitle.text = book.title

        lifecycleScope.launch {
            val url = storage.child("books/" + book.id.trim()).downloadUrl.await()
            Glide.with(this@BookDetailsFragment)
                .load(url)
                .into(binding.bookDetail.itemBookImg)
        }
    }

}