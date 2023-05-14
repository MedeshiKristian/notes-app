package com.uzhnu.notesapp.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.FullscreenPhotoActivity;
import com.uzhnu.notesapp.activities.MainActivity;
import com.uzhnu.notesapp.callbacks.CameraResultCallback;
import com.uzhnu.notesapp.callbacks.GalleryResultCallback;
import com.uzhnu.notesapp.callbacks.RequestCameraPermissionsCallback;
import com.uzhnu.notesapp.databinding.FragmentLoginUsernameBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.util.Arrays;

public class LoginUsernameFragment extends Fragment {
    private FragmentLoginUsernameBinding binding;

    private UserModel userModel;
    private String getArgPhoneNumber;
    private String encodedImage;

    private ActivityResultLauncher<Intent> pickImageFromGallery;
    private ActivityResultLauncher<Intent> pickImageFromCamera;
    private ActivityResultLauncher<String[]> requestCameraPermissions;

    private Uri cameraUri;
    private ImageUtil imageUtil;

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
        pickImageFromGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new GalleryResultCallback(getContext(), binding.imageViewUser));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        pickImageFromCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new CameraResultCallback(getContext(), binding.imageViewUser, cameraUri)
        );
        requestCameraPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new RequestCameraPermissionsCallback(imageUtil)
        );
        imageUtil = new ImageUtil(requireActivity(), pickImageFromGallery,
                pickImageFromCamera, requestCameraPermissions, cameraUri);
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

        binding.imageViewUser.setOnClickListener(view1 -> imageUtil.showBottomSheetPickImage());
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

    private void getUser() {
        setIsProgress(true);
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    setIsProgress(false);
                    if (task.isSuccessful()) {
                        userModel = task.getResult().toObject(UserModel.class);
                        if (userModel != null) {
                            Log.i(Constants.TAG,
                                    "Account with this number has already been created");
                            encodedImage = userModel.getImage();
                            binding.imageViewUser.setImageBitmap(
                                    ImageUtil.decodeImage(userModel.getImage()));
                            binding.textInputUsername.setText(userModel.getUsername());
                            PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
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

        if (userModel != null) {
            userModel.setImage(encodedImage);
            userModel.setUsername(username);
        } else {
            userModel = new UserModel(username, getArgPhoneNumber, encodedImage);
        }

        FirebaseUtil.getCurrentUserDetails().set(userModel)
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