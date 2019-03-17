package com.gradient.mapbox.mapboxgradient.helpers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gradient.mapbox.mapboxgradient.BuildConfig
import com.gradient.mapbox.mapboxgradient.Models.AreaReview
import io.reactivex.Observable


object AreaReviewHelper {

    private val TAG = "AreaReviewHelper"

    //Firebase DB Tables
    private const val AREA_ROOT = "area_review"

    val dbRef by lazy { FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL) }

    fun saveAreaReview(areaReview: AreaReview) {

        //If areaReview is new for area
        val databaseReference = dbRef.child(AREA_ROOT)

        val key = databaseReference.push().key
        saveReview(key, areaReview)

        /*isReviewAddedForArea(areaReview)
                .subscribeBy(
                        onNext = { reported ->
                            if (reported.id.isNotEmpty()) {
                                Log.i(TAG, "Review already added, update areaReview! ${reported.id}")
                                saveReview(reported.id, areaReview)
                            } else {

                            }
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )*/
    }

    private fun saveReview(key: String?, areaReview: AreaReview) {

        areaReview.id = key!!

        val values = areaReview.toMap()

        val updates: HashMap<String, Any> = hashMapOf()

        updates["/$AREA_ROOT/$key"] = values

        dbRef.updateChildren(updates).addOnCompleteListener {
            Log.i(TAG, "Review added!")
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun isReviewAddedForArea(crime: AreaReview): Observable<AreaReview> {

        return Observable.create { emitter ->
            val databaseReference = dbRef.child(AREA_ROOT)

            databaseReference.orderByChild("address").equalTo(crime.address).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.children.count() > 0) {
                            dataSnapshot.children.first().getValue(AreaReview::class.java)?.let { dbData ->
                                Log.i(TAG, "Review found with address: ${crime.address}")

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