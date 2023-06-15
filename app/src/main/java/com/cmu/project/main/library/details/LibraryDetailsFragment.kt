package com.cmu.project.main.library.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.TransitionManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cmu.project.R
import com.cmu.project.core.NetworkManager.checkWifiStatus
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentLibraryDetailsBinding
import com.cmu.project.main.library.details.libraries.LibraryDetailsAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

    private var wasLoaded = false
    override lateinit var presenter: LibraryDetailsPresenter
    private val args: LibraryDetailsFragmentArgs by navArgs()
    private lateinit var binding: FragmentLibraryDetailsBinding
    private var adapter: LibraryDetailsAdapter = LibraryDetailsAdapter(LibraryDetailsPresenter(this))
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private lateinit var library: Library

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryDetailsBinding.bind(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
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
        changeFavouriteButton(args.favourite)
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
        val uri =
            FileProvider.getUriForFile(requireContext(), "com.cmu.project.fileprovider", imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, 1)
    }

    private fun navigateToAddFragment() {
        val action = LibraryDetailsFragmentDirections.actionLibraryDetailsFragmentToAddBookFragment(
            library = args.library,
            coordinates = args.coordinates
        )

        findNavController().navigate(action)
    }

    override fun setLibraryName(name: String) {
        binding.tvLibraryName.text = name
    }

    override fun provideContext(): Context {
        return requireContext()
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
        val location = Gson().fromJson(args.coordinates, LatLng::class.java)
        googleMap.uiSettings.isScrollGesturesEnabled = false;
        if (location != null) {
            googleMap.addMarker(MarkerOptions().position(location))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15F))
        }
    }

    private fun setOnClickListeners() {
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ -> setRating(rating) }

        binding.btnRemoveBook.setOnClickListener { generateCameraIntent() }

        binding.btnAddBook.setOnClickListener { navigateToAddFragment() }

        binding.btnLibraryFavourite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                lifecycleScope.launch {
                    presenter.addLibraryToFavourites(
                        user,
                        Gson().fromJson(args.library, Library::class.java)
                    )
                }
            }
        }

        binding.imageView2.setOnClickListener {
            if(!wasLoaded) {
                wasLoaded = true
                setLibraryImage(force = true)
            }
        }
    }

    override fun setLibraryImage(force: Boolean) {
        if (checkWifiStatus(requireContext()) || force)
            CoroutineScope(Dispatchers.IO).launch {
                val uri = presenter.getLibraryImage(Gson().fromJson(args.library, Library::class.java))
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).load(uri)
                        .transform(CenterCrop(), RoundedCorners(25))
                        .into(binding.imageView2)
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