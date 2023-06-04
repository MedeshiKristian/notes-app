package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.uzhnu.notesapp.databinding.ActivityProfileBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;

public class LoginProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private String phoneNumber;

    private ImageUtil imageUtil;
    private String encodedImage;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getUser();
        setListeners();
    }

    private void init() {
        phoneNumber = AuthUtil.getUserPhoneNumber();
        imageUtil = new ImageUtil(LoginProfileActivity.this, binding.imageViewUser);
    }

    private void setListeners() {
        binding.buttonSignIn.setOnClickListener(view -> setUser());

        binding.imageViewUser.setOnClickListener(view -> imageUtil.showBottomSheetPickImage());

        binding.buttonLogOut.setOnClickListener(view -> {
            AuthUtil.signOut();
            PreferencesManager.getInstance().clear();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getUser() {
        setProgress(true);
        StoreUtil.getCurrentUser().get()
                .addOnCompleteListener(task -> {
                    setProgress(false);
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        userModel = UserModel.toUser(documentSnapshot);
                        if (userModel.getImage() != null) {
                            AndroidUtil.showToast(this, "Load user details successfully");
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
                        AndroidUtil.showToast(this, "Failed to load user details");
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
            AndroidUtil.showToast(this, "Please choose your profile image.");
            return;
        }

        setProgress(true);

        if (userModel.getImage() != null) {
            userModel.setImage(encodedImage);
            userModel.setUsername(username);
        } else {
            userModel = new UserModel(username, phoneNumber, encodedImage);
        }

        userModel.setUserId(AuthUtil.getCurrentUserId());

        StoreUtil.getCurrentUser().set(userModel)
                .addOnCompleteListener(task -> {
                    setProgress(false);
                    if (task.isSuccessful()) {
                        startMainActivity();
                    } else {
                        AndroidUtil.showToast(this, "Authentication failed");
                    }
                });
    }

    private void setProgress(boolean show) {
        if (show) {
            binding.buttonSignIn.setEnabled(false);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.buttonSignIn.setEnabled(true);
        }
    }

    public void startMainActivity() {
        Intent intent = new Intent(LoginProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}