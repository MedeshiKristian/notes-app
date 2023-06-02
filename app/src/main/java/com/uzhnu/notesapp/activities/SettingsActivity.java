package com.uzhnu.notesapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.uzhnu.notesapp.databinding.ActivitySettingsBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.FirebaseAuthUtil;
import com.uzhnu.notesapp.utilities.FirebaseStoreUtil;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends SlidrActivity {
    private ActivitySettingsBinding binding;

    private ImageUtil imageUtil;

    private FirebaseAuthUtil authUtil;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        init();
        loadUserDetails();
        setListeners();
    }

    private void init() {
        authUtil = new FirebaseAuthUtil(SettingsActivity.this,
                getApplicationContext(), binding.textViewResendOtp, this::setProgress);

        binding.countryCodePicker.registerCarrierNumberEditText(binding.editTextPhoneNumber);

        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());

        imageUtil = new ImageUtil(SettingsActivity.this, binding.imageViewUser);

        sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean(Constants.KEY_NIGHT_THEME, false);

        if (nightMode) {
            binding.switchTheme.setChecked(true);
        }

        binding.switchTheme.setOnClickListener(view -> {
            editor = sharedPreferences.edit();
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean(Constants.KEY_NIGHT_THEME, false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean(Constants.KEY_NIGHT_THEME, true);
            }
            editor.apply();
        });

        setProgress(false);
    }


    private void setListeners() {
        binding.imageViewUser.setOnClickListener(view -> {
            imageUtil.showBottomSheetPickImage();
        });
        binding.buttonSave.setOnClickListener(view -> {
            saveUsername();
            savePhoneNumber();
        });

        binding.textViewResendOtp.setOnClickListener(view1 -> {
            authUtil.sendOtp(getPhoneNumber());
        });

        binding.buttonVerifyOtpCode.setOnClickListener(view -> {
            String code = Objects.requireNonNull(binding.editTextOtpCode.getText()).toString();
            authUtil.updatePhoneNumber(code);
        });
    }

    private String getPhoneNumber() {
        return binding.countryCodePicker.getFullNumberWithPlus();
    }

    private void saveUsername() {
        setProgress(true);
        String newUsername = Objects.requireNonNull(binding.editTextUsername.getText()).toString();
        FirebaseStoreUtil.getCurrentUserDetails().update(Constants.KEY_USERNAME, newUsername)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.editTextUsername.setText(newUsername);
                        setProgress(false);
                    }
                });
    }

    private void savePhoneNumber() {
        setProgress(true);
        String newPhoneNumber = getPhoneNumber();
        if (newPhoneNumber.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber())) {
            return;
        }
        binding.editTextOtpCode.setVisibility(View.VISIBLE);
        binding.buttonVerifyOtpCode.setVisibility(View.VISIBLE);
        authUtil.changePhoneNumber(newPhoneNumber);
        binding.textViewResendOtp.setVisibility(View.VISIBLE);
        binding.editTextOtpCode.setVisibility(View.VISIBLE);
        binding.buttonVerifyOtpCode.setVisibility(View.VISIBLE);
    }


    private void loadUserDetails() {
        FirebaseStoreUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.editTextUsername.setText(userModel.getUsername());
                        String phoneNumber = PhoneNumberUtils.formatNumber(
                                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber(),
                                Locale.getDefault().getCountry()
                        );
                        phoneNumber = phoneNumber.substring(phoneNumber.indexOf(" ") + 1);
                        binding.editTextPhoneNumber.setText(phoneNumber);
                    } else {
                        Log.e(Constants.TAG, "Task for getting user image failed");
                    }
                });
    }

    private void setProgress(boolean show) {
        if (binding == null) return;
        binding.imageViewUser.setEnabled(!show);
        binding.editTextUsername.setEnabled(!show);
        binding.editTextPhoneNumber.setEnabled(!show);
        binding.editTextOtpCode.setEnabled(!show);
        binding.buttonSave.setEnabled(!show);
        binding.buttonVerifyOtpCode.setEnabled(!show);
        if (show) {
            binding.layoutOtpCode.setVisibility(View.GONE);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.layoutOtpCode.setVisibility(View.VISIBLE);
        }
    }
}