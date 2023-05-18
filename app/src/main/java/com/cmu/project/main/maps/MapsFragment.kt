package com.cmu.project.main.maps

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.core.dialog.CustomDialogFragment
import com.cmu.project.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(), MapsContract.View,
    NavigationView.OnNavigationItemSelectedListener {

    override lateinit var presenter: MapsPresenter
    private lateinit var binding: FragmentMapsBinding
    private lateinit var dialog: CustomDialogFragment

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        getLocationPermission()
        googleMap.isMyLocationEnabled = true
        googleMap.setMapStyle(context?.let {
            MapStyleOptions.loadRawResourceStyle(
                it,
                R.raw.style
            )
        })
        setupLibraryMarkers(googleMap)
        setupLibraryDetailsFragment(googleMap)
        setupMapSearchView(googleMap)
    }

    private fun getLocationPermission() {
        if (checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED)
        else ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION), 1)
    }

    override fun setupMapSearchView(googleMap: GoogleMap) {
        binding.svMapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = binding.svMapSearch.query.toString()
                context?.let { Geocoder(it) }?.getFromLocationName(location, 1)?.let { list ->
                    setMarker(googleMap, LatLng(list[0].latitude, list[0].longitude))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        binding.navView.setNavigationItemSelectedListener(this)

        binding.btnMenu.setOnClickListener { binding.drawerLayout.openDrawer(Gravity.LEFT) }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navSearch -> findNavController().navigate(R.id.action_mapsFragment_to_bookSearchFragment)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true
    }

    override fun setupLibraryMarkers(googleMap: GoogleMap) {
        lifecycleScope.launch(Dispatchers.IO) {
            presenter.retrieveLibrariesFromCloud().forEach { library ->
                val position = LatLng(library.location.latitude, library.location.longitude)
                withContext(Dispatchers.Main) {
                    val marker =
                        googleMap.addMarker(MarkerOptions().position(position).title(library.name))
                    val icon = createScaledBitmap(
                        decodeResource(
                            requireContext().resources,
                            R.drawable.iv_search
                        ), 80, 80, false
                    )
                    marker?.setIcon(fromBitmap(icon))
                }
            }
        }
    }

    override fun setupLibraryDetailsFragment(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener { coordinate ->
            findNavController().navigate(R.id.customDialogFragment)

            dialog.setupAddLibraryDialog()
            dialog.binding.btnAction.setOnClickListener {
                dialog.addLibrary(
                    GeoPoint(coordinate.latitude, coordinate.longitude)
                )
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.title?.let {
                val action = MapsFragmentDirections.actionMapsFragmentToCustomDialogFragment(it)
                findNavController().navigate(action)
            }
            true
        }
    }

    private fun setMarker(googleMap: GoogleMap, position: LatLng) {
        googleMap.addMarker(MarkerOptions().position(position))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10F))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = CustomDialogFragment()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        presenter = MapsPresenter(this)
        binding = FragmentMapsBinding.bind(view)
    }
}