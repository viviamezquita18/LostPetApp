package com.app.lostpetapp.repositories

import com.app.lostpetapp.api.ApiService
import com.app.lostpetapp.model.Pet
import okhttp3.MultipartBody
import okhttp3.RequestBody

import org.json.JSONObject
import retrofit2.Response

import javax.inject.Inject

class PetRepository @Inject constructor(
    private val service: ApiService
){
    suspend fun getPets(): Response<List<Pet>> {
        return service.getPets()
    }

    suspend fun createPet(
        file: MultipartBody.Part,
        description: RequestBody,
       latitude: RequestBody,
        longitude: RequestBody
    ): Response<String> {
        return service.createPet(file, description,latitude,longitude)
    }
}