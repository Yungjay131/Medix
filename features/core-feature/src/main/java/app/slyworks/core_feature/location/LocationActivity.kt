package app.slyworks.core_feature.location

import android.Manifest
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.slyworks.base_feature.PermissionManager
import app.slyworks.core_feature.R
import app.slyworks.core_feature._di.ActivityComponent
import app.slyworks.core_feature.databinding.ActivityLocationBinding
import app.slyworks.location_lib.LocationPlaceDetails
import app.slyworks.location_lib.LocationTracker
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.onNextAndComplete
import app.slyworks.utils_lib.utils.plusAssign
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by Joshua Sylvanus, 2:13 PM, 21-May-2023.
 */
const val KEY_CAMERA_POSITION = "key_camera_position"
const val KEY_LOCATION = "key_location"
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private val DEFAULT_ZOOM:Float = 15f

    private var isPermissionGranted:Boolean = false

    private val disposables:CompositeDisposable = CompositeDisposable()

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var lastKnownLocation: Location? = null

    private lateinit var binding: ActivityLocationBinding

    @Inject
    lateinit var locationTracker: LocationTracker

    @Inject
    lateinit var permissionsManager: PermissionManager

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        if(savedInstanceState != null){
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
        ActivityComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    private fun initData(){
        locationTracker.initResultLauncher(this)

        permissionsManager.initialize(this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION )

        disposables +=
        permissionsManager.getPermissionsObservable()
            .flatMap {
                isPermissionGranted = it.isSuccess

                if(!it.isSuccess) Observable.just(it)
                else {
                   locationTracker.turnOnGPS(this)
                       .map {
                           if(it)
                               Outcome.SUCCESS(Unit)
                           else Outcome.FAILURE(Unit, "failed to turn on GPS")
                       }.flatMap {
                           if(it.isSuccess){
                               locationTracker.getCurrentLocation()
                                   .toObservable()
                           }
                           else Observable.just(it)
                       }
                }
            }
            .subscribe{
                if(!it.isSuccess){
                    displayMessage(it.getAdditionalInfo()!!, binding.root)
                    finish()
                }

                lastKnownLocation = it.getTypedValue()

                val latLng: LatLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                val update: CameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                map?.moveCamera(update)
           }

        permissionsManager.requestPermissions()
    }

    private fun initViews(){
        binding.fabGetCurrentPlace.setOnClickListener {}

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(p0: GoogleMap) {
        if (map == null){
            displayMessage("map was not loaded. please try again", binding.root)
            return
        }

        try {
            if (isPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null

                permissionsManager.requestPermissions()
            }
        } catch (e: SecurityException) {
            Timber.e(e, "error getting location")
        }
    }

    private fun getCurrentLocation(){
        disposables +=
        locationTracker.getCurrentLocation()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::updateMap)
    }

    private fun updateMap(o:Outcome){
        when{
            o.isSuccess -> {
                lastKnownLocation = o.getTypedValue()

                val latLng:LatLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                val update:CameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM)
                map?.moveCamera(update)
            }

            o.isFailure -> {
                val update:CameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(defaultLocation,DEFAULT_ZOOM)
                map?.moveCamera(update)
                map?.uiSettings?.isMyLocationButtonEnabled = false
            }
        }
    }
}