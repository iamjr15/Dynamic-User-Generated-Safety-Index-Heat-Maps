package com.gradient.mapbox.mapboxgradient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gradient.mapbox.mapboxgradient.ViewModels.AccountViewModel;

public class AccountPhoneAuthFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = AccountPhoneAuthFragment.class.getSimpleName();

    private AccountViewModel mViewModel;
    private TextInputEditText mCodeInputView, mPhoneInputView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // Get Viewmodel Instance
        mViewModel = ViewModelProviders.of( getActivity() ).get(AccountViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        // Fragment layout
        View view = inflater.inflate(R.layout.fragment_confirm_phone, container, false);

        // listeners and view references
        view.findViewById(R.id.submitButton).setOnClickListener(this);
        view.findViewById(R.id.resendVeritifation).setOnClickListener(this);

        mPhoneInputView = view.findViewById(R.id.phoneInput);
        mCodeInputView = view.findViewById(R.id.codeInput);

        // Initiate phone authentication by sending auth code via sms to provided phone number
        mViewModel.initiatePhoneAuthentication(getActivity());

        // Observe sms code, which can be automatically obtained by Firebase SDK in some devices
        mViewModel.getSmsCode().observe(getActivity(), code -> mCodeInputView.setText(code));

//        Observe temporary phone number and put the value into UI input field
        mViewModel.getTmpPhone().observe(getActivity(), phone -> mPhoneInputView.setText(phone));

        return view;
    }




    @Override
    public void onClick(View view) {
        if (mViewModel == null) return;

        switch (view.getId()) {
            case R.id.submitButton:
                mViewModel.applyPhoneAuthCode(
                        mCodeInputView.getEditableText().toString()
                );
                break;

            case R.id.resendVeritifation:
                mViewModel.resendPhoneVerification(
                        getActivity(),
                        mPhoneInputView.getEditableText().toString()
                );
                break;
        }
    }
}
