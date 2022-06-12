package com.slyworks.medix

import com.slyworks.models.models.FirebaseCloudMessage
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface FCMClientApi {
    @POST("send")
    @Headers("Content-type:application/json")
    fun sendCloudMessage(@Body message: FirebaseCloudMessage,
                         @Header("Authorization:key") key:String = BuildConfig.SERVER_KEY): Call<ResponseBody>
}