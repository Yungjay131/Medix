package com.slyworks.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 *Created by Joshua Sylvanus, 4:19 PM, 29/05/2022.
 */
class ConnectivityCallback(private var o: PublishSubject<Boolean>?)
    : ConnectivityManager.NetworkCallback(){

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
    }

    override fun onAvailable(network: Network) {
       o!!.onNext(true)
    }

    override fun onUnavailable() {
       o!!.onNext(false)
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        /*val isConnected:Boolean = networkCapabilities
            .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        o!!.onNext(isConnected)*/
    }

}