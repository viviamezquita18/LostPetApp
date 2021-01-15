package com.app.lostpetapp.ui.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddPetViewModel @ViewModelInject constructor(
    app: Application,
    private val petRepository: PetRepository

) : AndroidViewModel(app) {

    private val _dataImage = MutableLiveData<ByteArray>()
    val dataImage: LiveData<ByteArray> = _dataImage

    private val _status = MutableLiveData<NetworkApiStatus>()
    val status: LiveData<NetworkApiStatus> = _status

    private val _lastLocation = MutableLiveData<LatLng>()
    val lastLocation: LiveData<LatLng> = _lastLocation


    fun setData(data: ByteArray) {
        _dataImage.value = data
    }

    fun saveLocation(locationPreference: LocationPreference, location: LatLng) {
        viewModelScope.launch {
            locationPreference.save("latitude", location.latitude)
            locationPreference.save("longitude", location.longitude)
        }
    }

    fun readLastLocation(locationPreference: LocationPreference) {
        viewModelScope.launch {
            val latitude = locationPreference.readData("latitude")
            val longitude = locationPreference.readData("longitude")
            _lastLocation.value = LatLng(latitude, longitude)
        }
    }

    fun createPet(pet: Pet) {

        val fileReqBody: RequestBody =
            RequestBody.create(MediaType.parse("image/*"), _dataImage.value)
        val part =
            MultipartBody.Part.createFormData("photo", "f", fileReqBody)
        val description = RequestBody.create(
            MediaType.parse("text/plain"),
            pet.description
        )
        val latitude = RequestBody.create(
            MediaType.parse("text/plain"),
            pet.latitude.toString()
        )
        val longitude = RequestBody.create(
            MediaType.parse("text/plain"),
            pet.longitude.toString()
        )

        viewModelScope.launch {
            safePetCall(part, description, latitude, longitude)
        }
    }


    private suspend fun safePetCall(
        file: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ) {
        try {
            if (hasInternetConnection()) {
                val response = petRepository.createPet(file, description, latitude, longitude)
                _status.value = if (response.isSuccessful) NetworkApiStatus.DONE
                else NetworkApiStatus.ERROR
            } else {
                _status.value = NetworkApiStatus.NETWORK_ERROR
            }
        } catch (t: Throwable) {
            _status.value = NetworkApiStatus.ERROR
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
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}