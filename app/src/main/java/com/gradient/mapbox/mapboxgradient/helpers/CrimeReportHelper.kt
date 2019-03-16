package com.gradient.mapbox.mapboxgradient.helpers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gradient.mapbox.mapboxgradient.BuildConfig
import com.gradient.mapbox.mapboxgradient.Models.Crime
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy


object CrimeReportHelper {

    private val TAG = "CrimeReportHelper"

    //Firebase DB Tables
    private const val CRIME_ROOT = "crime"

    val dbRef by lazy { FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL) }

    fun reportCrime(crime: Crime) {

        isCrimeReportedForArea(crime)
                .subscribeBy(
                        onNext = { reported ->
                            if (reported.id.isNotEmpty()) {
                                Log.i(TAG, "Crime already reported, update crime! ${reported.id}")
                                saveCrime(reported.id, crime)
                            } else {
                                //If crime is new for area
                                val databaseReference = dbRef.child(CRIME_ROOT)

                                val key = databaseReference.push().key
                                saveCrime(key, crime)
                            }
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    }

    private fun saveCrime(key: String?, crime: Crime) {
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

    private fun isCrimeReportedForArea(crime: Crime): Observable<Crime> {

        return Observable.create { emitter ->
            val databaseReference = dbRef.child(CRIME_ROOT)

            databaseReference.orderByChild("address").equalTo(crime.address).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.children.count() > 0) {
                            dataSnapshot.children.first().getValue(Crime::class.java)?.let { dbData ->
                                Log.i(TAG, "Crime found with address: ${crime.address}")

                                if (!emitter.isDisposed) {
                                    emitter.onNext(dbData)
                                    emitter.onComplete()
                                }
                            }
                        } else {
                            if (!emitter.isDisposed) {
                                emitter.onNext(crime)
                                emitter.onComplete()
                            }
                        }
                    } else {
                        if (!emitter.isDisposed) {
                            emitter.onNext(crime)
                            emitter.onComplete()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    databaseError.toException().stackTrace

                    if (!emitter.isDisposed) {
                        emitter.onComplete()
                    }
                }
            })
        }
    }


    /**
     * This will return current signed in 'FireBase User
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}