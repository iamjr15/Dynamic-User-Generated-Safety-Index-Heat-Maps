package com.gradient.mapbox.mapboxgradient.Models;

public class Vote {
    double vote;

    public static final double SCORE_MIN = 0;
    public static final double SCORE_MAX = 10;

    /**
     * Calculates new user score for a specific feature. The score has min and max values wich has to be taken into account
     * @param currentScore
     * @param newVote - vote, made by clicking on color buttons
     * @return - new score that should be applied from the user to feature
     */
    public static double calcNewUsersScore(double currentScore, double newVote) {

        double newScore = currentScore + newVote;

        if (newScore < SCORE_MIN) newScore = SCORE_MIN;
        if (newScore > SCORE_MAX) newScore = SCORE_MAX;

        return newScore;
    }
}
