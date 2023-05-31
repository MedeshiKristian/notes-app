package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.UserAdapter;
import com.uzhnu.notesapp.databinding.ActivityManageFolderAccessBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.FirebaseStoreUtil;

import java.util.ArrayList;
import java.util.List;

public class ManageFolderAccessActivity extends SlidrActivity {
    private ActivityManageFolderAccessBinding binding;

    private List<UserModel> userModels;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageFolderAccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        init();
        loadUsers();
    }

    private void init() {
        userModels = new ArrayList<>();
        userAdapter = new UserAdapter(userModels);
        binding.recyclerViewUsers.setAdapter(userAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUsers() {
        setProgress(true);
        FirebaseStoreUtil.getUsers().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userModels.clear();
                assert task.getResult() != null;
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    userModels.add(FirebaseStoreUtil.getUserFromDocument(queryDocumentSnapshot));
                }
                userAdapter.notifyDataSetChanged();
            } else {
                AndroidUtil.showToast(getApplicationContext(), "Failed to load users");
            }
            setProgress(false);
        });
    }

    private void setProgress(boolean show) {
        if (show) {
            binding.recyclerViewUsers.setVisibility(View.GONE);
            binding.circularProgressIndicatorUsers.setProgress(100, true);
            binding.circularProgressIndicatorUsers.show();
        } else {
            binding.circularProgressIndicatorUsers.hide();
            binding.recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }
}