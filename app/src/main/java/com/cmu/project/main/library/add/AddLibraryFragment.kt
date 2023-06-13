package com.cmu.project.main.library.add

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cmu.project.R
import com.cmu.project.databinding.FragmentAddLibraryBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File


class AddLibraryFragment : BottomSheetDialogFragment(com.cmu.project.R.layout.fragment_add_library), AddLibraryContract.View {

    private var libraryImage: Bitmap? = null
    override lateinit var presenter: AddLibraryPresenter
    private val args: AddLibraryFragmentArgs by navArgs()
    private lateinit var binding: FragmentAddLibraryBinding

    private lateinit var currentPhotoPath: String

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 &&  resultCode == Activity.RESULT_OK){
            libraryImage = fixImageOrientation(BitmapFactory.decodeFile(currentPhotoPath))
            Glide.with(requireContext()).load(libraryImage).transform(CenterCrop(), RoundedCorners(25)).into(binding.imageView2)
        }
    }

    private fun generateCameraIntent() {
        val storage = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("photo", ".jpg", storage)
        currentPhotoPath = imageFile.absolutePath
        val uri = FileProvider.getUriForFile(requireContext(), "com.cmu.project.fileprovider", imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddLibraryBinding.bind(view)
        presenter = AddLibraryPresenter(this)
        setupDialog()
    }

    private fun setupDialog() {
        isCancelable = true
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnBack.setOnClickListener { dismiss() }

        binding.btnAction.setOnClickListener { addLibraryInfo() }

        binding.btnLibraryFavourite.setOnClickListener { generateCameraIntent() }
    }

    override fun getLibraryImage(): Bitmap? {
        return libraryImage
    }

    private fun addLibraryInfo() {
        val location = Gson().fromJson(args.coordinates, LatLng::class.java)
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (binding.tvLibraryName.text.isNotBlank() && libraryImage != null) {
            presenter.addLibraryToRemoteCollection(binding.tvLibraryName.text.toString(), geoPoint)
            Toast.makeText(context, "New Library Added.", Toast.LENGTH_LONG).show()
        } else
            Toast.makeText(context, "Some Library information is Missing!", Toast.LENGTH_LONG).show()

        dismiss()
    }

    private fun fixImageOrientation(bitmap: Bitmap): Bitmap {
        val degrees = 90f
        return rotateImage(bitmap, degrees)
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}