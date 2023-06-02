package com.uzhnu.notesapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.activities.MainActivity;
import com.uzhnu.notesapp.callbacks.SetImageFromCameraCallback;
import com.uzhnu.notesapp.callbacks.SetImageFromGalleryCallback;
import com.uzhnu.notesapp.callbacks.RequestCameraPermissionCallback;
import com.uzhnu.notesapp.databinding.FragmentLoginUsernameBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.FirebaseStoreUtil;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;

public class LoginUsernameFragment extends Fragment {
    private FragmentLoginUsernameBinding binding;

    private UserModel userModel;
    private String getArgPhoneNumber;
    private String encodedImage;

    private ActivityResultLauncher<Intent> pickImageFromGallery;
    private ActivityResultLauncher<Intent> pickImageFromCamera;
    private ActivityResultLauncher<String> requestCameraPermission;

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
                new SetImageFromGalleryCallback(getContext(), binding.imageViewUser));
        cameraUri = ImageUtil.getUri(requireContext());
        pickImageFromCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new SetImageFromCameraCallback(getContext(), binding.imageViewUser, cameraUri)
        );
        imageUtil = new ImageUtil(requireActivity(), pickImageFromGallery,
                pickImageFromCamera, requestCameraPermission, cameraUri);
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new RequestCameraPermissionCallback(imageUtil)
        );
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setIsProgress(false);

        getUser();

        setListeners();
    }

    private void setListeners() {
        binding.buttonSignIn.setOnClickListener(view1 -> setUser());

        binding.imageViewUser.setOnClickListener(view1 -> imageUtil.showBottomSheetPickImage());
    }

    private void setIsProgress(boolean show) {
        if (show) {
            binding.buttonSignIn.setEnabled(false);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.buttonSignIn.setEnabled(true);
        }
    }

    private void getUser() {
        setIsProgress(true);
        FirebaseStoreUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    setIsProgress(false);
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        userModel = FirebaseStoreUtil.getUserFromDocument(documentSnapshot);
                        if (userModel.getImage() != null) {
                            AndroidUtil.showToast(getContext(), "Load user details successfully");
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
                    } else {
                        AndroidUtil.showToast(getContext(), "Failed to load user details");
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

        encodedImage = (String) PreferencesManager.getInstance().get(Constants.KEY_IMAGE);

        if (encodedImage == null) {
            AndroidUtil.showToast(getContext(), "Please choose your profile image.");
            return;
        }

        setIsProgress(true);

        if (userModel.getImage() != null) {
            userModel.setImage(encodedImage);
            userModel.setUsername(username);
        } else {
            userModel = new UserModel(username, getArgPhoneNumber, encodedImage);
        }

        FirebaseStoreUtil.getCurrentUserDetails().set(userModel)
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