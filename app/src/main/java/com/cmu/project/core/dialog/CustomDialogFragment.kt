package com.cmu.project.core.dialog

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult
import com.cmu.project.R
import com.cmu.project.core.Constants.CAMERA_REQUEST_CODE
import com.cmu.project.databinding.CustomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.GeoPoint


class CustomDialogFragment(private val activity: Activity) : BottomSheetDialog(activity),
    CustomDialogContract.View {

    lateinit var binding: CustomDialogBinding
    override lateinit var presenter: CustomDialogPresenter

    init {
        setupDialog()
    }

    private fun setupDialog() {
        binding = CustomDialogBinding.inflate(activity.layoutInflater)
        presenter = CustomDialogPresenter(this)
        setContentView(binding.root)
        setCancelable(true)
        create()
        setOnClickListeners()
        show()
    }

    override fun setLibraryName(name: String) {
        binding.tvLibraryName.text = name
    }

    override fun setLibraryDescription(description: String) {
        binding.tvDescription.text = description
    }

    private fun setOnClickListeners() {
        binding.btnLibraryFavourite.setOnClickListener { startActivityForResult(activity, Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE, null) }
        binding.btnBack.setOnClickListener { dismiss() }
    }

    override fun addLibrary(geoPoint: GeoPoint) {
        presenter.addLibraryToRemoteCollection(binding.etLibraryName.text.toString(), geoPoint)
        dismiss()
    }

    override fun setupAddLibraryDialog() {
        binding.tvLibraryName.visibility = View.INVISIBLE
        binding.etLibraryName.visibility = View.VISIBLE
        binding.btnAction.text = context.resources.getString(R.string.add_library)
        binding.btnLibraryFavourite.setImageResource(R.drawable.ic_image)
    }

}