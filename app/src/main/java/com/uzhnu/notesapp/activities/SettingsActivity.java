package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivitySettingsBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    private AppCompatActivity activity;
    private SlidrInterface slidrInterface;

    private View backgroundView;

    private ImageUtil imageUtil;

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
        activity = (AppCompatActivity) PreferencesManager
                .getInstance().get(Constants.KEY_MAIN_ACTIVITY);
        if (activity != null) {
            backgroundView = activity.findViewById(R.id.coordinatorContent);
        }

        binding.countryCodePicker.registerCarrierNumberEditText(binding.editTextPhoneNumber);

        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());

        imageUtil = new ImageUtil(SettingsActivity.this, binding.imageViewUser);
    }

    private void setListeners() {
        attachSlidr();

        binding.imageViewUser.setOnClickListener(view -> {
            imageUtil.showBottomSheetPickImage();
        });
    }

    private void attachSlidr() {
        SlidrConfig config = new SlidrConfig.Builder()
                .listener(new SlidrListener() {
                    @Override
                    public void onSlideStateChanged(int state) {
                    }

                    @Override
                    public void onSlideChange(float percent) {
                        float coefficient = 0.25f;
                        float moveFactor = binding.coordinatorContent.getWidth()
                                * percent * coefficient;
                        if (backgroundView != null) {
                            backgroundView.setTranslationX(-moveFactor);
                        }
                    }

                    @Override
                    public void onSlideOpened() {
                    }

                    @Override
                    public boolean onSlideClosed() {
                        return false;
                    }
                })
                .build();

        slidrInterface = Slidr.attach(SettingsActivity.this, config);

        binding.buttonSave.setOnClickListener(view -> {
            FirebaseUtil.getCurrentUserDetails().update(Constants.KEY_USERNAME, binding.editTextUserName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(getApplicationContext(), "Changes successfully saved");
                        } else {
                            AndroidUtil.showToast(getApplicationContext(), "Failed to save changes");
                        }
                    });
        });
    }

    private void loadUserDetails() {
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.editTextUserName.setText(userModel.getUsername());
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
}