package com.uzhnu.notesapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.FullscreenPhotoActivity;
import com.uzhnu.notesapp.activities.MainActivity;
import com.uzhnu.notesapp.databinding.FragmentLoginUsernameBinding;
import com.uzhnu.notesapp.models.User;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class LoginUsernameFragment extends Fragment {
    private FragmentLoginUsernameBinding binding;

    private User user;
    private String getArgPhoneNumber;
    private String encodedImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        getArgPhoneNumber = getArguments().getString(Constants.KEY_PHONE_NUMBER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginUsernameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setIsProgress(false);

        getUser();

        setListeners();
    }

    private void setListeners() {
        binding.buttonLetMeIn.setOnClickListener(view1 -> setUser());

        binding.imageViewUser.setOnClickListener(view1 -> showBottomSheetPickImage());
    }

    private void setIsProgress(boolean show) {
        if (show) {
            binding.buttonLetMeIn.setEnabled(false);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.buttonLetMeIn.setEnabled(true);
        }
    }

    private void showBottomSheetPickImage() {
        if (getContext() != null) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_pick_image);

            LinearLayout viewPictureLayout = bottomSheetDialog.findViewById(R.id.view_picture_linear_layout);
            LinearLayout cameraLayout = bottomSheetDialog.findViewById(R.id.camera_linear_layout);
            LinearLayout galleryLayout = bottomSheetDialog.findViewById(R.id.gallery_linear_layout);

            bottomSheetDialog.show();

            assert cameraLayout != null;
            cameraLayout.setOnClickListener(view -> {
                // TODO NullPointerException
                if (getContext().checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    pickImageFromCamera.launch(intent);
                } else {
                    requestCameraPermission.launch(Manifest.permission.CAMERA);
                }
                bottomSheetDialog.hide();
            });


            assert galleryLayout != null;
            galleryLayout.setOnClickListener(view -> {
                Intent intent =
                        new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImageFromGallery.launch(intent);
                bottomSheetDialog.hide();
            });

            assert viewPictureLayout != null;
            viewPictureLayout.setOnClickListener(view -> {
                if (encodedImage != null) {
                    Intent intent = new Intent(getContext(), FullscreenPhotoActivity.class);
                    PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);
                    startActivity(intent);
                } else {
                    AndroidUtil.showToast(getContext(), "Please choose an image");
                }
            });
        }
    }

    private final ActivityResultLauncher<Intent> pickImageFromCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        binding.imageViewUser.setImageBitmap(bitmap);
                        encodedImage = ImageUtil.encodeImage(bitmap);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    pickImageFromCamera.launch(intent);
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        // TODO NullPointerException
                        try {
                            InputStream inputStream =
                                    getContext().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                binding.imageViewUser.setImageBitmap(bitmap);
                                encodedImage = ImageUtil.encodeImage(bitmap);
                            } else {
                                Toast.makeText(getContext(),
                                        "Please choose a valid image",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (FileNotFoundException exception) {
                            exception.printStackTrace();
                            Toast.makeText(getContext(),
                                    "Please choose a valid file", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void getUser() {
        setIsProgress(true);
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    setIsProgress(false);
                    if (task.isSuccessful()) {
                        user = task.getResult().toObject(User.class);
                        if (user != null) {
                            Log.i(Constants.TAG,
                                    "Account with this number has already been created");
                            encodedImage = user.getImage();
                            binding.imageViewUser.setImageBitmap(
                                    ImageUtil.decodeImage(user.getImage()));
                            binding.textInputUsername.setText(user.getUsername());
                        } else {
                            Log.i(Constants.TAG,
                                    "Account with this number has not been created yet");
                        }
                    }
                });
    }

    private void setUser() {
        // TODO NullPointerException
        String username = binding.textInputUsername.getText().toString();
        if (username.length() < 3) {
            binding.textInputUsername.setError("Username must be at least 3 characters.");
            return;
        }

        if (encodedImage == null) {
            AndroidUtil.showToast(getContext(), "Please choose your profile image.");
            return;
        }

        setIsProgress(true);

        if (user != null) {
            user.setImage(encodedImage);
            user.setUsername(username);
        } else {
            user = new User(username, getArgPhoneNumber, encodedImage);
        }

        Log.d(Constants.TAG, "Username: " + user.getUsername());
        Log.d(Constants.TAG, "Phone number: " + user.getPhoneNumber());
        Log.d(Constants.TAG, "Image: " + user.getImage());

        FirebaseUtil.getCurrentUserDetails().set(user)
                .addOnCompleteListener(task -> {
                    setIsProgress(false);
                    if (task.isSuccessful()) {
                        startMainActivity();
                    } else {
                        AndroidUtil.showToast(getContext(), "Authentication failed");
                    }
                });
    }

    public void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}