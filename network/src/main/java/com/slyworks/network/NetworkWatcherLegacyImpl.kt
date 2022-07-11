package com.slyworks.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class NetworkWatcherLegacyImpl(private var context:Context?): NetworkWatcher {
    private var mCm: ConnectivityManager
    private var mConnectivityCallback: ConnectivityCallback? = null
    private var mNetworkRequest: NetworkRequest? = null
    private var mO: PublishSubject<Boolean>? = PublishSubject.create()

    init {
     mNetworkRequest =
         NetworkRequest.Builder()
             .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
             .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
             .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
             .build()

        mCm =  context!!
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        context = null
    }

    override fun getNetworkStatus(): Boolean {
        val networkInfo: NetworkInfo? =
            mCm.getActiveNetworkInfo()

        return (networkInfo != null && networkInfo.isConnected)
    }

    override fun subscribeTo(): Observable<Boolean> {
        mConnectivityCallback = ConnectivityCallback(mO)
        mCm.registerNetworkCallback(mNetworkRequest!!, mConnectivityCallback!!)
        return mO!!.startWithItem(getNetworkStatus()).hide()
    }

    override fun dispose() {
      mCm.unregisterNetworkCallback(mConnectivityCallback!!)
      mO = null
      mConnectivityCallback = null
    }
}