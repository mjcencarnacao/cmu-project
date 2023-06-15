package com.cmu.project.main.library.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cmu.project.R
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentLibraryDetailsBinding
import com.cmu.project.main.library.details.libraries.LibraryDetailsAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
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
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private lateinit var library: Library

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding = FragmentLibraryDetailsBinding.bind(view)
        binding.rvBooks.layoutManager = LinearLayoutManager(activity)
        binding.rvBooks.adapter = adapter
        presenter = LibraryDetailsPresenter(this)
        behavior = (dialog as? BottomSheetDialog)?.behavior!!
        lifecycleScope.launch(Dispatchers.IO) {
            val list = presenter.retrieveBooksFromLibrary()
            withContext(Dispatchers.Main) { adapter.updateList(list) }
        }
        setupDialog()
        library = Gson().fromJson(args.library, Library::class.java)
        setLibraryName(library.name)
        setLocationInfo(library.location)
        setLibraryImage()
        binding.ratingBar.rating = library.rating
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    private fun setRating(float: Float) {
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.sendRating(float)
        }
    }

    private fun setupDialog() {
        isCancelable = true
        setOnClickListeners()
        setDialogBehavior()
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

    @SuppressLint("SetTextI18n")
    fun setLocationInfo(point: GeoPoint) {
        val geocoder = Geocoder(requireContext())
        val address = geocoder.getFromLocation(point.latitude, point.longitude, 1)
        val city = address?.firstOrNull()?.locality
        val country = address?.firstOrNull()?.countryName
        val streetName = address?.firstOrNull()?.thoroughfare
        binding.libLocText.text = "$streetName\n$city/$country"
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = false
        googleMap.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.style) })

        /*
        val point = library.location

        val circleOptions = CircleOptions()
            .center(LatLng(point.latitude, point.longitude))
            .radius(20.0)
            .strokeColor(Color.BLUE)
            .fillColor(Color.argb(100, 100, 255, 200))

        googleMap.addCircle(circleOptions)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(point.latitude, point.longitude), 20f))

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12f), 1000, null);
        */
    }

    private fun setOnClickListeners() {
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->  setRating(rating)}

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

    override fun setLibraryImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val uri = presenter.getLibraryImage(Gson().fromJson(args.library, Library::class.java))
            withContext(Dispatchers.Main) {
                Glide.with(requireContext()).load(uri).transform(CenterCrop(), RoundedCorners(25)).into(binding.imageView2)            }
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
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite_filled)
        else
            binding.btnLibraryFavourite.setImageResource(R.drawable.ic_favorite)
    }

    private fun setDialogBehavior() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.guideline3.setGuidelinePercent(0f)
                        TransitionManager.beginDelayedTransition(binding.libraryImage)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.guideline3.setGuidelinePercent(0.5f)
                        TransitionManager.beginDelayedTransition(binding.libraryImage)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })
    }


}