package com.uzhnu.notesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.models.Note;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;

import java.util.Date;

public class EditNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.note = getIntent().getParcelableExtra(Constants.KEY_NOTE);

        if (note != null) {
            Log.i(Constants.TAG, note.getText());
            Log.i(Constants.TAG, note.getDocumentId());
            Log.i(Constants.TAG, "Creater at: " + note.getCreatedAt().toString());
            Log.i(Constants.TAG, "Last edited: " + note.getLastEdited().toString());
            binding.editTextNote.setText(note.getText());
        }
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
                if (note == null) {
                    Note note = new Note(binding.editTextNote.getText().toString());
                    if (note.getText().isEmpty()) {
                        onBackPressed();
                        return true;
                    }
                    Log.i(Constants.TAG, "Save new note");
                    FirebaseUtil.addUserNote(note)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sendBroadcast(new Intent(MainActivity.FINISH_ACTIVITY));
                                    startMainActivity();
                                } else {
                                    AndroidUtil.showToast(getApplicationContext(), "Note saving failed");
                                }
                            });
                } else {
                    Log.i(Constants.TAG, "Edit note");
                    String newText = binding.editTextNote.getText().toString();
                    FirebaseUtil.getUserNote(note.getDocumentId())
                            .update(Constants.KEY_TEXT, newText,
                                    Constants.KEY_LAST_EDITED, new Date())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sendBroadcast(new Intent(MainActivity.FINISH_ACTIVITY));
                                    startMainActivity();
                                } else {
                                    AndroidUtil.showToast(getApplicationContext(), "Note saving failed");
                                }
                            });
                }
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