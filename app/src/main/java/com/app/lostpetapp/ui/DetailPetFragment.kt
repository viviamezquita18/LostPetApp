package com.app.lostpetapp.ui

import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.app.lostpetapp.R
import com.app.lostpetapp.databinding.FragmentDetailPetBinding
import com.app.lostpetapp.model.Pet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class DetailPetFragment : Fragment() {

    private lateinit var binding: FragmentDetailPetBinding
    private var pet = Pet()
    private lateinit var mMap: GoogleMap
    private var mapReady = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        animation.duration = 1000
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_pet, container, false)
        pet = DetailPetFragmentArgs.fromBundle(requireArguments()).pet

        val transition = DetailPetFragmentArgs.fromBundle(requireArguments()).transition
        binding.pet = pet
        binding.imagePet.transitionName = transition
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync {
            mMap = it
            mapReady = true
            updateMap()
        }
    }

    private fun updateMap() {
        if (mapReady) {
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(pet.latitude, pet.longitude))
                    .title("Last seen")
            )
            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(LatLng(pet.latitude, pet.longitude)).zoom(17f)
                        .build()
                )
            )
        }
    }
}