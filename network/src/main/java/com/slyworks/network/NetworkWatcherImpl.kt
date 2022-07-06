package com.slyworks.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.subjects.PublishSubject


/*TODO:use Dagger here to pass ApplicationContext*/
class NetworkWatcherImpl(private var context:Context?) : NetworkWatcher {
    //region Vars
    private var mCm:ConnectivityManager
    private var mConnectivityCallback: ConnectivityCallback? = null
    private var mNetworkRequest: NetworkRequest? = null
    private var mO:PublishSubject<Boolean>? = PublishSubject.create()
    //endregion

    init{
        mNetworkRequest =
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

        mCm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        context = null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getNetworkStatus(): Boolean {
        val network: Network = mCm.activeNetwork ?: return false
        val capabilities = mCm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @SuppressLint("NewApi")
    override fun subscribeTo(): Observable<Boolean> {
        mConnectivityCallback = ConnectivityCallback(mO!!)
        mCm.registerNetworkCallback(mNetworkRequest!!, mConnectivityCallback!!)

        /*calling it first time*/
        return mO!!.hide()

    }

    override fun dispose() {
        if(mConnectivityCallback != null)
           mCm.unregisterNetworkCallback(mConnectivityCallback!!)

        mO = null
        mConnectivityCallback = null
    }
}

