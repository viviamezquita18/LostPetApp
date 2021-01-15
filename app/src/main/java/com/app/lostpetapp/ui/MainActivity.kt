package com.app.lostpetapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.app.lostpetapp.R
import com.app.lostpetapp.databinding.ActivityMainBinding
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var rxPermissions: RxPermissions
    private lateinit var navController: NavController
    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        rxPermissions = RxPermissions(this)
        requestLocationPermission()
        navController = Navigation.findNavController(
            this,
            R.id.myNavHostFragment
        )
        binding.bottomNav
            .setupWithNavController(navController)
    }

    private fun requestLocationPermission() {
        rxPermissions
            .request(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .subscribe { granted: Boolean ->
                if (granted) {
                    checkLocationEnabled()
                }
            }
    }

    private fun checkLocationEnabled() {
        if (!isLocationEnabled()) {
            showLocationAlert()
        }
    }

    private fun isLocationEnabled(): Boolean {
        locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showLocationAlert() {
        AlertDialog.Builder(this)
            .setMessage(R.string.enable_location)
            .setPositiveButton(R.string.ok_label) { dialog, id ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }.show()
    }
}