package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.databinding.ActivitySplashBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!FirebaseUtil.isLoggedIn()) {
            Log.i(Constants.TAG, "User is not logged in");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        FirebaseUtil.signOut();
                        Log.w(Constants.TAG, "Task for getting user details failed");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return;
                    }
                    if (task.getResult().toObject(UserModel.class) == null) {
                        // Lack of user details in database
                        FirebaseUtil.signOut();
                        Log.i(Constants.TAG, "UserModel logged in but has not set picture or username yet");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
    }
}