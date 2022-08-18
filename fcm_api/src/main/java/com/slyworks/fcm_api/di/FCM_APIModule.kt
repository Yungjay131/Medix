package com.slyworks.fcm_api.di

import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.fcm_api.ApiClient
import com.slyworks.fcm_api.FCMClientApi
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 8:52 PM, 23/07/2022.
 */

@Module
object FCM_APIModule {
    //region Vars
    //endregion

    @Provides
    @ApplicationScope
    fun provideApiClient(): FCMClientApi {
        return ApiClient.getApiInterface()
    }
}