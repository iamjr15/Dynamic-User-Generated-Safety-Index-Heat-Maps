package com.gradient.mapbox.mapboxgradient.APIs;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gradient.mapbox.mapboxgradient.Utils;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.geocoding.v5.models.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapboxDao {
    private static final String TAG = MapboxDao.class.getSimpleName();

    /**
     * Makes API call to find human readable place name from GPS location
     * @param location - location that is searched for
     * @param listener - returns human readable place as Feature object
     */
    public static void geocodeLocation(Location location, OnLocationGeocodedListener listener) {
        try {
            Position position = Position.fromCoordinates(location.getLongitude(), location.getLatitude());

            MapboxGeocoding.Builder clientBuilder = new MapboxGeocoding.Builder()
                    .setAccessToken(Mapbox.getAccessToken())
                    .setGeocodingTypes(new String[] {
                            GeocodingCriteria.TYPE_POI,
                            GeocodingCriteria.TYPE_ADDRESS,
                            GeocodingCriteria.TYPE_PLACE
                    })
                    .setProximity(position)
                    .setCoordinates(position);

            clientBuilder.build().enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {
                    if (response.body() == null) return;

                    List<CarmenFeature> results = response.body().getFeatures();
                    if (results.size() > 0) {

                        // Updating observable variable
                        listener.onFeatureReceived(
                                results.get(0).getPlaceName(),
                                Utils.positionToLatLng( results.get(0).asPosition() )
                        );
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable throwable) {
                    Log.e(TAG, "Geocoding Failure: " + throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Log.e(TAG, "Error geocoding: " + servicesException.toString());
            servicesException.printStackTrace();
        }
    }
    public interface OnLocationGeocodedListener {
        void onFeatureReceived(String placeName, LatLng centerLocation);
    }
}
