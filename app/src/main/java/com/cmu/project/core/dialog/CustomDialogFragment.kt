package com.cmu.project.core.dialog

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmu.project.R
import com.cmu.project.core.Constants.CAMERA_REQUEST_CODE
import com.cmu.project.databinding.CustomDialogBinding
import com.cmu.project.main.search.BookSearchPresenter
import com.cmu.project.main.search.book.BookSearchAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomDialogFragment : BottomSheetDialogFragment(R.layout.custom_dialog),
    CustomDialogContract.View {

    private lateinit var binding: CustomDialogBinding
    override lateinit var presenter: CustomDialogPresenter
    private val args: CustomDialogFragmentArgs by navArgs()
    var adapter: BookSearchAdapter = BookSearchAdapter(BookSearchPresenter())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CustomDialogBinding.bind(view)
        binding.rvBooks.layoutManager = LinearLayoutManager(activity)
        binding.rvBooks.adapter = adapter
        presenter = CustomDialogPresenter(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val list = presenter.retrieveBooksFromLibrary()
            withContext(Dispatchers.Main){ adapter.updateList(list) }
        }
        setupDialog()
        if (args.isAdd) setupAddLibraryDialog() else setLibraryName(args.libraryTitle)
    }

    private fun setupDialog() {
        isCancelable = true
        setOnClickListeners()
    }

    override fun setLibraryName(name: String) {
        binding.tvLibraryName.text = name
    }

    override fun setLibraryDescription(description: String) {
        binding.tvDescription.text = description
    }

    private fun setOnClickListeners() {
        binding.btnLibraryFavourite.setOnClickListener {
            startActivityForResult(
                requireActivity(),
                Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                CAMERA_REQUEST_CODE,
                null
            )
        }

        binding.btnBack.setOnClickListener { dismiss() }

        binding.btnAction.setOnClickListener { addLibrary() }
    }

    override fun addLibrary() {
        val location = Gson().fromJson(args.coordinates, LatLng::class.java)
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        presenter.addLibraryToRemoteCollection(binding.etLibraryName.text.toString(), geoPoint)
        dismiss()
    }

    override fun setupAddLibraryDialog() {
        binding.rvBooks.visibility = View.GONE
        binding.tvBooksTitle.visibility = View.GONE
        binding.tvLibraryName.visibility = View.INVISIBLE
        binding.etLibraryName.visibility = View.VISIBLE
        binding.btnAction.visibility = View.VISIBLE
        binding.btnBack.visibility = View.VISIBLE
        binding.ratingBar.visibility = View.GONE
        binding.btnAddBook.visibility = View.GONE
        binding.btnRemoveBook.visibility = View.GONE
        binding.btnLibraryFavourite.setImageResource(R.drawable.ic_image)
        binding.btnAction.text = context?.resources?.getString(R.string.add_library)
        binding.tvDescription.text = context?.resources?.getString(R.string.add_library_description)
    }

    override fun getLibraryName() : String {
        return binding.tvLibraryName.text.toString()
    }

}