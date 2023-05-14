package com.uzhnu.notesapp.utils;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.FullscreenPhotoActivity;
import com.uzhnu.notesapp.callbacks.CameraResultCallback;
import com.uzhnu.notesapp.callbacks.GalleryResultCallback;
import com.uzhnu.notesapp.callbacks.RequestCameraPermissionsCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class ImageUtil {
    private final ActivityResultLauncher<Intent> pickImageFromGallery;

    Uri cameraUri;
    private final ActivityResultLauncher<Intent> pickImageFromCamera;

    private final ActivityResultLauncher<String[]> requestCameraPermissions;

    FragmentActivity activity;

    public ImageUtil(@NonNull FragmentActivity activity, ImageView imageView) {
        this.activity = activity;
        Context context = activity.getApplicationContext();
        pickImageFromGallery = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new GalleryResultCallback(context, imageView)
        );
        // TODO Check WRITE_EXTERNAL_STORAGE permission
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        pickImageFromCamera = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new CameraResultCallback(context, imageView, cameraUri)
        );
        assert cameraUri != null;
        requestCameraPermissions = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new RequestCameraPermissionsCallback(this)
        );
    }

    public ImageUtil(@NonNull FragmentActivity activity,
                     @NonNull ActivityResultLauncher<Intent> pickImageFromGallery,
                     @NonNull ActivityResultLauncher<Intent> pickImageFromCamera,
                     @NonNull ActivityResultLauncher<String[]> requestCameraPermissions,
                     @NonNull Uri cameraUri) {
        this.activity = activity;
        this.pickImageFromGallery = pickImageFromGallery;
        this.pickImageFromCamera = pickImageFromCamera;
        this.requestCameraPermissions = requestCameraPermissions;
        this.cameraUri = cameraUri;
    }


    public static boolean isGrantedPermission(@NonNull Map<String, Boolean> permissions) {
        for (String permission : permissions.keySet()) {
            if (!Boolean.TRUE.equals(permissions.get(permission))) {
                return false;
            }
        }
        return true;
    }

    public void checkPermissionsAndLaunchCamera() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        String[] permissionsToRequest = Arrays.stream(permissions)
                .filter(permission -> activity.checkSelfPermission(permission)
                        != PackageManager.PERMISSION_GRANTED)
                .toArray(String[]::new);
        if (permissionsToRequest.length > 0) {
            requestCameraPermissions.launch(permissionsToRequest);
        } else {
            launchCamera();
        }
    }

    public void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        pickImageFromCamera.launch(cameraIntent);
    }

    public Uri getCameraUri() {
        return cameraUri;
    }

    public void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImageFromGallery.launch(intent);
    }

    public void launchFullScreenImage() {
        Intent intent = new Intent(activity, FullscreenPhotoActivity.class);
        activity.startActivity(intent);
    }

    @NonNull
    public static String encodeImage(@NonNull Bitmap bitmap) {
        int previewWidth = bitmap.getWidth();
        int previewHeight = bitmap.getHeight();
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

    public void showBottomSheetPickImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_pick_image);
        bottomSheetDialog.show();

        LinearLayout viewPictureLayout = bottomSheetDialog.findViewById(R.id.view_picture_linear_layout);
        LinearLayout cameraLayout = bottomSheetDialog.findViewById(R.id.camera_linear_layout);
        LinearLayout galleryLayout = bottomSheetDialog.findViewById(R.id.gallery_linear_layout);

        assert cameraLayout != null;
        cameraLayout.setOnClickListener(view -> {
            Log.i(Constants.TAG, "Camera clicked");
            checkPermissionsAndLaunchCamera();
            bottomSheetDialog.hide();
        });

        assert galleryLayout != null;
        galleryLayout.setOnClickListener(view -> {
            launchGallery();
            bottomSheetDialog.hide();
        });

        assert viewPictureLayout != null;
        viewPictureLayout.setOnClickListener(view -> {
            launchFullScreenImage();
            bottomSheetDialog.hide();
        });
    }

    public static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri imageUri)
            throws FileNotFoundException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        return BitmapFactory.decodeStream(inputStream);
    }
}
