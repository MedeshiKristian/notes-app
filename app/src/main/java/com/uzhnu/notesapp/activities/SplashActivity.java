package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.databinding.ActivitySplashBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!AuthUtil.isLoggedIn()) {
            Log.i(Constants.TAG, "User is not logged in");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(Constants.TAG, "User is logged in");

        StoreUtil.getCurrentUser().get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        AuthUtil.signOut();
                        Log.w(Constants.TAG, "Task for getting user details failed");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    UserModel userModel = UserModel.toUser(task.getResult());
                    if (userModel.getImage() == null) {
                        // Lack of user details in database
                        Log.i(Constants.TAG, "Lack of user details in database");
                        Intent intent = new Intent(SplashActivity.this, LoginProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    finish();

                });
    }
}