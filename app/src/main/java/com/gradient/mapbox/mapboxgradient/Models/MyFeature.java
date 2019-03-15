package com.gradient.mapbox.mapboxgradient.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class MyFeature {
    private final static String TAG = MyFeature.class.getSimpleName();

    private String id;
    private Double lat, lng;
    private String name;
    private int votes = 0;
    private double totalScore;

    /**
     * Normal constructor
     */
    public MyFeature(LatLng location, String geocodedName) {
        this.lat = location.latitude;
        this.lng = location.longitude;
        this.name = geocodedName;
        this.totalScore = 5.0f; //Set Initial score

        // Generate ID
        this.id = MyFeature.locationToFirebaseEntryId(location);
    }

    /**
     * Empty constructor, used when converting object to JSON
     */
    public MyFeature() {}




    /**
     * Getters, used in object -> JSON conversion
     * @return
     */
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public int getVotes() {
        return votes;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getAvgScore() {
        if (this.votes <= 0) return 0;

        double score;
        score = (this.totalScore / (double) this.votes);
        score = Math.round(score * 100) / 100d;

        return score;
    }

    /**
     * Excluded to be stripped when writing to firebase
     */
    @Exclude
    public LatLng getLatLng() {
        return new LatLng( this.lat, this.lng);
    }


    public void setName(String text) {
        this.name = text;
    }






    /**
     * Appends votes and totalScore variables, which are used to calculate average score
     * @param scoreIncrease - score increase (should be -0.25, 0.25 or 0.75)
     * @param isNewVote - defines if this user is voting for this feature for the first time. If its not first time, we are not increasing "votes" value
     */
    public void appendScore(double scoreIncrease, boolean isNewVote) {
        // update number of total votes
        this.votes += isNewVote ? 1 : 0;

        // update total sum of vote values
        this.totalScore += scoreIncrease;
    }

    /**
     * Converts MyFeature instance to Mapbox feature
     * @return
     */
    private Feature toMapboxFeature() {
        // Prepare Geometry object
        Point geometry = Point.fromLngLat(this.getLng(), this.getLat());

        // Prepare Properties object
        JsonObject properties = new JsonObject();
        properties.addProperty("avgScore", getAvgScore());
        properties.addProperty("name", getName());
        properties.addProperty("totalScore", getTotalScore());
        properties.addProperty("votes", getVotes());

        return Feature.fromGeometry(geometry, properties, getId());
    }


    /**
     * Static converters
     * @param loc
     * @return
     */
    public static String locationToFirebaseEntryId(LatLng loc) {
        String id = loc.latitude + "@" + loc.longitude;

        return id.replaceAll("\\.", "-");
    }

    public static FeatureCollection myFeaturesToFeatureCollection(List<MyFeature> myList) {

        // Create mapbox Feature list
        List<Feature> featureList = new ArrayList<>();
        for (MyFeature myFeature : myList) {
            featureList.add( myFeature.toMapboxFeature());
        }

        // Create Mapbox Feature collection
        return FeatureCollection.fromFeatures(featureList);
    }

    public static List<LatLng> featuresToLocations(List<MyFeature> features) {
        List<LatLng> newList = new ArrayList<>();

        if (features == null) return newList;

        for (MyFeature item : features) {
            newList.add( item.getLatLng() );
        }

        return newList;
    }


}
