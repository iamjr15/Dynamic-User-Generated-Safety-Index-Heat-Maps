package com.gradient.mapbox.mapboxgradient.helpers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.gradient.mapbox.mapboxgradient.Models.Contacts
import com.gradient.mapbox.mapboxgradient.Models.Volunteers
import com.gradient.mapbox.mapboxgradient.Preferences
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

    private val contactsData = arrayListOf<Pair<String, String>>(
            Pair("1", "Vishwambhar Sarwate"),
            Pair("2", "Taruntapan Sudesha"),
            Pair("3", "Anarghya Cheran"),
            Pair("4", "Vir Pamela"),
            Pair("5", "Neelanjan Subramanian"),
            Pair("6", "Premendra Venkateshwara"),
            Pair("7", "Khazana Sangodkar"),
            Pair("8", "Gourishankar Govindraj"),
            Pair("9", "Nimish Ghosal"),
            Pair("10", "Sujan Nirupa"))

    /**
     * This will generate random Volunteers data and save to Firebase DB.
     */
    fun generateSaveRandomVolunteersNearMe(myLocation: LatLng) {

        val volunteersAdded = Preferences.getInstance().getBoolean(Preferences.SP_VOLUNTEERS_ADDED, false)

        if (!volunteersAdded) {
            Observable.fromArray(volunteersData)
                    .flatMapIterable { volunteer -> volunteer }
                    .flatMap { volunteer ->
                        val location = LocationUtils.getRandomLocation(myLocation, 500)
                        val volunteers = Volunteers("", volunteer.first, volunteer.second, location.latitude, location.longitude)
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

            //Set 'true' if list of volunteers are added to DB
            Preferences.getInstance().save(Preferences.SP_VOLUNTEERS_ADDED, true)
        }
    }

    /**
     * This will generate random Contacts data and save to Firebase DB.
     */
    fun generateMockedContactsData() {

        val contactsAdded = Preferences.getInstance().getBoolean(Preferences.SP_CONTACTS_ADDED, false)

        if (!contactsAdded) {
            Observable.fromArray(contactsData)
                    .flatMapIterable { list -> list }
                    .flatMap {
                        val contacts = Contacts("", it.first, it.second)
                        ContactsHelper.saveContactsRefInFireBaseDB(contacts)
                    }
                    .subscribeBy(
                            onNext = {
                                if (it)
                                    Log.i(TAG, "Added Contacts info to DB.")
                            },
                            onError = {
                                it.printStackTrace()
                            }
                    )

            //Set 'true' if list of volunteers are added to DB
            Preferences.getInstance().save(Preferences.SP_CONTACTS_ADDED, true)
        }
    }

    fun clearPreviouslySavedVolunteersInfo() {
        Preferences.getInstance().save(Preferences.SP_VOLUNTEERS_ADDED, false)
        Preferences.getInstance().save(Preferences.SP_CONTACTS_ADDED, false)
        VolunteersHelper.deleteVolunteersInfo()
        ContactsHelper.deleteContactsInfo()
    }
}