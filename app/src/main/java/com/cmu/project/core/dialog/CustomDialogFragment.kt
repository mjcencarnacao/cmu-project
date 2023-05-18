package com.cmu.project.core.dialog

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.navigation.fragment.navArgs
import com.cmu.project.R
import com.cmu.project.core.Constants.CAMERA_REQUEST_CODE
import com.cmu.project.databinding.CustomDialogBinding
import com.cmu.project.main.details.book.libraries.BookDetailsPresenter
import com.cmu.project.main.details.book.libraries.BookDetailsAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CustomDialogFragment : BottomSheetDialogFragment(R.layout.custom_dialog),
    CustomDialogContract.View {

    private val libraryCollection = Firebase.firestore.collection("libraries")
    lateinit var binding: CustomDialogBinding
    override lateinit var presenter: CustomDialogPresenter
    private val args: CustomDialogFragmentArgs by navArgs()
    private var adapter: BookDetailsAdapter = BookDetailsAdapter(BookDetailsPresenter())


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CustomDialogBinding.bind(view)
        setupDialog()
        setLibraryName(args.title)
    }

    private fun setupDialog() {
        presenter = CustomDialogPresenter(this)
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
    }

    override fun addLibrary(geoPoint: GeoPoint) {
        presenter.addLibraryToRemoteCollection(binding.etLibraryName.text.toString(), geoPoint)
        dismiss()
    }

    override fun setupAddLibraryDialog() {
        binding.rvBooks.visibility = View.GONE
        binding.tvLibraryName.visibility = View.INVISIBLE
        binding.etLibraryName.visibility = View.VISIBLE
        binding.btnAction.text = "Add Library"
        binding.btnLibraryFavourite.setImageResource(R.drawable.ic_image)
    }

}