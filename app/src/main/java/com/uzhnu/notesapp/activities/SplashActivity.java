package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.uzhnu.notesapp.databinding.ActivitySplashBinding;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.FirebaseAuthUtil;
import com.uzhnu.notesapp.utilities.FirebaseStoreUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!FirebaseAuthUtil.isLoggedIn()) {
            Log.i(Constants.TAG, "User is not logged in");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(Constants.TAG, "User is logged in");

        FirebaseStoreUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        FirebaseAuthUtil.signOut();
                        Log.w(Constants.TAG, "Task for getting user details failed");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    if (task.getResult().getString(Constants.KEY_IMAGE) == null) {
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