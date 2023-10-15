package app.slyworks.base_feature

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dev.joshuasylvanus.navigator.Navigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by Joshua Sylvanus, 10:08 PM, 18/05/2022.
 */
object ActivityHelper {
    private const val TIMEOUT_IN_MILLIS = 3 * 60 * 1_000L

    private var foregroundStatus:Boolean = false
    private var count:Int = 0
    private var currentActivityTag:String = ""

    private var lastCheckedTime:Long = 0L
    private var disposable: Disposable = Disposable.empty()

    fun setForegroundStatus(status:Boolean, tag:String){
        if(status){
            currentActivityTag = tag
            foregroundStatus = status
        }else if(!status && (tag == currentActivityTag))
            foregroundStatus = status
    }

    fun isAppInForeground():Boolean = foregroundStatus

    fun isLastActivity():Boolean = count == 1
    fun incrementActivityCount():Int = count++
    fun decrementActivityCount():Int = count--
    fun getActivityCount():Int = count
    fun setActivityCount(count:Int){ this.count = count }

    fun cancelCountDown():Unit = disposable.dispose()

}

