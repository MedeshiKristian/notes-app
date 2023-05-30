package com.uzhnu.notesapp.callbacks;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseStoreUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.io.FileNotFoundException;

public class SetImageFromCameraCallback implements ActivityResultCallback<ActivityResult> {
    private final Context context;
    private final ImageView imageView;
    private final Uri imageUri;

    public SetImageFromCameraCallback(Context context, ImageView imageView, Uri imageUri) {
        this.context = context;
        this.imageView = imageView;
        this.imageUri = imageUri;
    }

    @Override
    public void onActivityResult(@NonNull ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
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
        }
    }
}
