package com.gradient.mapbox.mapboxgradient.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.gradient.mapbox.mapboxgradient.Models.Msg;
import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.gradient.mapbox.mapboxgradient.Models.Vote;
import com.gradient.mapbox.mapboxgradient.R;
import com.gradient.mapbox.mapboxgradient.APIs.FirebaseMapboxDao;
import com.gradient.mapbox.mapboxgradient.APIs.FirebaseUserDao;
import com.gradient.mapbox.mapboxgradient.APIs.MapboxDao;
import com.gradient.mapbox.mapboxgradient.SingleLiveEvent;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;
import java.util.Objects;

import static com.gradient.mapbox.mapboxgradient.APIs.FirebaseUserDao.USERSCORE_ERROR_NO_ENTRY;

public class HeatmapViewModel extends ViewModel {
    private static final String TAG = HeatmapViewModel.class.getSimpleName();

    /**
     * Live data variables
     */
    private MutableLiveData<MyFeature> userFeature = new MutableLiveData<>();
    private MutableLiveData<MyFeature> displayedFeature = new MutableLiveData<>(); // todo: connect this value with livedata featurelist to have it updated together with list update
    private SingleLiveEvent<Msg> toast = new SingleLiveEvent<>();
    private MutableLiveData<List<MyFeature>> features = new MutableLiveData<>();
    private MutableLiveData<Boolean> isVotingAllowed = new MutableLiveData<>();


    /**
     * Live variable getters
     */
    public MutableLiveData<MyFeature> getUserFeature() {
        return userFeature;
    }
    public MutableLiveData<MyFeature> getDisplayedFeature() {
        return displayedFeature;
    }
    public MutableLiveData<List<MyFeature>> getFeatures() { return features; }
    public LiveData<Msg> getToastMessage() { return toast; }
    public MutableLiveData<Boolean> getIsVotingAllowed() { return isVotingAllowed; }

    public HeatmapViewModel() {
        // Register heatmap Firebase data listener
        FirebaseMapboxDao.getInstance().getFeatureList(items -> features.setValue(items));

        isVotingAllowed.setValue(true);
    }


    /**
     * Receives location changes from GPS. On location change cheks if this is a new place
     * and tries to decode the new location into a place name
     * @param location - GPS location
     */
    public void onLocationChanged(Location location) {
        // DEBUGGING. Injects demo locations for testing purposes
//        location = DemoLocation.getRandLatLng();

        //todo check if distance from previous location is far enough to refresh API calls in order to minimise API calls

        // decode user location into place name and it's center location
        MapboxDao.geocodeLocation(location, (placeName, centerLocation) -> {

            // Find Feature in dataset by geocoded location. If not found - new record is created
            FirebaseMapboxDao.getInstance().getOrCreateFeature(centerLocation, placeName, feature -> {

                // If currently no feature is displayed, set userFeature also as displayedFeature
                if (displayedFeature.getValue() == null)
                    displayedFeature.setValue(feature);

                // Update users feature (if its not the same as is currently)
                if (userFeature.getValue() == null || userFeature.getValue().getId() != feature.getId()) {
                    userFeature.setValue(feature);
                }
            });
        });
    }


    /**
     * Click on heatmap circle event
     */
    public void onMapClick(MapboxMap mapboxMap, LatLng point, String layerId) {
        Log.d(TAG, "onMapClick()");

        // Find feature by Map click location
        PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> featureList = mapboxMap.queryRenderedFeatures(pointf, layerId);

        if (featureList.size() > 0) {
            // Taking first found feature as the lucky one
            String featureId = featureList.get(0).id();
            Log.d(TAG, "Feature clicked: " + featureId);

            FirebaseMapboxDao.getInstance().getFeature(featureId, feature -> displayedFeature.setValue(feature));
        }
    }




    /**
     * makes APi call to update Feature's totalScore and votes uantity
     * @param featureId - feature that is voted for
     * @param vote - the vote value
     */
    public void onNewVote(String featureId, double vote) {
        Log.d(TAG, "onNewVote(): " + vote);

        // disallow voting untill server will be updated
        isVotingAllowed.setValue(false);

        // Check if displayedFeature is set
        MyFeature dFeature = displayedFeature.getValue();
        if (dFeature == null) {
            Log.e(TAG, "onNewVote(): vote receive while displayedFeature is not set");
            return;
        }

        // featureId, passed from HeatmapControlPanelView should always be the same as displayedFeature in ViewModel. Validating just in case..
        if (!displayedFeature.getValue().getId().equals(featureId)) {
            Log.e(TAG, "onNewVote: voted featureId["+featureId+"] != displayedFeature.getId");
            return;
        }

        // Check if user has voted for this feature.
        // If so, we are just updating vote without increasing features vote quantity
        FirebaseUserDao.getInstance().getUserScoreForFeature(featureId, (oldScore, errorCode) -> {
            Log.d(TAG, "getUserScoreForFeature(): " + oldScore);

            // Calculate new score that should be applied from the user to feature. Its the same as score, received from firebase, just checking it to apply for min/max boundaries
            Double usersNewScore = Vote.calcNewUsersScore(oldScore, vote);
            Log.d(TAG, "usersNewScore: " + usersNewScore);

            // Check if it is first time user is voting for a particular feature
            boolean isUsersFirstVote = (errorCode == USERSCORE_ERROR_NO_ENTRY);

            // if users score has changed, we update users score for feature and Features general score values
            if (usersNewScore != oldScore) {

                // calculate score increment
                double scoreIncrease = usersNewScore - oldScore;

                // Update feature object
                dFeature.appendScore(scoreIncrease, isUsersFirstVote);

                // Update Feature score
                FirebaseMapboxDao.getInstance().updateFeature(dFeature);

                // update User to feature voting score. Doing it with a callback to save from double votes
                FirebaseUserDao.getInstance().updateUsersScoreForFeature(dFeature.getId(), usersNewScore, (databaseError, databaseReference) -> {
                        // Save updated feature to displayedFeature observable
                        displayedFeature.setValue(dFeature); //todo: take value from livedata list

                        // If displayedFeature (the one that just received a vote) is the same as userFeature, updating userFeature also
                        String ufid = Objects.requireNonNull(userFeature.getValue()).getId();
                        if (displayedFeature.getValue().getId().equals(ufid)) {
                            userFeature.setValue(dFeature);
                        }

                        // Turn on voting again
                        isVotingAllowed.setValue(true);
                    });

            } else {
                // no updates made as probably the score exceeded min/mxa values
                toast.setValue(new Msg(R.string.vote_not_accepted, usersNewScore.toString()));

                // Turn on voting again
                isVotingAllowed.setValue(true);
            }
        });
    }


}
