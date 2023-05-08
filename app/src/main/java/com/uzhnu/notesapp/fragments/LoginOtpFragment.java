package com.uzhnu.notesapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.FragmentLoginOtpBinding;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpFragment extends Fragment {
    private static final Long timeoutSeconds = 30L;

    private Long timeoutLeftSeconds;

    private String getArgPhoneNumber;

    private FragmentLoginOtpBinding binding;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

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
        setIsProgress(false);
        setVariables();
//        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        if (mResendToken == null) {
            sendOtp();
        }
        setListeners();
    }

    private void setVariables() {
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(Constants.TAG, "onVerificationCompleted:" + credential);

                AndroidUtil.showToast(getContext(),
                        "OTP verification has been completed successfully");

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(Constants.TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.w(Constants.TAG, "Invalid request", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.w(Constants.TAG, "The SMS quota for the project has been exceeded", e);
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                    Log.w(Constants.TAG, "reCAPTCHA verification attempted with null Activity", e);
                }

                // Show a message and update the UI
                setIsProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(Constants.TAG, "onCodeSent:" + verificationId);

                super.onCodeSent(verificationId, token);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                AndroidUtil.showToast(getContext(),
                        "OTP has been sent successfully");

                startResendTimer();
                setIsProgress(false);
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
                        PhoneAuthProvider.getCredential(mVerificationId, code);

                signInWithPhoneAuthCredential(credential);
            }
        });

        binding.textViewResendOtp.setOnClickListener(view1 -> sendOtp());
    }

    private void setIsProgress(boolean show) {
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
        setIsProgress(true);

        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(getArgPhoneNumber)      // Phone number to verify
                        .setTimeout(timeoutSeconds, TimeUnit.SECONDS) // Timeout and unit
                        //.setActivity(getActivity()) // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks);          // OnVerificationStateChangedCallbacks


        if (getActivity() != null) {
            builder.setActivity(requireActivity());

            if (mResendToken != null) {
                builder.setForceResendingToken(mResendToken);
            }

            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void startResendTimer() {
        binding.textViewResendOtp.setEnabled(false);
        Timer timer = new Timer();
        timeoutLeftSeconds = timeoutSeconds;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    timeoutLeftSeconds -= 1;
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            binding.textViewResendOtp
                                    .setText("Resent otp code in " + timeoutLeftSeconds + " seconds");
                        }
                    });
                }
                if (timeoutLeftSeconds == 0) {
                    timer.cancel();
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (binding != null) {
                                binding.textViewResendOtp
                                        .setEnabled(true);
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        setIsProgress(true);
        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), task -> {
                        setIsProgress(false);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Constants.TAG, "signInWithCredential:success");

                            AndroidUtil.showToast(getContext(),
                                    "OTP verification has been completed successfully");

                            // FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            navigateToNextFragment();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                AndroidUtil.showToast(getContext(),
                                        "The verification code entered was invalid");
                            }
                        }
                    });
        }
    }

    private void navigateToNextFragment() {
        setIsProgress(false);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PHONE_NUMBER, getArgPhoneNumber);
        NavHostFragment.findNavController(LoginOtpFragment.this)
                .navigate(R.id.action_loginOtpFragment_to_loginUsernameFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}