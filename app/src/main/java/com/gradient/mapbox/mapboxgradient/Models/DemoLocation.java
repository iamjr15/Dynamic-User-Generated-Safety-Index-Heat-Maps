package com.gradient.mapbox.mapboxgradient.Models;

import android.location.Location;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DemoLocation {

    /**
     * Static demo data list
     */
    private static List<LatLng> list = Arrays.asList(
            new LatLng(54.6357146,23.9203486), // "Prienai"
            new LatLng(43.7032932,7.1827767), // "Nica"
            new LatLng(54.6024189,23.9899354), // "Birštonas"
            new LatLng(51.5287352,-0.3817841), // "London"
            new LatLng(48.8589507,2.2770198), // "Paris"
            new LatLng(51.4686194,-2.7308018), // "Bristol"
            new LatLng(54.8282863,23.8379615), // "Garliava"
            new LatLng(54.9334198,24.0740135) // "Neveronys"
    );

    /**
     * Random place selector
     * @return
     */
    public static LatLng getRandLocation() {
        Random rand = new Random();
        return list.get( rand.nextInt(list.size()) );
    }

    public static Location getRandLatLng() {
        LatLng latLng = getRandLocation();

        Location location = new Location("Sdf");
        location.setLatitude( latLng.getLatitude());
        location.setLongitude( latLng.getLongitude());

        return location;
    }
}
