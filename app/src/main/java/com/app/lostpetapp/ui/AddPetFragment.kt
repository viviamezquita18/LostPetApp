package com.app.lostpetapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.app.lostpetapp.R
import com.app.lostpetapp.databinding.FragmentAddPetBinding
import com.app.lostpetapp.model.Pet
import com.app.lostpetapp.ui.viewModels.AddPetViewModel
import com.app.lostpetapp.util.LocationPreference
import com.app.lostpetapp.util.NetworkApiStatus
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException

@AndroidEntryPoint
class AddPetFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentAddPetBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var rxPermissions: RxPermissions
    private lateinit var locationManager: LocationManager
    val REQUEST_IMAGE_CAPTURE = 1
    private var filePath: Uri = Uri.EMPTY
    private val viewModel: AddPetViewModel by viewModels()
    private lateinit var pet: Pet
    private lateinit var locationPreferences: LocationPreference
    private var isPermissionGranted = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPetBinding.inflate(inflater, container, false)
        val view = binding.root
        pet = Pet()
        binding.pet = pet
        rxPermissions = RxPermissions(this)
        locationPreferences = LocationPreference(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initListeners()
        requestCameraPermission()
    }

    private fun initListeners() {
        binding.cameraButton.setOnClickListener {
            intentChooser()
        }
        binding.newPetButton.setOnClickListener {
            viewModel.createPet(pet)
            subscribeToStatusObserver()
        }
    }

    private fun intentChooser() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/jpeg"
        val chooser = Intent.createChooser(galleryIntent, "Select source")
        if (isPermissionGranted) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }
        startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            placeMarkerOnMap(latLng)
        }
    }

    private fun requestCameraPermission() {
        rxPermissions
            .request(
                Manifest.permission.CAMERA
            )
            .subscribe { granted: Boolean ->
                if (granted) {
                    isPermissionGranted = true
                }
            }
    }

    private fun isLocationEnabled(): Boolean {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestLocation() {
        fusedLocationClient = getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                pet.latitude = location.latitude
                pet.longitude = location.longitude
                placeMarkerOnMap(currentLatLng)
                viewModel.saveLocation(locationPreferences, currentLatLng)
            } else {
                subscribeToObserverLocation()
            }
        }.addOnFailureListener {
            Log.e("LOCATION", "FAILED")
        }
    }

    private fun subscribeToObserverLocation() {
        viewModel.readLastLocation(LocationPreference(requireContext()))
        viewModel.lastLocation.observe(requireActivity(), Observer {
            if (it.latitude != 0.0 && it.longitude != 0.0) {
                placeMarkerOnMap(it)
            }
        })
    }

    private fun placeMarkerOnMap(location: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Marker pointed at ")
        )
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(location).zoom(17f).build()
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (isLocationEnabled()) {
            requestLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val isCamera = activityResultSource(data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (isCamera) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                Glide.with(binding.clickImage).clear(binding.clickImage)
                binding.clickImage.setImageBitmap(imageBitmap)
                imageData()
            } else {
                if (data == null || data.data == null) {
                    return
                }
                filePath = data.data!!
                try {
                    val bitmap = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                            val source =
                                ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    filePath
                                )
                            ImageDecoder.decodeBitmap(source)
                        }
                        else -> MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver, filePath
                        )
                    }
                    Glide.with(binding.clickImage).clear(binding.clickImage)
                    binding.clickImage.setImageBitmap(bitmap)
                    imageData()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun activityResultSource(data: Intent?): Boolean {
        return if (data != null) data.action != null else false
    }

    private fun imageData() {
        val bitmap = (binding.clickImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        viewModel.setData(data)
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

    private fun showLocationAlert(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok_label) { dialog, id ->
                dialog.dismiss()
            }.show()
    }
}