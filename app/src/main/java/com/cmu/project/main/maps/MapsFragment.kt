package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.core.activities.StartupActivity
import com.cmu.project.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(R.layout.fragment_maps), MapsContract.View, NavigationView.OnNavigationItemSelectedListener {

    override lateinit var presenter: MapsPresenter
    private lateinit var binding: FragmentMapsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        presenter = MapsPresenter(this)
        binding = FragmentMapsBinding.bind(view)
        FirebaseAuth.getInstance().signInWithEmailAndPassword("miguel.encarnacao@tecnico.ulisboa.pt", "12345678")
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.setMapStyle(context?.let { loadRawResourceStyle(it, R.raw.style) })
        setupListeners(googleMap)
        setupLibraryMarkers(googleMap)
    }

    @Suppress("DEPRECATION")
    override fun setupListeners(googleMap: GoogleMap) {
        binding.svMapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = binding.svMapSearch.query.toString()
                context?.let { Geocoder(it) }?.getFromLocationName(location, 1)?.let { list ->
                    if (list.isNotEmpty()) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(list[0].latitude, list[0].longitude), 25F))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        googleMap.setOnMapClickListener { position ->
            val action = MapsFragmentDirections.actionMapsFragmentToCustomDialogFragment(isAdd = true, library = "", coordinates = Gson().toJson(position))
            findNavController().navigate(action)
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.tag?.let { data ->
                val lib = Gson().toJson(data)
                val action = MapsFragmentDirections.actionMapsFragmentToCustomDialogFragment(library = lib, coordinates = null)
                findNavController().navigate(action)
            }
            true
        }

        binding.navView.setNavigationItemSelectedListener(this)

        binding.btnMenu.setOnClickListener { binding.drawerLayout.openDrawer(Gravity.LEFT) }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navSearch -> findNavController().navigate(R.id.action_mapsFragment_to_bookSearchFragment)
            // R.id.navLogout ->
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true
    }

    override fun setupLibraryMarkers(googleMap: GoogleMap) {
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.retrieveLibrariesFromCloud().forEach { library ->
                val position = LatLng(library.location.latitude, library.location.longitude)
                withContext(Dispatchers.Main) {
                    val marker = googleMap.addMarker(MarkerOptions().position(position).title(library.name))
                    marker?.tag = library
                    val icon = createScaledBitmap(decodeResource(requireContext().resources, R.drawable.iv_search), 80, 80, false)
                    marker?.setIcon(fromBitmap(icon))
                }
            }
        }
    }

}