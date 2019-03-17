package com.gradient.mapbox.mapboxgradient.Models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

data class Crime(
        var id: String = "",
        var userId: String = "",
        var userName: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var address: String = "",
        var time: Long = 0L,
        var totalScore: Double = 0.0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result.put("id", id)
        result.put("userId", userId)
        result.put("userName", userName)
        result.put("lat", lat)
        result.put("lng", lng)
        result.put("address", address)
        result.put("time", time)
        result.put("totalScore", totalScore)

        return result
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readDouble(),
            source.readDouble(),
            source.readString(),
            source.readLong(),
            source.readDouble()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(userId)
        writeString(userName)
        writeDouble(lat)
        writeDouble(lng)
        writeString(address)
        writeLong(time)
        writeDouble(totalScore)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Crime> = object : Parcelable.Creator<Crime> {
            override fun createFromParcel(source: Parcel): Crime = Crime(source)
            override fun newArray(size: Int): Array<Crime?> = arrayOfNulls(size)
        }
    }
}
