package com.gradient.mapbox.mapboxgradient;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize preferences
        Preferences.init(this);
    }
}
