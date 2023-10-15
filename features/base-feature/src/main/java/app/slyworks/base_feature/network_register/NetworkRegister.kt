package app.slyworks.base_feature.network_register

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow


/**
 * Created by Joshua Sylvanus, 11:27 AM, 29/05/2022.
 */

interface INetworkRegister{
    fun getNetworkStatus():Boolean
    fun subscribeToNetworkUpdates():Observable<Boolean>
    fun unsubscribeToNetworkUpdates():Unit
}

@SuppressLint("NewApi")
class NetworkRegister(private val context: Context) : INetworkRegister{
    //region Vars
    private var impl: NetworkWatcher
    //endregion

    init{
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            impl = NetworkWatcherLegacyImpl(context)
        else
            impl = NetworkWatcherImpl(context)
    }

    override fun getNetworkStatus(): Boolean =
        impl.getNetworkStatus()

    override fun subscribeToNetworkUpdates():Observable<Boolean> =
        impl.subscribeTo()

    override fun unsubscribeToNetworkUpdates():Unit =
        impl.dispose()


}