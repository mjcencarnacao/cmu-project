package com.cmu.project.core.dialog

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmu.project.R
import com.cmu.project.core.Constants.CAMERA_REQUEST_CODE
import com.cmu.project.databinding.CustomDialogBinding
import com.cmu.project.main.search.BookSearchPresenter
import com.cmu.project.core.dialog.libraries.LibraryDetailsAdapter
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CustomDialogFragment : BottomSheetDialogFragment(R.layout.custom_dialog),
    CustomDialogContract.View {

    private lateinit var binding: CustomDialogBinding
    override lateinit var presenter: CustomDialogPresenter
    private val args: CustomDialogFragmentArgs by navArgs()
    var adapter: LibraryDetailsAdapter = LibraryDetailsAdapter(CustomDialogPresenter(this))

    /*
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        Toast.makeText(context, "Result!", Toast.LENGTH_SHORT).show()
        // Save in DB
        if (result.resultCode == RESULT_OK) {
            val libImage = result.data?.data as Uri
            addLibraryInfo(libImage)
        }
    }
    */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("USER_LOGGED_IN", "User => ${FirebaseAuth.getInstance().currentUser?.email}")
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
        val library = Gson().fromJson(args.library, Library::class.java)
        if (args.isAdd) setupAddLibraryDialog() else setLibraryName(library.name)
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
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                lifecycleScope.launch {
                    presenter.addLibraryToFavourites(user, Gson().fromJson(args.library, Library::class.java))
                }
            }
        }

        binding.btnBack.setOnClickListener { dismiss() }

        binding.btnAction.setOnClickListener { addLibraryInfo() }
    }

    override fun addLibrary() {
        // Library photo taken from the camera
        startActivityForResult(
            requireActivity(),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            CAMERA_REQUEST_CODE,
            null
        )
    }

    private fun addLibraryInfo() {
        val location = Gson().fromJson(args.coordinates, LatLng::class.java)
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (binding.etLibraryName.text.isNotBlank()) {
            presenter.addLibraryToRemoteCollection(binding.etLibraryName.text.toString(), geoPoint)
            Toast.makeText(context, "New Library added!", Toast.LENGTH_SHORT).show()
        }
        else
            Toast.makeText(context, "Library information is missing!", Toast.LENGTH_SHORT).show()

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

    override fun goToBookDetails(bundle: Bundle) {
        this.findNavController().navigate(R.id.bookDetailsFragment, bundle)
    }

    override fun changeFavouriteBtn(isFav: Boolean) {
        if (isFav)
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite_red)
        else
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite)
    }
}