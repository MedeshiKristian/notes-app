package com.uzhnu.notesapp.callbacks;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.uzhnu.notesapp.utils.ImageUtil;

public class RequestCameraPermissionCallback implements ActivityResultCallback<Boolean> {
    private final ImageUtil imageUtil;

    public RequestCameraPermissionCallback(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    @Override
    public void onActivityResult(@NonNull Boolean result) {
        if (result) {
            imageUtil.launchCamera();
        }
    }
}
