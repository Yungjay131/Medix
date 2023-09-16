package app.slyworks.location_lib

import com.google.android.gms.maps.model.LatLng

data class LocationPlaceDetails(
    val likelyPlaceNames:Array<String?>,
    val likelyPlaceAddresses:Array<String?>,
    val likelyPlaceAttributions:Array<List<*>?>,
    val likelyPlaceLatLngs:Array<LatLng?>){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationPlaceDetails

        if (!likelyPlaceNames.contentEquals(other.likelyPlaceNames)) return false
        if (!likelyPlaceAddresses.contentEquals(other.likelyPlaceAddresses)) return false
        if (!likelyPlaceAttributions.contentEquals(other.likelyPlaceAttributions)) return false
        if (!likelyPlaceLatLngs.contentEquals(other.likelyPlaceLatLngs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = likelyPlaceNames.contentHashCode()
        result = 31 * result + likelyPlaceAddresses.contentHashCode()
        result = 31 * result + likelyPlaceAttributions.contentHashCode()
        result = 31 * result + likelyPlaceLatLngs.contentHashCode()
        return result
    }

}