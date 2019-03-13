package com.gradient.mapbox.mapboxgradient.Models;

import android.content.Context;
import android.widget.Toast;

public class Msg {
    private int stringId = 0;
    private String extra;

    public Msg(int stringId, String extraMessage) {
        this.stringId = stringId;

        this.extra = extraMessage == null ? "" : extraMessage;
    }

    public Msg(int stringId) {
        this.stringId = stringId;

        this.extra = "";
    }

    public void show(Context context) {

        String msg = "";

        // Concatenate the message
        if (stringId > 0) msg += context.getString(stringId);
        if (stringId > 0 && extra != "") msg += ": ";
        if (stringId > 0) msg += extra;

        // display toast
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
