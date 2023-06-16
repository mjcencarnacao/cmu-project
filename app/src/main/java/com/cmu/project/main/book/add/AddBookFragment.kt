package com.cmu.project.main.book.add

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.cmu.project.R
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentAddBookBinding
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File


class AddBookFragment : DialogFragment(R.layout.fragment_add_book), AddBookContract.View {

    private var bookImage: Bitmap? = null
    override lateinit var presenter: AddBookPresenter
    private val args: AddBookFragmentArgs by navArgs()
    private lateinit var binding: FragmentAddBookBinding

    private lateinit var currentPhotoPath: String

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val scanner = BarcodeScanning.getClient()
            bookImage = rotateImage(BitmapFactory.decodeFile(currentPhotoPath))
            val image = InputImage.fromBitmap(bookImage!!, 0)
            scanner.process(image).addOnSuccessListener {
                it.forEach { code -> presenter.addBookToFirebaseWithCode(code.displayValue!!); dismiss() }
            }
            binding.btnBookImage.setImageResource(R.drawable.ic_correct)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddBookBinding.bind(view)
        presenter = AddBookPresenter(this)
        setupDialog()
    }

    private fun setupDialog() {
        isCancelable = true
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnAction.setOnClickListener { presenter.addBookToFirebaseWithoutCode(binding.etBookName.text.toString(), bookImage!!) }

        binding.btnBookImage.setOnClickListener { generateCameraIntent() }
    }

    override fun getBookImage(): Bitmap? {
        return bookImage
    }

    override fun dismissDialog() {
        dismiss()
    }

    override fun getLibraryName(): String {
        return Gson().fromJson(args.library, Library::class.java).name
    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}