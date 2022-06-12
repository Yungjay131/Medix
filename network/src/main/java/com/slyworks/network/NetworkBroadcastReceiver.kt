package com.slyworks.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.slyworks.network.NetworkWatcher
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


/*1 instance per application execution*/
class NetworkBroadcastReceiver : BroadcastReceiver(), com.slyworks.network.NetworkWatcher {
    //region Vars
    private var o:PublishSubject<Boolean>? = PublishSubject.create()
    private var networkStatus:Boolean = false
    //endregion

    /*this should return an Observable that components and classes can subscribe to*/
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ConnectivityManager.CONNECTIVITY_ACTION -> {
               networkStatus = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false)
                o!!.onNext(networkStatus)

                /*AppController.notifyObservers(EVENT_GET_NETWORK_UPDATES, !noConnectivityAvailable)
                NetworkManager.setNetworkStatus(!noConnectivityAvailable)*/
            }
        }
    }

    /*on 24+*/
    override fun subscribeTo(): Observable<Boolean> {
        return o!!.hide()
    }

    override fun getNetworkStatus():Boolean = networkStatus

    override fun dispose(){
       o = null
    }
}