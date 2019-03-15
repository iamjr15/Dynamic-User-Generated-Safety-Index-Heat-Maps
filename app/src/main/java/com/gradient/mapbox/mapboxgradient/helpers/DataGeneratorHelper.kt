package com.gradient.mapbox.mapboxgradient.helpers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.gradient.mapbox.mapboxgradient.Models.Volunteers
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

/**
 * This will generate random user data near my current location;
 */
object DataGeneratorHelper {

    private val TAG = "DataGeneratorHelper"

    private val volunteersData = arrayListOf<Pair<String, String>>(
            Pair("1", "Rajas Shubhashish"),
            Pair("2", "Srinivas Kallichuran"),
            Pair("3", "Yashodhara Ghosh"),
            Pair("4", "Dharmendu Choudhari"),
            Pair("5", "Jayashekhar Sumedh"),
            Pair("6", "Premendra Venkateshwara"),
            Pair("7", "Khazana Sangodkar"),
            Pair("8", "Rathin Sajja"),
            Pair("9", "Nimish Ghosal"),
            Pair("10", "Nabarun Sthanumurthy"))

    /**
     * This will generate random Volunteers data and save to Firebase DB.
     */
    fun generateSaveRandomVolunteersNearMe(myLocation: LatLng) {

        Observable.fromArray(volunteersData)
                .flatMapIterable { volunteer -> volunteer }
                .flatMap { volunteer ->
                    val location = LocationUtils.getRandomLocation(myLocation, 500)
                    val volunteers = Volunteers("",volunteer.first, volunteer.second, location.latitude, location.longitude)
                    VolunteersHelper.saveVolunteersRefInFireBaseDB(volunteers)
                }
                .subscribeBy(
                        onNext = {
                            if (it)
                                Log.i(TAG, "Added Volunteer info to DB.")
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    }

    fun clearPreviouslySavedVolunteersInfo() {
        VolunteersHelper.deleteVolunteersInfo()
    }
}