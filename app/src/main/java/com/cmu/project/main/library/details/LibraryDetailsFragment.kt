package com.cmu.project.main.library.details

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.EXTRA_OUTPUT
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmu.project.R
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentLibraryDetailsBinding
import com.cmu.project.main.library.details.libraries.LibraryDetailsAdapter
import com.cmu.project.main.maps.MapsFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LibraryDetailsFragment : BottomSheetDialogFragment(R.layout.fragment_library_details),
    LibraryDetailsContract.View {

    private lateinit var currentPhotoPath: String

    override lateinit var presenter: LibraryDetailsPresenter
    private val args: LibraryDetailsFragmentArgs by navArgs()
    private lateinit var binding: FragmentLibraryDetailsBinding
    private var adapter: LibraryDetailsAdapter = LibraryDetailsAdapter(LibraryDetailsPresenter(this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryDetailsBinding.bind(view)
        binding.rvBooks.layoutManager = LinearLayoutManager(activity)
        binding.rvBooks.adapter = adapter
        presenter = LibraryDetailsPresenter(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val list = presenter.retrieveBooksFromLibrary()
            withContext(Dispatchers.Main) { adapter.updateList(list) }
        }
        setupDialog()
        val library = Gson().fromJson(args.library, Library::class.java)
        setLibraryName(library.name)
    }

    private fun setupDialog() {
        isCancelable = true
        setOnClickListeners()
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val scanner = BarcodeScanning.getClient()
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            scanner.process(image).addOnSuccessListener {
                it.forEach { code -> presenter.removeBookFromLibrary(code.displayValue!!); dismiss() }
            }
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

    private fun navigateToAddFragment() {
        val action = LibraryDetailsFragmentDirections.actionLibraryDetailsFragmentToAddBookFragment(library = args.library, coordinates = args.coordinates)

        findNavController().navigate(action)

    }

    override fun setLibraryName(name: String) {
        binding.tvLibraryName.text = name
    }

    private fun setOnClickListeners() {
        binding.btnRemoveBook.setOnClickListener { generateCameraIntent() }

        binding.btnAddBook.setOnClickListener { navigateToAddFragment() }

        binding.btnLibraryFavourite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                lifecycleScope.launch {
                    presenter.addLibraryToFavourites(user, Gson().fromJson(args.library, Library::class.java))
                }
            }
        }
    }

    override fun getLibraryName(): String {
        return binding.tvLibraryName.text.toString()
    }

    override fun goToBookDetails(bundle: Bundle) {
        this.findNavController().navigate(R.id.bookDetailsFragment, bundle)
    }

    override fun changeFavouriteButton(isFav: Boolean) {
        if (isFav)
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite_red)
        else
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite)
    }
}