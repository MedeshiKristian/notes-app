package com.uzhnu.notesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.models.Note;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.FirebaseUtil;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        setListeners();
    }

    private void setListeners() {
        binding.topAppBar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.save_note):
                Note note = new Note(binding.editTextNote.getText().toString());
                FirebaseUtil.addNote(note)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                sendBroadcast(new Intent(MainActivity.FINISH_ACTIVITY));
                                startMainActivity();
                            } else {
                                AndroidUtil.showToast(getApplicationContext(), "Note saving failed");
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}