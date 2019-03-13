package com.gradient.mapbox.mapboxgradient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.gradient.mapbox.mapboxgradient.ViewModels.HeatmapViewModel;
import com.gradient.mapbox.mapboxgradient.Views.HeatmapControlPanelView;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

public class MapActivity extends BaseActivity implements MapboxMap.OnCameraMoveListener, OnMapReadyCallback, HeatmapControlPanelView.HeatmapControlsListener, View.OnClickListener {
    private static final String TAG = MapActivity.class.getSimpleName();

    private static final String HEATMAP_SOURCE_ID = "heatmap-source";
    private static final String HEATMAP_LAYER_ID = "heatmap_layer";
    private static final String CIRCLE_LAYER_ID = "click-circles";
    private final static double CENTERME_ZOOM = 13;

    // Mapbox
    private MapView mapView;
    private MapboxMap mapboxMap;

    // Viewmodel
    private HeatmapViewModel mViewModel;

    // Views
    private TextView zoomLabelView;
    private HeatmapControlPanelView heatmapPanelView;
    private FloatingActionButton centerMeFABView;
    private boolean isFirstDataLoad = true;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map);

        // View references
        heatmapPanelView = findViewById(R.id.heatmapPanel);
        zoomLabelView = findViewById(R.id.zoomLabelView);
        centerMeFABView = findViewById(R.id.centerMe);
        centerMeFABView.setVisibility(View.INVISIBLE);

        // click listeners
        findViewById(R.id.logoutButton).setOnClickListener(this);
        findViewById(R.id.fittoScreen).setOnClickListener(this);
        centerMeFABView.setOnClickListener(this);
        heatmapPanelView.setControlsListener(this);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Viewmodel instance
        mViewModel = ViewModelProviders.of( this).get(HeatmapViewModel.class);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Log.d(TAG, "onMapReady()");

        MapActivity.this.mapboxMap = mapboxMap;

        // register GPS listener
        startLocationUpdates();

        // get Zoom level displayed
        initDebugPanel();

        // Viewmodel data observables
        registerViewModelObservables();

        // Add data to map
        initiateHeatmapData();
    }

    private void initiateHeatmapData() {
        // Create empty source, which will later be updated
        mapboxMap.addSource(new GeoJsonSource(HEATMAP_SOURCE_ID));

        // Add heatmap and click listenerlayers
        addHeatmapLayer();
        addHeatmapClickListener();

        mViewModel.getFeatures().observe(this, features -> {
            if (features == null) return;

            // Convert MyFeature to Mapbox geojsonsource
            FeatureCollection geoSource = MyFeature.myFeaturesToFeatureCollection(features);

            // Update map source data
            GeoJsonSource source = (GeoJsonSource) mapboxMap.getSource(HEATMAP_SOURCE_ID);
            if (source != null) {
                source.setGeoJson(geoSource);
            }

            // fit map to show all features (for the first time only)
            if (isFirstDataLoad) {
                fitlocationsToScreen( MyFeature.featuresToLocations(features));
                isFirstDataLoad = false;
            }
        });
    }




    @SuppressLint("RestrictedApi")
    private void registerViewModelObservables() {

        // Feature that must be displayed in control panel
        mViewModel.getDisplayedFeature().observe(this, (displayedFeature) -> {
            if (displayedFeature != null) {
                heatmapPanelView.setFeature(displayedFeature);
            }
        });


        // Toast listener. Receives messages from viewmodel on login/register events and displays toasts
        mViewModel.getToastMessage().observe(this, (msg)-> {
            if (msg != null) {
                msg.show(getApplicationContext());
            }
        });

        // Toggle voting. It is disable while processing API requests
        mViewModel.getIsVotingAllowed().observe(this, allowed -> heatmapPanelView.setVotingAllowed(allowed));
    }


    @Override
    public void onNewVote(String featureId, double vote) {
        mViewModel.onNewVote(featureId, vote);
    }


    /**
     * Adds the main layer which displays heatmaps
     */
    private void addHeatmapLayer() {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID);
        layer.setMaxZoom(30);
        layer.setProperties(
//                heatmapWeight(get("avgScore")),
                heatmapWeight(
                        interpolate(
                                linear(), get("avgScore"),
                                stop(0, 0.5),
                                stop(10, 5)
                        )
                ),
                heatmapOpacity(0.65f),
                heatmapRadius(
                        interpolate(
                            linear(), zoom(),
                            stop(5, 15),
                            stop(14.5, 80)
                        )
                ),
                heatmapIntensity(
                        interpolate(
                            linear(), zoom(),
                            stop(5, 0.5),
                            stop(14.5, 1)
                        )
                )
        );
        mapboxMap.addLayer(layer);
    }


    /**
     * HeatmapLayer can't be directly listened for a click, so we add CircleLayer sonsisting
     * same features and with circles of same size as heatmaps
     */
    private void addHeatmapClickListener() {
        Log.d(TAG, "addHeatmapClickListener()");

        // Creating mock transparent circles of same size as heatmaps
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, HEATMAP_SOURCE_ID);
        circleLayer.setProperties(
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(5, 15),
                                stop(14.5, 80)
                        )
                ),
                circleOpacity(0f)
        );
        mapboxMap.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);

        // Registering map click listener
        mapboxMap.addOnMapClickListener(point -> mViewModel.onMapClick(mapboxMap, point, CIRCLE_LAYER_ID));
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    /**
     * Posts camera move actions to onCameraMove method
     */
    private void initDebugPanel() {
        Log.d(TAG, "setupDebugPanel()");
        mapboxMap.addOnCameraMoveListener(MapActivity.this);
    }
    @Override
    public void onCameraMove() {
        double zoomLvl = Math.round(mapboxMap.getCameraPosition().zoom * 10.0) / 10.0;
        this.zoomLabelView.setText("zoom: " + zoomLvl);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        Log.d(TAG, "onLocationChanged(): " + location.toString());

        // Post new location to viewmodel
        if (mViewModel != null)
            mViewModel.onLocationChanged(location);

        // Turn onCenter me button
        centerMeFABView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.logoutButton:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                startActivity(new Intent(this, AccountActivity.class));
                break;

            case R.id.fittoScreen:
                List<MyFeature> features = mViewModel.getFeatures().getValue();
                fitlocationsToScreen(
                        MyFeature.featuresToLocations(features)
                );

                break;

            case R.id.centerMe:
                centerMe();
                break;
        }
    }

    /**
     * Moves map camera to user location
     */
    public void centerMe() {
        if (mViewModel != null && mViewModel.getUserFeature().getValue() == null) return;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                mViewModel.getUserFeature().getValue().getLatLng(),
                CENTERME_ZOOM
        );
        mapboxMap.easeCamera(cameraUpdate);
    }

    /**
     * Reads all locations from features and fits them in to screen
     */
    private void fitlocationsToScreen(List<LatLng> locations) {
        // Create bounds object
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.includes(locations);

        // Fit to screen
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        displaylogoutOnExitDialog();
    }

    private void displaylogoutOnExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.logout_on_exit)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    super.onBackPressed();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}