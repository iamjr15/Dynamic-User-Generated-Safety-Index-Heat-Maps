package com.gradient.mapbox.mapboxgradient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccountStartFragment extends Fragment implements View.OnClickListener {

    private BaseActivity mBaseActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // View references and listeners
        View view = inflater.inflate(R.layout.fragment_account_start, container, false);

        view.findViewById(R.id.signupButton).setOnClickListener(this);
        view.findViewById(R.id.loginButton).setOnClickListener(this);

        mBaseActivity = (BaseActivity) getActivity();

        return view;
    }

    @Override
    public void onClick(View view) {
        if (mBaseActivity == null) return;

        switch (view.getId()) {
            case R.id.loginButton: mBaseActivity.startFragment(new AccountLoginFragment(), R.id.contentFrame); break;
            case R.id.signupButton: mBaseActivity.startFragment(new AccountSignupFragment(), R.id.contentFrame); break;
        }
    }

}
