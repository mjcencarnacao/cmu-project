package com.cmu.project.main.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmu.project.R
import com.cmu.project.core.Collection
import com.cmu.project.core.NetworkManager.cacheRemoteResults
import com.cmu.project.core.NetworkManager.checkWifiStatus
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.database.entities.toBookModel
import com.cmu.project.core.models.Book
import com.cmu.project.databinding.FragmentBookSearchBinding
import com.cmu.project.main.search.BookSearchFragment.HOLDER.TAG
import com.cmu.project.main.search.book.BookSearchAdapter
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookSearchFragment : Fragment(R.layout.fragment_book_search), BookSearchContract.View {

    object HOLDER {
        const val TAG = "BookSearchFragment"
    }

    private var lastBook = ""
    private var totalItems = 0
    private var currentItems = 0
    private var isMaxData = false
    private val itemLoadCount = 1L
    private var isScrolling = false
    private var scrolledOutItems = 0
    private val initialItemCount = 5L

    private lateinit var binding: FragmentBookSearchBinding
    private lateinit var database: CacheDatabase
    private val bookCollection = Firebase.firestore.collection("books")
    var adapter: BookSearchAdapter = BookSearchAdapter(BookSearchPresenter(this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookSearchBinding.bind(view)
        binding.rvBook.layoutManager = LinearLayoutManager(activity)
        binding.rvBook.adapter = adapter
        database = CacheDatabase.getInstance(requireContext())
        lifecycleScope.launch(Dispatchers.IO) { getBooks() }
        setSearchViewListeners()
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val manager = binding.rvBook.layoutManager as LinearLayoutManager
            currentItems = manager.childCount
            totalItems = manager.itemCount
            scrolledOutItems = manager.findLastCompletelyVisibleItemPosition()

            Log.i(
                TAG,
                "Scrolling = $isScrolling | Current Items = $currentItems | Scrolled Items = $scrolledOutItems | Total Items = $totalItems"
            )

            if (checkWifiStatus(requireContext()))
                if (isScrolling && (scrolledOutItems) == totalItems - 1) {
                    Log.i(TAG, "Getting more books...")
                    isScrolling = false
                    showProgressBar()
                    lifecycleScope.launch(Dispatchers.IO) { getBooks() }
                }
        }
    }

    private fun setSearchViewListeners() {
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

        binding.rvBook.addOnScrollListener(scrollListener)
    }

    @SuppressLint("SetTextI18n")
    private suspend fun getBooks() {
        if (checkWifiStatus(requireContext())) {
            if (isMaxData) {
                dismissProgressBar()
                return
            }

            val collection = adapter.getAlreadyFetchedBooks()
            val books: QuerySnapshot = if (lastBook.isEmpty()) {
                bookCollection
                    .orderBy(FieldPath.documentId())
                    .limit(initialItemCount)
                    .get()
                    .await()
            } else {
                bookCollection
                    .orderBy(FieldPath.documentId())
                    .startAfter(lastBook)
                    .limit(itemLoadCount)
                    .get()
                    .await()
            }

            lastBook = try {
                books.last().id
            } catch (e: Exception) {
                ""
            }

            Log.i(TAG, "Last Book = $lastBook | ${books.size()} < $itemLoadCount")
            if (books.size() < itemLoadCount)
                isMaxData = true

            books.forEach { document ->
                collection.add(
                    document.toObject(Book::class.java).apply { id = document.id })
            }
            cacheRemoteResults(requireContext(), books, Collection.BOOKS)
            withContext(Dispatchers.Main) {
                adapter.updateList(collection)
            }

            dismissProgressBar()
        } else {
            val collection = mutableListOf<Book>()
            database.bookDao().getAll().forEach {
                collection.add(it.toBookModel())
            }
            withContext(Dispatchers.Main) {
                adapter.updateList(collection)
            }
            dismissProgressBar()
        }

        val results = (binding.rvBook.layoutManager as LinearLayoutManager).itemCount
        binding.numberOfResults.text = "Showing $results results"
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                Thread.sleep(500)
            }
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun provideContext(): Context {
        return requireContext()
    }

}