package com.uzhnu.notesapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.databinding.ActivityFullscreenPhotoBinding;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;

public class FullscreenPhotoActivity extends AppCompatActivity {
    private ActivityFullscreenPhotoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String encodedImage = (String) PreferencesManager.getInstance().get(Constants.KEY_IMAGE);
        if (encodedImage == null) {
            finish();
            return;
        }
        binding.photoViewFullscreen.setImageBitmap(ImageUtil.decodeImage(encodedImage));
    }
}