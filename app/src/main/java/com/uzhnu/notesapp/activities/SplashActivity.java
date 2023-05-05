package com.uzhnu.notesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.databinding.ActivitySplashBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseUtil.isLoggedIn()) {
            FirebaseUtil.getCurrentUserDetails().get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().toObject(UserModel.class) == null) {
                                // Lack of username or image in database
                                FirebaseUtil.signOut();
                            }
                        } else {
                            FirebaseUtil.signOut();
                            Log.w(Constants.TAG, "Task for getting user image failed");
                        }
                    });
        }

        new Handler().postDelayed(() -> {
            if (FirebaseUtil.isLoggedIn()) {
                Log.i(Constants.TAG, "User is logged in");
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Log.i(Constants.TAG, "User is not logged in");
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1000);
    }
}