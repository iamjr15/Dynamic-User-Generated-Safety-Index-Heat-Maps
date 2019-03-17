package com.gradient.mapbox.mapboxgradient.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.gradient.mapbox.mapboxgradient.Models.AreaReview;
import com.gradient.mapbox.mapboxgradient.Models.Crime;
import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.gradient.mapbox.mapboxgradient.R;
import com.gradient.mapbox.mapboxgradient.helpers.AreaReviewHelper;
import com.gradient.mapbox.mapboxgradient.helpers.ContactsHelper;
import com.gradient.mapbox.mapboxgradient.helpers.CrimeReportHelper;
import com.gradient.mapbox.mapboxgradient.helpers.VolunteersHelper;


public class HeatmapControlPanelView extends RelativeLayout implements View.OnClickListener {
    private TextView addressLabel, scoreLabel, waitingNotification;
    private LinearLayout controlsContent;
    private RelativeLayout voteButtonHolderView;

    private HeatmapControlsListener controlslistener;
    private MyFeature displayedFeature;

    private final static double VOTE_RED = -0.50;
    private final static double VOTE_YELLOW = -0.25;
    private final static double VOTE_GREEN = 0.75;
    private boolean isVotingAlowed = true;


    public HeatmapControlPanelView(Context context) {
        super(context);
        initView(context);
    }

    public HeatmapControlPanelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_heatmap_control_panel, this, true);

        // save view references
        addressLabel = view.findViewById(R.id.addressLabel);
        scoreLabel = view.findViewById(R.id.scoreLabel);
        controlsContent = view.findViewById(R.id.controlsContent);
        waitingNotification = view.findViewById(R.id.waitingNotification);
        voteButtonHolderView = view.findViewById(R.id.voteButtonHolder);


        // set view initial states
        toggleControlsVisibility(true);

        // set button listeners
        view.findViewById(R.id.buttonRed).setOnClickListener(this);
        view.findViewById(R.id.buttonGreen).setOnClickListener(this);
        view.findViewById(R.id.buttonYellow).setOnClickListener(this);
    }

    private void toggleControlsVisibility(boolean visible) {
        if (visible) {
            waitingNotification.setVisibility(GONE);
            controlsContent.setVisibility(VISIBLE);
        } else {
            waitingNotification.setVisibility(VISIBLE);
            controlsContent.setVisibility(GONE);
        }
    }

    /**
     * Displays feature data in the view
     */
    @SuppressLint("DefaultLocale")
    public void setFeature(MyFeature feature) {
        // save feature reference
        this.displayedFeature = feature;

        // Display feature data in the view
        addressLabel.setText(feature.getName());
        scoreLabel.setText(
                String.format("%s: %s/%d",
                        getResources().getString(R.string.score),
                        feature.getAvgScore(),
                        10)
        );

        // Display control views
        toggleControlsVisibility(true);
    }


    public void setVotingAllowed(boolean allowed) {
        float alpha = allowed ? 1f : 0.3f;

        isVotingAlowed = allowed;
        voteButtonHolderView.setAlpha(alpha);
    }


    /**
     * Listens for Red, Green, Blue vote button clicks
     */
    @Override
    public void onClick(View view) {
        // Check if voting enabled (it can be disabled after vote click, until
        // the callback is received from server that data was sucesfully ipdated)
        if (!isVotingAlowed) return;

        // Assign vote valute depending on the clicked button
        double vote;


        switch (view.getId()) {
            case R.id.buttonYellow: {
                vote = VOTE_YELLOW;
                doOnCrimeReported();
                break;
            }
            case R.id.buttonGreen:
                vote = VOTE_GREEN;
                break;
            case R.id.buttonRed:
                doOnCrimeReported();

                if (displayedFeature != null) {
                    LatLng latLng = new LatLng(displayedFeature.getLat(), displayedFeature.getLng());

                    //Alert All Volunteers
                    ContactsHelper.INSTANCE.sendMyLocationToContacts(latLng);
                }

                vote = VOTE_RED;
                break;
            default:
                vote = 0;
        }

        // Check if displayedFeature is set, to get rid of null exeptions
        String featureId = "";
        if (displayedFeature != null) featureId = displayedFeature.getId();

        // Send the vote to listener
        if (controlslistener != null)
            controlslistener.onNewVote(featureId, vote);
    }

    private void doOnCrimeReported() {
        if (displayedFeature != null) {
            LatLng latLng = new LatLng(displayedFeature.getLat(), displayedFeature.getLng());

            //Alert All Volunteers
            VolunteersHelper.INSTANCE.alertAllVolunteers(latLng);

            //Report Crime
            reportCrime();
        }
    }

    private void reportCrime() {

        Crime crime = new Crime();
        crime.setAddress(displayedFeature.getName());
        crime.setLat(displayedFeature.getLat());
        crime.setLng(displayedFeature.getLng());
        crime.setTime(System.currentTimeMillis());

        CrimeReportHelper.INSTANCE.reportCrime(crime);

        AreaReview review = new AreaReview();
        review.setAddress(displayedFeature.getName());
        review.setLat(displayedFeature.getLat());
        review.setLng(displayedFeature.getLng());
        review.setTime(System.currentTimeMillis());

        AreaReviewHelper.INSTANCE.saveAreaReview(review);
    }

    // Vote click listener
    public interface HeatmapControlsListener {
        void onNewVote(String featureId, double vote);
    }

    public void setControlsListener(HeatmapControlsListener listener) {
        this.controlslistener = listener;
    }
}
