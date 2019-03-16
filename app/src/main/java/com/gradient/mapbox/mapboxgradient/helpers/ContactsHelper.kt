package com.gradient.mapbox.mapboxgradient.helpers

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gradient.mapbox.mapboxgradient.BuildConfig
import com.gradient.mapbox.mapboxgradient.Models.Contacts
import io.reactivex.Observable


object ContactsHelper {

    private val TAG = "ContactsHelper"

    //Firebase DB Tables
    private const val CONTACTS_ROOT = "contacts"

    val dbRef by lazy { FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL) }

    fun saveContactsRefInFireBaseDB(contacts: Contacts): Observable<Boolean> {

        return Observable.create { emitter ->

            val databaseReference = dbRef.child(CONTACTS_ROOT)

            val key = databaseReference.push().key

            contacts.id = key!!

            val values = contacts.toMap()

            val updates: HashMap<String, Any> = hashMapOf()

            updates["/$CONTACTS_ROOT/$key"] = values

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

    fun sendMyLocationToContacts(myLocation: LatLng) {

        val currentUser = getCurrentUser()
        val databaseReference = dbRef.child(CONTACTS_ROOT)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {

                    data.getValue(Contacts::class.java)?.let { dbData ->

                        val info = dbData

                        info.isNewCrimeReported = true
                        currentUser?.displayName?.let {
                            info.victimUserName = it
                        }
                        info.crimeLat = myLocation.latitude
                        info.crimeLng = myLocation.longitude

                        val values = info.toMap()

                        val updates: HashMap<String, Any> = hashMapOf()

                        updates["/$CONTACTS_ROOT/${dbData.id}"] = values

                        dbRef.updateChildren(updates)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().stackTrace
            }
        })
    }

    fun deleteContactsInfo() {
        val databaseReference = dbRef.child(CONTACTS_ROOT)
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