package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.uzhnu.notesapp.databinding.ActivityFullscreenPhotoBinding;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

public class FullscreenPhotoActivity extends AppCompatActivity {
    private ActivityFullscreenPhotoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String encodedImage = (String) PreferencesManager.getInstance().get(Constants.KEY_IMAGE);

        assert encodedImage != null;
        binding.photoViewFullscreen.setImageBitmap(ImageUtil.decodeImage(encodedImage));
    }
}