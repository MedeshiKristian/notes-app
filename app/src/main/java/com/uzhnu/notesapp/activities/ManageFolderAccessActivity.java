package com.uzhnu.notesapp.activities;

import android.os.Bundle;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityManageFolderAccessBinding;

public class ManageFolderAccessActivity extends SlidrActivity {
    private ActivityManageFolderAccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageFolderAccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        // TODO
    }
}