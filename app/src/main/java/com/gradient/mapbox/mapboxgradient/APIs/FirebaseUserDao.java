package com.gradient.mapbox.mapboxgradient.APIs;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gradient.mapbox.mapboxgradient.BuildConfig;
import com.gradient.mapbox.mapboxgradient.Utils;

public class FirebaseUserDao {
    private final static String TAG = FirebaseUserDao.class.getSimpleName();

    private static FirebaseUserDao instance = null;
    private final FirebaseAuth mAuth;
    private final DatabaseReference ref;

    private final static String DIRECTORY_USERS = "users";
    private final static String DIRECTORY_MY_VOTES = "my_votes";

    private final static String ITEM_NAME = "name";
    private final static String ITEM_TMP_PHONE = "tmp_phone";

    public final static int USERSCORE_ERROR_NO_ENTRY = 1;
    private final static int USERSCORE_ERROR_CANCELED = 2;

    private FirebaseUserDao() {

        // Firebase auth reference
        mAuth = FirebaseAuth.getInstance();

        // Firebase database reference
        ref = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL);
    }

    public static FirebaseUserDao getInstance() {
        if (instance == null) instance = new FirebaseUserDao();

        return instance;
    }

    private DatabaseReference getUserDbReference() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            return null;
        } else {
            return ref.child(DIRECTORY_USERS).child(user.getUid());
        }
    }

    public void getUserScoreForFeature(String featureId, UserVotingForFeatureListener listener) {
        DatabaseReference dbRef = getUserDbReference();
        if (dbRef == null) return;

        DatabaseReference refToFeatureVotes = dbRef.child(DIRECTORY_MY_VOTES).child(featureId);
        refToFeatureVotes.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange()");
                    if (dataSnapshot.getValue() != null) {
                        Log.d(TAG, "(dataSnapshot.getValue() != null)");
                        listener.onResponse(
                                Utils.convertToDouble( dataSnapshot.getValue() ) ,
                                0
                        );

                    } else {
                        // no entry found. returning NO_ENTRY status to inform that this is the first vote of this user to this feature
                        listener.onResponse(0, USERSCORE_ERROR_NO_ENTRY);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled()");
                    listener.onResponse(0, USERSCORE_ERROR_CANCELED);
                }
            });
    }


    public interface UserVotingForFeatureListener {
        void onResponse(double score, int errorCode);
    }


    public void updateUsersScoreForFeature(String featureId, double usersNewScore, DatabaseReference.CompletionListener listener) {
        DatabaseReference dbRef = getUserDbReference();
        if (dbRef == null) return;

        DatabaseReference refToFeatureVotes = dbRef.child(DIRECTORY_MY_VOTES).child(featureId);

        refToFeatureVotes.setValue(usersNewScore, listener);
    }



    public void updateUserName(String name) {
        Log.d(TAG, "updateUserName()");

        DatabaseReference dbRef = getUserDbReference();
        if (dbRef == null) return;

        dbRef.child(ITEM_NAME).setValue(name);
    }

    public void updateUserTmpPhone(String phone) {
        Log.d(TAG, "updateUserName(). phone: " + phone);

        DatabaseReference dbRef = getUserDbReference();
        if (dbRef == null) return;

        dbRef.child(ITEM_TMP_PHONE).setValue(phone);
    }



    public void getUserTmpPhone(OnTmpPhoneReceived listener) {
        DatabaseReference dbRef = getUserDbReference();
        if (dbRef == null) return;

        dbRef.child(ITEM_TMP_PHONE).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    listener.onReceived(dataSnapshot.getValue().toString());
                } else {
                    listener.onReceived("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onReceived("");
            }
        });
    }
    public interface OnTmpPhoneReceived {
        void onReceived(String tmpPhone);
    }

}
