package com.cmu.project.main.maps

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.cmu.project.R
import com.cmu.project.core.models.Library
import com.cmu.project.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MapsFragment : Fragment() {

    private lateinit var binding: FragmentMapsBinding

    private val libraryCollection = Firebase.firestore.collection("libraries")

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.isMyLocationEnabled = true

        binding.svMapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = binding.svMapSearch.query.toString()
                val geoCoder = context?.let { Geocoder(it) }
                val list = geoCoder?.getFromLocationName(location, 1)
                val latlng = LatLng(list?.get(0)!!.latitude, list.get(0)!!.longitude)
                googleMap.addMarker(MarkerOptions().position(latlng))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10F))
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        retrieveLibraries(googleMap)
    }

    private fun retrieveLibraries(googleMap: GoogleMap) = CoroutineScope(Dispatchers.IO).launch {
        val snapshot = libraryCollection.get().await()
        for (document in snapshot) {
            val library = document.toObject(Library::class.java)
            withContext(Dispatchers.Main) {
                googleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            library.location.latitude,
                            library.location.longitude
                        )
                    ).title(library.name)
                )
            }
        }
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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding = FragmentMapsBinding.bind(view)
    }
}