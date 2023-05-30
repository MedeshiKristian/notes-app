package com.uzhnu.notesapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.FullscreenPhotoActivity;
import com.uzhnu.notesapp.callbacks.SetImageFromCameraCallback;
import com.uzhnu.notesapp.callbacks.SetImageFromGalleryCallback;
import com.uzhnu.notesapp.callbacks.RequestCameraPermissionCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageUtil {
    private final ActivityResultLauncher<Intent> pickImageFromGallery;

    private final Uri cameraUri;
    private final ActivityResultLauncher<Intent> pickImageFromCamera;

    private final ActivityResultLauncher<String> requestCameraPermissions;

    FragmentActivity activity;

    public ImageUtil(@NonNull FragmentActivity activity, ImageView imageView) {
        this.activity = activity;
        Context context = activity.getApplicationContext();
        pickImageFromGallery = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new SetImageFromGalleryCallback(context, imageView)
        );
        cameraUri = getUri(context);
        pickImageFromCamera = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new SetImageFromCameraCallback(context, imageView, cameraUri)
        );
        assert cameraUri != null;
        requestCameraPermissions = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new RequestCameraPermissionCallback(this)
        );
    }

    public static Uri getUri(@NonNull Context context) {
        return FileProvider.getUriForFile(context,
                "com.uzhnu.notesapp.providers.GenericFileProvider",
                new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "image"));
    }

    public ImageUtil(@NonNull FragmentActivity activity,
                     @NonNull ActivityResultLauncher<Intent> pickImageFromGallery,
                     @NonNull ActivityResultLauncher<Intent> pickImageFromCamera,
                     @NonNull ActivityResultLauncher<String> requestCameraPermissions,
                     @NonNull Uri cameraUri) {
        this.activity = activity;
        this.pickImageFromGallery = pickImageFromGallery;
        this.pickImageFromCamera = pickImageFromCamera;
        this.requestCameraPermissions = requestCameraPermissions;
        this.cameraUri = cameraUri;
    }

    public void checkPermissionsAndLaunchCamera() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissions.launch(Manifest.permission.CAMERA);
        } else {
            launchCamera();
        }
    }

    public void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        pickImageFromCamera.launch(cameraIntent);
    }

    public void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
        pickImageFromGallery.launch(Intent.createChooser(intent, null));
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