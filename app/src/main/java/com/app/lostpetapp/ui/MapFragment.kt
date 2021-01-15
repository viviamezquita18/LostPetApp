package com.app.lostpetapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.app.lostpetapp.R
import com.app.lostpetapp.databinding.FragmentMapBinding
import com.app.lostpetapp.ui.viewModels.PetListViewModel
import com.app.lostpetapp.util.LocationPreference
import com.app.lostpetapp.util.MarkerClusterRenderer
import com.app.lostpetapp.util.NetworkApiStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: PetListViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private lateinit var binding: FragmentMapBinding
    private lateinit var clusterManager: ClusterManager<MyItem>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync {
            mMap = it
            mapReady = true
           subscribeToObserverLocation()
            subscribeToStatusObserver()
        }
    }

    private fun updateMap(lastLocation : LatLng) {
        if (mapReady) {
            setUpClusterer(lastLocation )
            subscribeToObserverPet()
        }
    }

    private fun setUpClusterer(lastLocation : LatLng) {
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(lastLocation).zoom(5f).build()
            )
        )
        clusterManager = ClusterManager(context, mMap)
        clusterManager.renderer = MarkerClusterRenderer(requireContext(),mMap,clusterManager)
        mMap.setOnCameraIdleListener(clusterManager)
          mMap.setOnMarkerClickListener(clusterManager)

    }


    private fun subscribeToObserverLocation(){
        viewModel.readLastLocation(LocationPreference(requireContext()))
        viewModel.lastLocation.observe(requireActivity(), Observer {
            Log.e("last", "$it")
            updateMap(it)
        })
    }

    private fun subscribeToObserverPet() {
        viewModel.pets.observe(requireActivity(), Observer {
            if (it.isNotEmpty()) {
                for (pet in it) {
                    val item = MyItem(pet.latitude, pet.longitude, "Last seen", "${pet._date}",pet.photo)
                    clusterManager.addItem(item)
                }
            }
        })
    }

    inner class MyItem(
        lat: Double,
        lng: Double,
        title: String,
        snippet: String,
        url:String
    ) : ClusterItem {

        private val position: LatLng
        private val title: String
        private val snippet: String
        val url: String

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String? {
            return title
        }

        override fun getSnippet(): String? {
            return snippet
        }

        init {
            position = LatLng(lat, lng)
            this.title = title
            this.snippet = snippet
            this.url = url
        }
    }

    private fun subscribeToStatusObserver() {
        viewModel.status.observe(requireActivity(), Observer {
            when (it) {
                NetworkApiStatus.NETWORK_ERROR -> {
                    showLocationAlert(getString(R.string.no_internet_connection))
                }
                NetworkApiStatus.ERROR -> {
                    showLocationAlert(getString(R.string.no_server_connection))
                }

            }

        })
    }

    private fun showLocationAlert(message:String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok_label) { dialog, id ->
                dialog.dismiss()

            }.show()

    }

}



