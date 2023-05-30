package com.uzhnu.notesapp.callbacks;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseStoreUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.io.FileNotFoundException;

public class SetImageFromGalleryCallback implements ActivityResultCallback<ActivityResult> {
    private final Context context;
    private final ImageView imageView;

    public SetImageFromGalleryCallback(Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    public void onActivityResult(@NonNull ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            try {
                Bitmap bitmap = ImageUtil.getBitmapFromUri(context, imageUri);
                assert bitmap != null;
                imageView.setImageBitmap(bitmap);
                String encodedImage = ImageUtil.encodeImage(bitmap);
                FirebaseStoreUtil.getCurrentUserDetails().update(Constants.KEY_IMAGE, encodedImage);
                PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        } else {
            Log.e(Constants.TAG, "Result code: " + result.getResultCode());
            AndroidUtil.showToast(context, "Failed");
        }
    }
}
