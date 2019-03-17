package com.gradient.mapbox.mapboxgradient.Models

import com.google.firebase.database.Exclude

class Contacts(
        var id: String = "",
        var userId: String = "",
        var userName: String = "",
        var isNewCrimeReported: Boolean = false,
        var crimeLat: Double = 0.0,
        var crimeLng: Double = 0.0,
        var victimUserName: String = ""
) {

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result.put("id", id)
        result.put("userId", userId)
        result.put("userName", userName)
        result.put("isNewCrimeReported", isNewCrimeReported)
        result.put("crimeLat", crimeLat)
        result.put("crimeLng", crimeLng)
        result.put("victimUserName", victimUserName)

        return result
    }
}