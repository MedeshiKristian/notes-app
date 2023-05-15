package com.uzhnu.notesapp.callbacks;

import androidx.activity.result.ActivityResultCallback;
import androidx.fragment.app.FragmentActivity;

import com.uzhnu.notesapp.utils.ImageUtil;

import java.util.Map;

public class RequestCameraPermissionCallback implements ActivityResultCallback<Boolean> {
    private ImageUtil imageUtil;
    private FragmentActivity activity;

    public RequestCameraPermissionCallback(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    @Override
    public void onActivityResult(Boolean result) {
        if (result) {
            imageUtil.launchCamera();
        }
    }
}
