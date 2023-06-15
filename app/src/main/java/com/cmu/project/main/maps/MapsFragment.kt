package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(R.layout.fragment_maps), MapsContract.View,
    NavigationView.OnNavigationItemSelectedListener {

    override lateinit var presenter: MapsPresenter
    private lateinit var binding: FragmentMapsBinding
    private val renderedLibraries: MutableMap<Library, Marker> = HashMap()
    private var lastMarkerOpened: Marker? = null
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userLocation: Location? = null

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
        setupLibraryMarkers(googleMap)
    }

    override fun provideContext(): Context {
        return requireContext()
    }

    @Suppress("DEPRECATION")
    override fun setupListeners(googleMap: GoogleMap) {
        binding.svMapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = binding.svMapSearch.query.toString()
                context?.let { Geocoder(it) }?.getFromLocationName(location, 1)?.let { list ->
                    if (list.isNotEmpty()) googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(list[0].latitude, list[0].longitude),
                            25F
                        )
                    )
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        googleMap.setOnMapClickListener { position ->
            val action = MapsFragmentDirections.actionMapsFragmentToAddLibraryFragment(
                library = "",
                coordinates = Gson().toJson(position)
            )
            findNavController().navigate(action)
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.tag?.let { data ->
                val lib = Gson().toJson(data)
                var isFavourite = false
                lifecycleScope.launch(Dispatchers.Main) {
                    isFavourite = isFavouriteLibrary(Gson().fromJson(lib, Library::class.java))
                    val action = MapsFragmentDirections.actionMapsFragmentToLibraryDetailsFragment(
                        library = lib,
                        coordinates = null,
                        favourite = isFavourite
                    )
                    findNavController().navigate(action)
                }
            }
            true
        }

        googleMap.setOnMyLocationChangeListener {
            val nearbyLibs = HashMap<Float, Pair<Library, Marker>>()

            for ((lib, marker) in renderedLibraries) {
                val libLoc = Location("")
                libLoc.longitude = lib.location.longitude
                libLoc.latitude = lib.location.latitude

                val distance = it.distanceTo(libLoc)
                if (distance <= 100)
                    nearbyLibs[distance] = Pair(lib, marker)
            }

            if (nearbyLibs.isEmpty())
                return@setOnMyLocationChangeListener

            val nearestLib = nearbyLibs.toSortedMap().entries.first().value
            if (lastMarkerOpened != nearestLib.second) {
                nearestLib.second.tag.let {
                    val library = nearestLib.first
                    val action = MapsFragmentDirections.actionMapsFragmentToLibraryDetailsFragment(
                        library = Gson().toJson(library),
                        coordinates = null
                    )
                    findNavController().navigate(action)
                }
                lastMarkerOpened = nearestLib.second
            }

            userLocation = it
        }

        binding.navView.setNavigationItemSelectedListener(this)

        binding.btnMenu.setOnClickListener { binding.drawerLayout.openDrawer(Gravity.LEFT) }

        binding.btnRefresh.setOnClickListener { setupLibraryMarkers(googleMap, true) }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navSearch -> findNavController().navigate(R.id.action_mapsFragment_to_bookSearchFragment)
            R.id.navLogout -> signOut()
            R.id.navRate -> shareToSocialMedia()

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startStartupActivity()
    }

    private fun shareToSocialMedia() {
        val intent = Intent();
        intent.action = Intent.ACTION_SEND;
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Share and manage libraries effortlessly with LibrarIST."
        );
        intent.type = "text/plain";
        startActivity(Intent.createChooser(intent, "Share"));
    }

    override fun startStartupActivity() {
        startActivity(Intent(context, StartupActivity::class.java)).also { activity?.finish() }
    }

    @SuppressLint("MissingPermission")
    override fun setupLibraryMarkers(googleMap: GoogleMap, refresh: Boolean) {
        googleMap.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.retrieveLibrariesFromCloud(refresh).forEach { library ->
                val position = LatLng(library.location.latitude, library.location.longitude)
                withContext(Dispatchers.Main) {
                    val marker =
                        googleMap.addMarker(MarkerOptions().position(position).title(library.name))
                    marker?.tag = library

                    val icon: Bitmap

                    if (isFavouriteLibrary(library)) {
                        icon = createScaledBitmap(
                            decodeResource(
                                requireContext().resources,
                                R.drawable.iv_search_fav
                            ), 80, 80, false
                        )
                    } else {
                        icon = createScaledBitmap(
                            decodeResource(
                                requireContext().resources,
                                R.drawable.iv_search
                            ), 80, 80, false
                        )
                    }

                    marker?.setIcon(fromBitmap(icon))
                    if (marker != null)
                        renderedLibraries[library] = marker
                }
            }
        }
    }

    private suspend fun isFavouriteLibrary(library: Library): Boolean {
        try {
            val userRef = Firebase.firestore.collection("users")
                .whereEqualTo("email", currentUser?.email)
                .get()
                .await().documents[0].reference

            val libRef = Firebase.firestore.collection("libraries").document(library.id)

            var isFav = false
            val userFavourites = userRef.get().await().get("favourites") as List<*>

            for (fav in userFavourites) {
                if (fav is DocumentReference) {
                    if (fav == libRef) {
                        Log.i("COMPARE_REFS", "$fav vs $libRef")
                        isFav = true
                        break
                    }
                }
            }
            return isFav

        } catch (e: Exception) {
            return false
        }
    }

}