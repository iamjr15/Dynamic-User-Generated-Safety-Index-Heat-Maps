package com.gradient.mapbox.mapboxgradient.helpers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.gradient.mapbox.mapboxgradient.BuildConfig
import com.gradient.mapbox.mapboxgradient.Models.Crime


object CrimeReportHelper {

    private val TAG = "CrimeReportHelper"

    //Firebase DB Tables
    private const val CRIME_ROOT = "crime"

    val dbRef by lazy { FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL) }

    fun reportCrime(crime: Crime) {

        val databaseReference = dbRef.child(CRIME_ROOT)

        val key = databaseReference.push().key

        getCurrentUser()?.let {
            crime.userId = it.uid
            crime.userName = it.displayName!!
        }

        crime.id = key!!

        val values = crime.toMap()

        val updates: HashMap<String, Any> = hashMapOf()

        updates["/$CRIME_ROOT/$key"] = values

        dbRef.updateChildren(updates).addOnCompleteListener {
            Log.i(TAG, "Crime reported!")
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }


    /**
     * This will return current signed in 'FireBase User
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}