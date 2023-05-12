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
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;

import java.util.Date;

public class EditNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    private NoteModel noteModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.noteModel = getIntent().getParcelableExtra(Constants.KEY_NOTE);

        if (noteModel != null) {
            Log.i(Constants.TAG, noteModel.getText());
            Log.i(Constants.TAG, noteModel.getDocumentId());
            Log.i(Constants.TAG, "Creater at: " + noteModel.getCreatedAt().toString());
            Log.i(Constants.TAG, "Last edited: " + noteModel.getLastEdited().toString());
            binding.editTextNote.setText(noteModel.getText());
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
                if (noteModel == null) {
                    NoteModel noteModel = new NoteModel(binding.editTextNote.getText().toString());
                    if (noteModel.getText().isEmpty()) {
                        onBackPressed();
                        return true;
                    }
                    Log.i(Constants.TAG, "Save new noteModel");
                    FirebaseUtil.addUserNote(noteModel)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sendBroadcast(new Intent(MainActivity.FINISH_ACTIVITY));
                                    startMainActivity();
                                } else {
                                    AndroidUtil.showToast(getApplicationContext(), "NoteModel saving failed");
                                }
                            });
                } else {
                    Log.i(Constants.TAG, "Edit note");
                    String newText = binding.editTextNote.getText().toString();
                    FirebaseUtil.getUserNote(noteModel.getDocumentId())
                            .update(Constants.KEY_TEXT, newText,
                                    Constants.KEY_LAST_EDITED, new Date())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sendBroadcast(new Intent(MainActivity.FINISH_ACTIVITY));
                                    startMainActivity();
                                } else {
                                    AndroidUtil.showToast(getApplicationContext(), "NoteModel saving failed");
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