package com.uzhnu.notesapp.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.auth.User;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class FirebaseAuthUtil {
    private static final Long TIMEOUT_SECONDS = 30L;
    private Long timeoutLeftSeconds;
    private FragmentActivity activity;
    private Context context;
    private TextView textViewLeftSeconds;
    private Consumer<Boolean> setProgress;
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public FirebaseAuthUtil(FragmentActivity activity, Context context, TextView textView,
                            Consumer<Boolean> setProgress) {
        this.activity = activity;
        this.context = context;
        this.textViewLeftSeconds = textView;
        this.setProgress = setProgress;
        this.auth = FirebaseAuth.getInstance();
        this.callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(Constants.TAG, "onVerificationCompleted:" + credential);

                AndroidUtil.showToast(context,
                        "OTP verification has been completed successfully");

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(Constants.TAG, "onVerificationFailed", e);

                AndroidUtil.showToast(context,
                        "Failed to send otp code");

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.w(Constants.TAG, "Invalid request", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.w(Constants.TAG, "The SMS quota for the project has been exceeded", e);
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    Log.w(Constants.TAG, "reCAPTCHA verification attempted with null Activity", e);
                }

                setProgress.accept(false);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(Constants.TAG, "onCodeSent:" + verificationId);

                super.onCodeSent(verificationId, token);

                FirebaseAuthUtil.this.verificationId = verificationId;
                resendToken = token;

                AndroidUtil.showToast(context, "OTP has been sent successfully");

                startResendTimer(textViewLeftSeconds);

                setProgress.accept(false);
            }
        };
    }

    public void sendOtp(String phoneNumber) {
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(callbacks);
        if (resendToken != null) {
            builder.setForceResendingToken(resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d(Constants.TAG, "signInWithCredential:success");

                        AndroidUtil.showToast(context,
                                "OTP verification has been completed successfully");
                    } else {
                        Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof
                                FirebaseAuthInvalidCredentialsException) {
                            AndroidUtil.showToast(context,
                                    "The verification code entered was invalid");
                        }
                    }
                });
    }

    public void changePhoneNumber(String phoneNumber) {
        sendOtp(phoneNumber);
    }

    public void updatePhoneNumber(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updatePhoneNumber(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d(Constants.TAG, "signInWithCredential:success");

                        AndroidUtil.showToast(context,
                                "OTP verification has been completed successfully");
                        FirebaseStoreUtil.getCurrentUserDetails().update(Constants.KEY_PHONE_NUMBER,
                                Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber());
                    } else {
                        Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof
                                FirebaseAuthInvalidCredentialsException) {
                            AndroidUtil.showToast(context,
                                    "The verification code entered was invalid");
                        }
                    }
                });
        ;
    }

    public void startResendTimer(@NonNull TextView textView) {
        textView.setEnabled(false);
        Timer timer = new Timer();
        timeoutLeftSeconds = TIMEOUT_SECONDS;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (activity != null) {
                    timeoutLeftSeconds -= 1;
                    activity.runOnUiThread(() -> {
                        textView.setText("Resent otp code in " + timeoutLeftSeconds + " seconds");
                    });
                }
                if (timeoutLeftSeconds == 0) {
                    timer.cancel();
                    activity.runOnUiThread(() -> {
                        textView.setEnabled(true);
                    });
                }
            }
        }, 0, 1000);
    }

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static String getUserPhoneNumber() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getPhoneNumber();
    }

    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
