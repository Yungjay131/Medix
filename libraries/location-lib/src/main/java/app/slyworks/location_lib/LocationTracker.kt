package app.slyworks.location_lib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import app.slyworks.utils_lib.Outcome
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * Created by Joshua Sylvanus, 7:39 AM, 20-May-2023.
 */

class LocationTracker(private val context:Context,
                      private val locationApiHelper:LocationApiHelper,
                      private val placesApiHelper:PlacesApiHelper) {

    private val locationManager:LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val permissionSubject:PublishSubject<Boolean> = PublishSubject.create()

    private lateinit var resultLauncher: ActivityResultLauncher<IntentSenderRequest>

    fun initResultLauncher(activity:AppCompatActivity){
        resultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult())
        { result ->
            permissionSubject.onNext(result.resultCode == Activity.RESULT_OK)
        }
    }

    fun turnOnGPS(activity: AppCompatActivity): Observable<Boolean>{
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            permissionSubject.onNext(true)
        }

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val locationRequest:LocationRequest = LocationRequest.create()

        val locationSettingsRequest:LocationSettingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.locationSettingsStates?.isGpsPresent
                } else {
                    when ((it.exception as ApiException).statusCode) {
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            /* cant be fixed here, fix in settings */
                            val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                            permissionSubject.onNext(false)
                        }

                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            /* Show the dialog by calling startResolutionForResult(), and check the
                               result in onActivityResult() */
                            val rae: ResolvableApiException = it.exception as ResolvableApiException
                            // rae.startResolutionForResult(context as Activity, 1)


                            val isr: IntentSenderRequest = IntentSenderRequest.Builder(rae.resolution).build()

                            resultLauncher.launch(isr)
                        }

                    }
                }
            }

        return permissionSubject.hide()
    }

    fun getCurrentLocation(): Single<Outcome> = locationApiHelper.getCurrentLocation()
    fun getLocationUpdates(): Observable<Location> =locationApiHelper.getLocationUpdates()
}
