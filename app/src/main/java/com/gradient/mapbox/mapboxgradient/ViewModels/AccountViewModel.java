package com.gradient.mapbox.mapboxgradient.ViewModels;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gradient.mapbox.mapboxgradient.BuildConfig;
import com.gradient.mapbox.mapboxgradient.Models.Msg;
import com.gradient.mapbox.mapboxgradient.R;
import com.gradient.mapbox.mapboxgradient.APIs.FirebaseUserDao;
import com.gradient.mapbox.mapboxgradient.SingleLiveEvent;
import com.gradient.mapbox.mapboxgradient.Utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class AccountViewModel extends ViewModel {
    private static final String TAG = AccountViewModel.class.getSimpleName();

    private final FirebaseAuth mAuth;
    private final DatabaseReference ref;

    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    private SingleLiveEvent<Msg> toast = new SingleLiveEvent<>();
    private String mVerificationId;
    private MutableLiveData<String> smsCode = new MutableLiveData<>();

    private MutableLiveData<String> tmpPhoneNumber = new MutableLiveData<>();

    public AccountViewModel() {
        Log.d(TAG, "AccountViewModel()");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Firebase database reference
        ref = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_URL);

        // assign current user to User observable
        user.setValue(mAuth.getCurrentUser());
    }


    public LiveData<FirebaseUser> getUser() { return user; }

    public LiveData<Msg> getToastMessage() { return toast; }

    public LiveData<String> getSmsCode() { return smsCode; }

    public LiveData<String> getTmpPhone() { return tmpPhoneNumber; }

    /**
     * Firebase user creation
     */
    public void registerUser(String name, String email, String password, String phone) {
        Log.d(TAG, "createNewUser()");

        // Validating form data. Email format is not validated as Firebase does it by its own
        if (email.isEmpty()) { toast.setValue( new Msg(R.string.form_enter_email) ); return;}
        else if (password.isEmpty()) { toast.setValue( new Msg(R.string.form_enter_password) ); return;}
        else if (phone.isEmpty()) { toast.setValue( new Msg(R.string.form_enter_phone) ); return;}
        else if (!Utils.isPhoneValid(phone)){ toast.setValue( new Msg(R.string.error_bad_phone_format) ); return; }


        // Create Firebase user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail: success");

                        // Registration success, update "user" observable, which should be tracked in the AccountActivity
                        user.setValue( mAuth.getCurrentUser() );

                        // store users extra data
                        FirebaseUserDao.getInstance().updateUserName(name);
                        FirebaseUserDao.getInstance().updateUserTmpPhone(phone);

                    } else {
                        Log.d(TAG, "createUserWithEmail: fail: " + task.getException().getMessage());
                        task.getException().printStackTrace();

                        // If sign in fails, post a toast message
                        toast.setValue( new Msg(R.string.registration_failed, task.getException().getMessage()) );
                    }
                });
    }


    /**
     * Firebase user log in
     */
    public void logInUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            toast.setValue( new Msg(R.string.login_failed_empty_fields, null) );
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update "user" observable, which should be tracked in the AccountActivity
                        user.setValue( mAuth.getCurrentUser() );

                        // If sign in fails, post a toast message
                        toast.setValue( new Msg(R.string.login_success, null) );

                    } else {
                        // If sign in fails, post a toast message
                        toast.setValue( new Msg(R.string.login_failed, Objects.requireNonNull(task.getException()).getMessage()) );
                    }
                });
    }


    /**
     * Methid is called after user enters auth code
     */
    public void applyPhoneAuthCode(String code) {
        Log.d(TAG, "applyPhoneAuthCode()");

        // Check if VerificationID exists
        if (mVerificationId == null) {
            toast.setValue(new Msg(R.string.error));

            Log.e(TAG, "applyPhoneAuthCode() initiated without setting mVerificationId");
            return;
        }

        // Manually create PhoneAuthCredential object
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        // Process authentication with phone
        signInWithPhoneAuthCredential(credential);
    }

    /**
     * Initiate phone authentication (retrieve phone number from firebase and send auth SMS)
     */
    public void initiatePhoneAuthentication(Activity activity) {
        Log.d(TAG, "initiatePhoneAuthentication()");
        if (activity == null) {
            toast.setValue(new Msg(R.string.error, ""));
            return;
        }

        // Retrieve temporary phone number, which was entered in registration fragment
        FirebaseUserDao.getInstance().getUserTmpPhone(tmpPhone -> {
            Log.d(TAG, "getUserTmpPhone: " + tmpPhone);

            // Post value so it could be entered in UI's phone input
            tmpPhoneNumber.setValue(tmpPhone);

            // Send SMS
            sendSMSAuthCode(activity);
        });
    }

    private void sendSMSAuthCode(Activity activity) {
        Log.d(TAG, "sendSMSAuthCode()");

        if (Utils.isEmpty( tmpPhoneNumber.getValue() )) {
            toast.setValue(new Msg(R.string.error_phone_not_entered));
            return;
        }

        // send Firebase authentication code via SMS
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                tmpPhoneNumber.getValue(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                mSMSVerificationCallback);        // OnVerificationStateChangedCallbacks
    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mSMSVerificationCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted(): " + phoneAuthCredential);

            // post SMS code value so it could be automatically entered in SMS code input
            String code = phoneAuthCredential.getSmsCode();
            smsCode.setValue(code);

            // process authentication with phone
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d(TAG, "onVerificationFailedd()");

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                toast.setValue(new Msg(R.string.error_bad_phone_number));
            } else  {
                toast.setValue(new Msg(R.string.error));
            }

            e.printStackTrace();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            Log.d(TAG, "onCodeSent()");

            toast.setValue(new Msg(R.string.sms_send_check_code));
            mVerificationId = verificationId;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            Log.d(TAG, "onCodeAutoRetrievalTimeOut()");
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        toast.setValue(new Msg(R.string.phone_auth_success));
                        user.setValue(task.getResult().getUser());

                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            toast.setValue(new Msg(R.string.error_wrong_sms_verification_code));
                        } else {
                            toast.setValue(new Msg(R.string.error));
                        }
                    }
                });
    }

    public void resendPhoneVerification(FragmentActivity activity, String phone) {
        Log.d(TAG, "resendPhoneVerification()");

        // save phone number, which could have been updated in the input field
        tmpPhoneNumber.setValue(phone);
        FirebaseUserDao.getInstance().updateUserTmpPhone(phone);

        sendSMSAuthCode(activity);
    }

}
