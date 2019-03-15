package com.gradient.mapbox.mapboxgradient.helpers

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gradient.mapbox.mapboxgradient.BuildConfig
import com.gradient.mapbox.mapboxgradient.Models.Volunteers
import io.reactivex.Observable


object VolunteersHelper {

    private val TAG = "VolunteersHelper"

    //Firebase DB Tables
    private const val VOLUNTEERS_ROOT = "volunteers"

    val dbRef by lazy { FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL) }

    fun saveVolunteersRefInFireBaseDB(volunteers: Volunteers): Observable<Boolean> {

        return Observable.create { emitter ->

            val databaseReference = dbRef.child(VOLUNTEERS_ROOT)

            val key = databaseReference.push().key

            volunteers.id = key!!

            val values = volunteers.toMap()

            val updates: HashMap<String, Any> = hashMapOf()

            updates["/$VOLUNTEERS_ROOT/$key"] = values

            dbRef.updateChildren(updates).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(true)
                        emitter.onComplete()
                    }
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onNext(false)
                        emitter.onComplete()
                    }
                }
            }.addOnFailureListener {
                it.printStackTrace()

                if (!emitter.isDisposed) {
                    emitter.onNext(false)
                    emitter.onComplete()
                }
            }
        }
    }


    /**
     * This will return current signed in 'FireBase User
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun alertAllVolunteers(myLocation: LatLng) {

        val currentUser = getCurrentUser()
        val databaseReference = dbRef.child(VOLUNTEERS_ROOT)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {

                    data.getValue(Volunteers::class.java)?.let { dbData ->

                        val volunteerInfo = dbData

                        volunteerInfo.isNewCrimeReported = true
                        volunteerInfo.victimUserName = currentUser?.displayName!!
                        volunteerInfo.crimeLat = myLocation.latitude
                        volunteerInfo.crimeLng = myLocation.longitude

                        val values = volunteerInfo.toMap()

                        val updates: HashMap<String, Any> = hashMapOf()

                        updates["/$VOLUNTEERS_ROOT/${dbData.id}"] = values

                        dbRef.updateChildren(updates)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().stackTrace
            }
        })
    }

    fun deleteVolunteersInfo() {
        val databaseReference = dbRef.child(VOLUNTEERS_ROOT)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    data.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}