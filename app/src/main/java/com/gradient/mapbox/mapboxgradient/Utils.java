package com.gradient.mapbox.mapboxgradient;

import android.content.Context;
import android.util.Log;

import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.models.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String readAssetFileToString(Context context, String fileName) {
        Log.d(TAG, "readAssetFileToString()");

        try {
            StringBuilder buf = new StringBuilder();
            String str = "";

            InputStream json = context.getAssets().open(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();

            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Converts any thing (string/long/int) to double.
     * Used when reading firebase data
     */
    public static double convertToDouble(Object value){
        double valueTwo = -1; // whatever to state invalid!

        if(value instanceof Long) valueTwo = ((Long) value).doubleValue();
        else if(value instanceof Double) valueTwo = ((Double) value);
        else if(value instanceof Integer) valueTwo = ((Integer) value).doubleValue();
        else valueTwo = Double.valueOf( (String) value);

        System.out.println(valueTwo);
        return valueTwo;
    }


    public static LatLng positionToLatLng(Position position) {
        return new LatLng(position.getLatitude(), position.getLongitude());
    }


    public static boolean isEmpty(String phoneNumber) {
        return phoneNumber == null || phoneNumber.equals("");
    }

    /**
     * Validates phone for basic rules, leaving more deep validation to Firebase
     */
    public static boolean isPhoneValid(String phone) {
        String pattern = "^\\+[0-9 \\-()]{5,15}";

        return phone.matches(pattern);
    }

    /**
     * Email basic validation. The email is passed to firebase which then validates the email
     */
    public static boolean isEmailValid(String email) {
        String pattern = "^([^@\\.]+[^@]*)\\@([^@\\.]+)\\.([^@]+)$";

        return email.matches(pattern);
    }

}
