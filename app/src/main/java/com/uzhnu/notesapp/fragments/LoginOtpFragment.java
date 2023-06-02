package com.uzhnu.notesapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.uzhnu.notesapp.activities.LoginProfileActivity;
import com.uzhnu.notesapp.databinding.FragmentLoginOtpBinding;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpFragment extends Fragment {
    private static final Long TIMEOUT_SECONDS = 30L;
    private FirebaseAuth auth;

    private Long timeoutLeftSeconds;

    private String getArgPhoneNumber;

    private FragmentLoginOtpBinding binding;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        getArgPhoneNumber = getArguments().getString(Constants.KEY_PHONE_NUMBER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Constants.TAG, getArgPhoneNumber);
        setProgress(false);
        setVariables();
//        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        if (resendToken == null) {
            sendOtp();
        }
        setListeners();
    }

    private void setVariables() {
        auth = FirebaseAuth.getInstance();

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(Constants.TAG, "onVerificationCompleted:" + credential);

                AndroidUtil.showToast(getContext(),
                        "OTP verification has been completed successfully");

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(Constants.TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.w(Constants.TAG, "Invalid request", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.w(Constants.TAG, "The SMS quota for the project has been exceeded", e);
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    Log.w(Constants.TAG, "reCAPTCHA verification attempted with null Activity", e);
                }

                setProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(Constants.TAG, "onCodeSent:" + verificationId);

                super.onCodeSent(verificationId, token);

                LoginOtpFragment.this.verificationId = verificationId;
                resendToken = token;

                AndroidUtil.showToast(getContext(), "OTP has been sent successfully");

                startResendTimer();
                setProgress(false);
            }
        };
    }

    private void setListeners() {
        binding.buttonVerifyOtpCode.setOnClickListener(view1 -> {
            String code = Objects.requireNonNull(binding.editTextOtpCode.getText()).toString();

            if (code.length() != 6) {
                binding.editTextOtpCode.setError("Enter the 6-digit verification code");
            } else {
                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(verificationId, code);

                signInWithPhoneAuthCredential(credential);
            }
        });

        binding.textViewResendOtp.setOnClickListener(view1 -> sendOtp());
    }

    private void setProgress(boolean show) {
        if (show) {
            binding.buttonVerifyOtpCode.setEnabled(false);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.buttonVerifyOtpCode.setEnabled(true);
        }
    }

    private void sendOtp() {
        setProgress(true);

        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(getArgPhoneNumber)
                        .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(callbacks);

        if (resendToken != null) {
            builder.setForceResendingToken(resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private void startResendTimer() {
        try {
            binding.textViewResendOtp.setEnabled(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        timeoutLeftSeconds = TIMEOUT_SECONDS;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutLeftSeconds -= 1;
                try {
                    getActivity().runOnUiThread(() -> {
                        try {
                            binding.textViewResendOtp
                                    .setText("Resent otp code in " + timeoutLeftSeconds + " seconds");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (timeoutLeftSeconds == 0) {
                    timer.cancel();
                    try {
                        getActivity().runOnUiThread(() -> {
                            try {
                                binding.textViewResendOtp.setEnabled(true);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 1000);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        setProgress(true);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    setProgress(false);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constants.TAG, "signInWithCredential:success");

                        AndroidUtil.showToast(getContext(),
                                "OTP verification has been completed successfully");

                        navigateToNext();
                    } else {
                        Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof
                                FirebaseAuthInvalidCredentialsException) {
                            AndroidUtil.showToast(getContext(),
                                    "The verification code entered was invalid");
                        }
                    }
                });
    }

    private void navigateToNext() {
        setProgress(false);
        Intent intent = new Intent(requireActivity(), LoginProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.KEY_PHONE_NUMBER, getArgPhoneNumber);
//        NavHostFragment.findNavController(LoginOtpFragment.this)
//                .navigate(R.id.action_loginOtpFragment_to_loginUsernameFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}