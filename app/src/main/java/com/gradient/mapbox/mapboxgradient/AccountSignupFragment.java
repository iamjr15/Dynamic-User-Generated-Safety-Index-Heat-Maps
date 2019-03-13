package com.gradient.mapbox.mapboxgradient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gradient.mapbox.mapboxgradient.ViewModels.AccountViewModel;

public class AccountSignupFragment extends Fragment implements View.OnClickListener {

    private AccountViewModel mViewModel;
    private TextInputEditText mNameInputView, mEmailInputView, mPasswordInputView, mPPhoneInputView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Viewmodel Instance
        mViewModel = ViewModelProviders.of( getActivity() ).get(AccountViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Main fragment layout
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // listeners and view references
        view.findViewById(R.id.submitButton).setOnClickListener(this);

        mNameInputView = view.findViewById(R.id.nameField);
        mEmailInputView = view.findViewById(R.id.emailField);
        mPasswordInputView = view.findViewById(R.id.passwordField);
        mPPhoneInputView = view.findViewById(R.id.phoneField);


        // add Phone format mask
        mPPhoneInputView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        return view;
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.submitButton && mViewModel != null) {
            mViewModel.registerUser(
                    mNameInputView.getEditableText().toString(),
                    mEmailInputView.getEditableText().toString(),
                    mPasswordInputView.getEditableText().toString(),
                    mPPhoneInputView.getEditableText().toString()
            );
        }
    }
}
