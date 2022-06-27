package com.slyworks.medix.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 *Created by Joshua Sylvanus, 1:03 AM, 1/12/2022.
 */
class ApiClient {
    //region Vars
    private val BASE_URL:String = "https://fcm.googleapis.com/fcm/"
    private var INSTANCE: Retrofit? = null
    //endregion
    private fun getRetrofit(){
        if(INSTANCE == null){
            INSTANCE = Retrofit.Builder()
                               .baseUrl(BASE_URL)
                               .addConverterFactory(GsonConverterFactory.create())
                               .build()
        }

    }

    fun getApiInterface(): FCMClientApi {
        getRetrofit()
        return INSTANCE!!.create(FCMClientApi::class.java)
    }
}