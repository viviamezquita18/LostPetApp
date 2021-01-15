package com.app.lostpetapp.api

import com.app.lostpetapp.model.Pet
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("lostPets")
    suspend fun getPets():
            Response<List<Pet>>

    @Multipart
    @POST("lostPets")
    suspend fun createPet(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Response<String>

}