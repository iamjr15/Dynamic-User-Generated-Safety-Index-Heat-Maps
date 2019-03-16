package com.gradient.mapbox.mapboxgradient.Models

import com.google.firebase.database.Exclude

class AreaReview(
        var id: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var address: String = "",
        var time: Long = 0L
) {

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result.put("id", id)
        result.put("lat", lat)
        result.put("lng", lng)
        result.put("address", address)
        result.put("time", time)

        return result
    }
}
