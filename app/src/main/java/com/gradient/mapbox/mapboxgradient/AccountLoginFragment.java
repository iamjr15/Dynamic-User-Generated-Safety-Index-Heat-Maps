package com.gradient.mapbox.mapboxgradient;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gradient.mapbox.mapboxgradient.ViewModels.AccountViewModel;

public class AccountLoginFragment extends Fragment implements View.OnClickListener {

    private AccountViewModel mViewModel;
    private TextInputEditText mEmailInputView, mPasswordInputView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Fragment layout
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // listeners and view references
        view.findViewById(R.id.submitButton).setOnClickListener(this);

        mEmailInputView = view.findViewById(R.id.emailField);
        mPasswordInputView = view.findViewById(R.id.passwordField);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Viewmodel Instance
        mViewModel = ViewModelProviders.of( getActivity() ).get(AccountViewModel.class);
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.submitButton && mViewModel != null) {
            mViewModel.logInUser(
                    mEmailInputView.getEditableText().toString(),
                    mPasswordInputView.getEditableText().toString()
            );
        }
    }
}
