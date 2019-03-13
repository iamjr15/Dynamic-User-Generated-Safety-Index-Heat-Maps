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

import com.gradient.mapbox.mapboxgradient.Models.MyFeature;
import com.gradient.mapbox.mapboxgradient.R;


public class HeatmapControlPanelView extends RelativeLayout implements View.OnClickListener {
    private TextView addressLabel, scoreLabel, waitingNotification;
    private LinearLayout controlsContent;
    private RelativeLayout voteButtonHolderView;

    private HeatmapControlsListener controlslistener;
    private MyFeature displayedFeature;

    private final static double VOTE_RED = -0.25;
    private final static double VOTE_BLUE = 0.25;
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
        toggleControlsVisibility(false);

        // set button listeners
        view.findViewById(R.id.buttonRed).setOnClickListener(this);
        view.findViewById(R.id.buttonGreen).setOnClickListener(this);
        view.findViewById(R.id.buttonBlue).setOnClickListener(this);
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
        addressLabel.setText( feature.getName() );
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
            case R.id.buttonBlue: vote = VOTE_BLUE; break;
            case R.id.buttonGreen: vote = VOTE_GREEN; break;
            case R.id.buttonRed: vote = VOTE_RED; break;
            default: vote = 0;
        }

        // Check if displayedFeature is set, to get rid of null exeptions
        String featureId = "";
        if(displayedFeature != null) featureId = displayedFeature.getId();

        // Send the vote to listener
        if (controlslistener != null)
            controlslistener.onNewVote(featureId, vote);
    }

    // Vote click listener
    public interface HeatmapControlsListener {
        void onNewVote(String featureId, double vote);
    }
    public void setControlsListener(HeatmapControlsListener listener) {
        this.controlslistener = listener;
    }
}
