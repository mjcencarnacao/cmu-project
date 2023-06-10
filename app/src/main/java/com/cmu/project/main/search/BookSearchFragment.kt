package com.cmu.project.main.search

import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmu.project.R
import com.cmu.project.core.models.Book
import com.cmu.project.databinding.FragmentBookSearchBinding
import com.cmu.project.main.search.book.BookSearchAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookSearchFragment : Fragment(R.layout.fragment_book_search) {

    private lateinit var binding: FragmentBookSearchBinding
    var adapter: BookSearchAdapter = BookSearchAdapter(BookSearchPresenter())
    private val bookCollection = Firebase.firestore.collection("books")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookSearchBinding.bind(view)
        binding.rvBook.layoutManager = LinearLayoutManager(activity)
        binding.rvBook.adapter = adapter
        lifecycleScope.launch(Dispatchers.IO) {retrieveBooksFromCloud() }
        setSearchViewListener()
    }

    private suspend fun retrieveBooksFromCloud() {
        val collection = mutableListOf<Book>()
        val books = bookCollection.get().await()
        books.forEach { document -> collection.add(document.toObject(Book::class.java).apply { id = document.id }) }
        withContext(Dispatchers.Main){ adapter.updateList(collection) }
    }

    private fun setSearchViewListener() {
        binding.svBookSearch.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { adapter.setFilteredList(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.setFilteredList(it) }
                return false
            }
        })
    }

}