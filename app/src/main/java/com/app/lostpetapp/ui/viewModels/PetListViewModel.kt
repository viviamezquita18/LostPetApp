package com.app.lostpetapp.ui.viewModels

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.lostpetapp.BaseApplication
import com.app.lostpetapp.model.Pet
import com.app.lostpetapp.repositories.PetRepository
import com.app.lostpetapp.util.LocationPreference
import com.app.lostpetapp.util.NetworkApiStatus
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class PetListViewModel @ViewModelInject constructor(
    app: Application,
    private val petRepository: PetRepository

) : AndroidViewModel(app) {

    private val _status = MutableLiveData<NetworkApiStatus>()
    val status: LiveData<NetworkApiStatus> = _status

    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    private val _lastLocation = MutableLiveData<LatLng>()
    val lastLocation: LiveData<LatLng> = _lastLocation

    init {
        getPets()
    }

    fun readLastLocation(locationPreference: LocationPreference) {
        viewModelScope.launch {
            val latitude = locationPreference.readData("latitude")
            val longitude = locationPreference.readData("longitude")
            _lastLocation.value = LatLng(latitude, longitude)
        }
    }

    private fun getPets() = viewModelScope.launch {
        safePetCall()
    }

    private suspend fun safePetCall() {
        try {
            if (hasInternetConnection()) {
                val response = petRepository.getPets()
                if (response.isSuccessful) {
                    val pets= response.body() ?: ArrayList()
                    Log.e("location",pets.toString())
                        for (pet in pets) {
                            pet.city = getCityName(pet.latitude, pet.longitude).toString() }
                    _pets.value = pets
                    _status.value = NetworkApiStatus.DONE
                }
            } else {
                _status.value = NetworkApiStatus.NETWORK_ERROR
                _pets.value = ArrayList()
            }

        } catch (t: Throwable) {
            _status.value = NetworkApiStatus.ERROR
            _pets.value = ArrayList()
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<BaseApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        val gcd = Geocoder(getApplication(), Locale.getDefault())
        val address = gcd.getFromLocation(latitude, longitude, 1)
        Log.e("location",address.toString())
        return "${address[0].locality}, ${address[0].adminArea}"
    }
}