package com.uzhnu.notesapp.callbacks;

import androidx.activity.result.ActivityResultCallback;

import com.uzhnu.notesapp.utils.ImageUtil;

import java.util.Map;

public class RequestCameraPermissionsCallback implements ActivityResultCallback<Map<String, Boolean>> {
    private ImageUtil imageUtil;

    public RequestCameraPermissionsCallback(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    @Override
    public void onActivityResult(Map<String, Boolean> result) {
        if (ImageUtil.isGrantedPermission(result)) {
            imageUtil.launchCamera();
        }
    }
}
