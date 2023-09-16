package app.slyworks.location_lib

import android.annotation.SuppressLint
import android.content.Context
import app.slyworks.utils_lib.Outcome
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

@SuppressLint("MissingPermission")
class PlacesApiHelper(private val context: Context){
    private val MAX_ENTRIES: Int = 5

    private val placesClient: PlacesClient

    init {
        Places.initialize(context.applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(context)
    }

    fun getCurrentPlaces(): Single<Outcome> =
        Single.create { emitter ->
            // Use fields to define the data types to return.
            val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = placesClient.findCurrentPlace(request)
            placesClient.findCurrentPlace(request)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful || task.result != null) {
                        Timber.e(task.exception)
                        emitter.onSuccess(Outcome.FAILURE(Unit, task.exception?.message))
                    }


                    val likelyPlaces: FindCurrentPlaceResponse? = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count: Int
                    if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < MAX_ENTRIES)
                        count = likelyPlaces.placeLikelihoods.size
                    else
                        count = MAX_ENTRIES


                    var i: Int = 0

                    val likelyPlaceNames: Array<String?> = arrayOfNulls(count)
                    val likelyPlaceAddresses: Array<String?> = arrayOfNulls(count)
                    val likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(count)
                    val likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(count)

                    val l: List<PlaceLikelihood> = likelyPlaces?.placeLikelihoods ?: emptyList()
                    for (placeLikelihood in l) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng

                        i++
                        if (i > count - 1)
                            break
                    }

                    val r: LocationPlaceDetails =
                        LocationPlaceDetails(likelyPlaceNames, likelyPlaceAddresses, likelyPlaceAttributions, likelyPlaceLatLngs)
                    emitter.onSuccess(Outcome.SUCCESS(r))
                }
        }
}