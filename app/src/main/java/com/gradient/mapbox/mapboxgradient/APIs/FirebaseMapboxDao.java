package com.gradient.mapbox.mapboxgradient.APIs;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gradient.mapbox.mapboxgradient.BuildConfig;
import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class FirebaseMapboxDao {
    private final static String TAG = FirebaseMapboxDao.class.getSimpleName();

    private static FirebaseMapboxDao instance = null;
    private final DatabaseReference ref;
    private static final String DIR_HEATMAP = "heatmap";


    private FirebaseMapboxDao() {
        // Firebase database reference
        ref = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL).child(DIR_HEATMAP);
    }

    public static FirebaseMapboxDao getInstance() {
        if (instance == null) instance = new FirebaseMapboxDao();
        return instance;
    }


    /**
     * Infinite feature list change listener. Returns list, formated as Mapbox feature
     */
    public void getFeatureList(OnFeatureListListener listener) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Read firebase data as MyFeature object list
                List<MyFeature> list = FirebaseMapboxDao.snapshotToMyFeatureList(dataSnapshot);

//                for (MyFeature item : list) {
//                    Log.d(TAG, "Feature " + item.getName() + " : " + item.getAvgScore());
//                }

                listener.onChanged(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    /**
     * Single feature listener
     */
    public void getFeature(String fId, OnFeatureReceived listener) {
        ref.child(fId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange(): ");

                if (dataSnapshot.exists()) {
                    listener.onReceived( dataSnapshot.getValue(MyFeature.class) );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    /**
     * Updates single Feature
     */
    public void updateFeature(MyFeature feature) {
        Log.d(TAG, "updateFeature()");

        // Update feature in Firebase
        ref.child( feature.getId() ).setValue( feature );
    }


    public void getOrCreateFeature(LatLng location, String geocodedName, OnFeatureReceived listener) {
        // make an ID, which is a concatenated location string
        String fid = MyFeature.locationToFirebaseEntryId(location);

        ref.child(fid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    // Returning the feature that was found on server
                    Log.d(TAG, "getOrCreateFeature: found");
                    Log.d(TAG, "getOrCreateFeature: getTotalScore " + dataSnapshot.getValue(MyFeature.class).getTotalScore());
                    Log.d(TAG, "getOrCreateFeature: name " + dataSnapshot.getValue(MyFeature.class).getName());
                    listener.onReceived( dataSnapshot.getValue(MyFeature.class) );

                } else {

                    // returns newly created feature
                    Log.d(TAG, "getOrCreateFeature: created");
                    listener.onReceived( createFeature(location, geocodedName) );
                }

                ref.child(fid).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "getOrCreateFeature: onCancelled ");
                ref.child(fid).removeEventListener(this);
            }
        });
    }

    /**
     * Makes Firebase entry from MyFeature object
     */
    private MyFeature createFeature(LatLng location, String geocodedName) {
        Log.d(TAG, "createFeature()");

        // Prepare object
        MyFeature feature =  new MyFeature(location, geocodedName);

        // Make entry to firebase DB
        ref.child( feature.getId() ).setValue(feature);

        return feature;
    }

    /**
     Callback interfaces
     */
    public interface OnFeatureReceived {
        void onReceived(MyFeature feature);
    }
    public interface OnFeatureListListener {
        void onChanged(List<MyFeature> features);
    }

    /**
     * Converter
     */
    private static List<MyFeature> snapshotToMyFeatureList(DataSnapshot dataSnapshot) {
        List<MyFeature> list = new ArrayList<>();
        for (DataSnapshot item : dataSnapshot.getChildren()) {
            list.add( item.getValue(MyFeature.class) );
        }
        return list;
    }
}
