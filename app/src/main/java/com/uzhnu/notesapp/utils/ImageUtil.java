package com.uzhnu.notesapp.utils;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageUtil {
    @NonNull
    public static String encodeImage(@NonNull Bitmap bitmap) {
        int previewWidth = bitmap.getWidth();
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getHeight();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @NonNull
    public static Bitmap decodeImage(@NonNull String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    public static ActivityResultCallback<ActivityResult> getActivityResultCallbackForGallery(
            Context context, ImageView imageView) {
        return (ActivityResultCallback<ActivityResult>) result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = getBitmapFromUri(context, imageUri);
                        assert bitmap != null;
                        imageView.setImageBitmap(bitmap);
                        String encodedImage = ImageUtil.encodeImage(bitmap);
                        FirebaseUtil.getCurrentUserDetails().update(Constants.KEY_IMAGE, encodedImage);
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);

                    } catch (FileNotFoundException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        };
    }

    public static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri imageUri)
            throws FileNotFoundException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        return BitmapFactory.decodeStream(inputStream);
    }
}
