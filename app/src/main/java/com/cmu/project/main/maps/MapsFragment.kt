package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cmu.project.R
import com.cmu.project.core.activities.StartupActivity
import com.cmu.project.core.models.Library
import com.cmu.project.core.workmanager.WorkManagerScheduler
import com.cmu.project.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(R.layout.fragment_maps), MapsContract.View, NavigationView.OnNavigationItemSelectedListener {

    private var lastMarkerOpened: Marker? = null
    override lateinit var presenter: MapsPresenter
    private val args: MapsFragmentArgs by navArgs()
    private lateinit var binding: FragmentMapsBinding
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private val renderedLibraries: MutableMap<Library, Marker> = HashMap()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        presenter = MapsPresenter(this)
        binding = FragmentMapsBinding.bind(view)

        if (currentUser != null)
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userNameText).text =
                "Hi ${currentUser?.email}!"

        WorkManagerScheduler(requireContext())
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.setMapStyle(context?.let { loadRawResourceStyle(it, R.raw.style) })
        setupListeners(googleMap)
        presenter.cacheAddedLibrary(Gson().fromJson(args.library, Library::class.java))
        setupLibraryMarkers(googleMap)
    }

    @Suppress("DEPRECATION")
    override fun setupListeners(googleMap: GoogleMap) {
        binding.svMapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = binding.svMapSearch.query.toString()
                context?.let { Geocoder(it) }?.getFromLocationName(location, 1)?.let { list ->
                    if (list.isNotEmpty())
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(list[0].latitude, list[0].longitude), 25F))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        googleMap.setOnMapClickListener { position ->
            val action = MapsFragmentDirections.actionMapsFragmentToAddLibraryFragment(library = "", coordinates = Gson().toJson(position))
            findNavController().navigate(action)
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.tag?.let { data ->
                val lib = Gson().toJson(data)
                lifecycleScope.launch(Dispatchers.IO) {
                    val isFavourite = presenter.isFavouriteLibrary(Gson().fromJson(lib, Library::class.java))
                    withContext(Dispatchers.Main){
                        val action = MapsFragmentDirections.actionMapsFragmentToLibraryDetailsFragment(library = lib, coordinates = Gson().toJson(marker.position), favourite = isFavourite)
                        findNavController().navigate(action)
                    }
                }
            }
            true
        }

        binding.navView.setNavigationItemSelectedListener(this)

        googleMap.setOnMyLocationChangeListener { handleNearbyLibraries(it) }

        binding.btnMenu.setOnClickListener { binding.drawerLayout.openDrawer(Gravity.LEFT) }

        binding.btnRefresh.setOnClickListener { setupLibraryMarkers(googleMap, true) }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navSearch -> findNavController().navigate(R.id.action_mapsFragment_to_bookSearchFragment)
            R.id.navLogout -> signOut()
            R.id.navRate -> shareToSocialMedia()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startStartupActivity()
    }

    override fun startStartupActivity() {
        startActivity(Intent(context, StartupActivity::class.java)).also { activity?.finish() }
    }

    override fun provideContext(): Context {
        return requireContext()
    }

    private fun shareToSocialMedia() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, "Share and manage libraries effortlessly with LibrarIST.")
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share"))
    }

    override fun setupLibraryMarkers(googleMap: GoogleMap, refresh: Boolean) {
        googleMap.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.retrieveLibrariesFromCloud(refresh).forEach { library ->
                withContext(Dispatchers.Main) {
                    setupMarker(googleMap, library, presenter.isFavouriteLibrary(library))
                }
            }
        }
    }

    override fun setupMarker(googleMap: GoogleMap, library: Library, isFavourite: Boolean) {
        val icon = if(isFavourite)
            createScaledBitmap(decodeResource(requireContext().resources, R.drawable.iv_search_fav), 80, 80, false)
        else
            createScaledBitmap(decodeResource(requireContext().resources, R.drawable.iv_search), 80, 80, false)
        val location = LatLng(library.location.latitude, library.location.longitude)
        val marker = googleMap.addMarker(MarkerOptions().position(location).title(library.name))
        marker?.apply { setIcon(fromBitmap(icon)); tag = library }
    }

    private fun handleNearbyLibraries(location: Location) {
        val nearbyLibraries = HashMap<Float, Pair<Library, Marker>>()

        for ((library, marker) in renderedLibraries) {
            val libraryLocation = Location("")
            libraryLocation.latitude = library.location.latitude
            libraryLocation.longitude = library.location.longitude
            val distance = location.distanceTo(libraryLocation)
            if (distance <= 100)
                nearbyLibraries[distance] = Pair(library, marker)
        }

        if (nearbyLibraries.isNotEmpty()) {
            val nearestLibrary = nearbyLibraries.toSortedMap().entries.first().value
            if (lastMarkerOpened != nearestLibrary.second) {
                nearestLibrary.second.tag.let {
                    val library = nearestLibrary.first
                    val action = MapsFragmentDirections.actionMapsFragmentToLibraryDetailsFragment(library = Gson().toJson(library), coordinates = null)
                    findNavController().navigate(action)
                }
                lastMarkerOpened = nearestLibrary.second
            }
        }
    }

}