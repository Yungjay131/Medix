package app.slyworks.location_lib

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import app.slyworks.utils_lib.Outcome
import com.google.android.gms.location.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

@SuppressLint("MissingPermission")
class LocationApiHelper(private val context: Context){
    private val MIN_TIME_BETWEEN_UPDATES:Long = 1 * 60 * 1_000

    private val subject: PublishSubject<Location> = PublishSubject.create()

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val locationRequest: LocationRequest =
        LocationRequest.create()
            .apply {
                this.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                this.setInterval(MIN_TIME_BETWEEN_UPDATES)
            }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for(location in p0.locations){
                if(location == null)
                    continue

                subject.onNext(location)
            }
        }
    }


    fun startListeningForUpdates(){
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null)
    }

    fun stopListeningForUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getLocationUpdates(): Observable<Location> = subject.hide()

    fun getLastKnownLocation(): Single<Outcome> =
        Single.create { emitter ->
            fusedLocationClient.lastLocation
                .addOnCompleteListener {
                    if (!it.isSuccessful || it.result == null)
                        emitter.onSuccess(Outcome.FAILURE(Unit, it.exception?.message))

                    emitter.onSuccess(Outcome.SUCCESS(it.result!!))
                }
        }

    fun getCurrentLocation(): Single<Outcome> =
        Single.create { emitter ->
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener {
                    if (!it.isSuccessful || it.result == null)
                        emitter.onSuccess(Outcome.FAILURE(Unit, it.exception?.message))

                    emitter.onSuccess(Outcome.SUCCESS(it.result))
                }
        }
}