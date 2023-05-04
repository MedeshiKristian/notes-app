package com.uzhnu.notesapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.FragmentLoginPhoneNumberBinding;

public class LoginPhoneNumberFragment extends Fragment {
    private FragmentLoginPhoneNumberBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginPhoneNumberBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setIsProgress(false);

        binding.countryCodePicker.registerCarrierNumberEditText(binding.editTextPhoneNumber);

        setListeners();
    }

    private void setListeners() {
        binding.sentOtpButton.setOnClickListener(view1 -> {
            if (!binding.countryCodePicker.isValidFullNumber()) {
                binding.editTextPhoneNumber.setError("Phone number is not valid");
            } else {
                setIsProgress(true);
                new Handler().postDelayed(this::navigateToNextFragment, 5000);
            }
        });
    }

    private void setIsProgress(boolean show) {
        if (show) {
            binding.sentOtpButton.setEnabled(false);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.sentOtpButton.setEnabled(true);
            binding.sentOtpButton.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToNextFragment() {
        setIsProgress(false);

        Bundle bundle = new Bundle();
        bundle.putString(LoginOtpFragment.ARG_PHONE_NUMBER,
                binding.countryCodePicker.getFullNumberWithPlus());
        NavHostFragment.findNavController(LoginPhoneNumberFragment.this)
                .navigate(R.id.action_loginPhoneNumberFragment_to_loginOtpFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}