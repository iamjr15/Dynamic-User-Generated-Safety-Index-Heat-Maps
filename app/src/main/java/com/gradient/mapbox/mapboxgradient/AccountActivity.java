package com.gradient.mapbox.mapboxgradient;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.gradient.mapbox.mapboxgradient.ViewModels.AccountViewModel;

public class AccountActivity extends BaseActivity  {
    private static final String TAG = AccountActivity.class.getSimpleName();

    private AccountViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // set main activity layout, consisting of a FrameLayout which will hold logi/signup/other fragments
        setContentView(R.layout.activity_account);

        // launch viewmodel
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        initObservables();
    }

    private void initObservables() {

        // Observe User. when user logged in -> redirect to Map activity
        mViewModel.getUser().observe(this, this::proceedActivityBasedOnState);

        // Toast listener. Receives messages from viewmodel on login/register events and displays toasts
        mViewModel.getToastMessage().observe(this, (msg)-> {
            if (msg != null) {
                msg.show(getApplicationContext());
            }
        });
    }

    public void proceedActivityBasedOnState(FirebaseUser user) {
        if (user != null) {
            if (Utils.isEmpty(user.getPhoneNumber())) {
                // User signed and logged in, but phone not verified.
                Log.d(TAG, "getUser().observe: (user != null), getPhoneNumber is empty");
                startFragment(new AccountPhoneAuthFragment(), R.id.contentFrame);
            } else {

                Log.d(TAG, "getUser().observe: (user != null), getPhoneNumber is NOT empty");
                startActivity(new Intent(AccountActivity.this, MapActivity.class));
                finish();
            }

        } else {
            Log.d(TAG, "getUser().observe: (user == null)");
            startFragment(new AccountStartFragment(), R.id.contentFrame);
        }
    }

    private boolean isVisibleFragment(String fragmentName) {
        Fragment myFragment = getSupportFragmentManager().findFragmentByTag(fragmentName);

        return myFragment != null && myFragment.isVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

//        mViewModel.onActivityResume();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");

        Fragment searchFragment = getSupportFragmentManager().findFragmentByTag( AccountStartFragment.class.getSimpleName() );
        if (searchFragment != null && searchFragment.isVisible()) {
            displayExitConfirmationDialog();

        } else {
            super.onBackPressed();
        }
    }

    private void displayExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_exit)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
