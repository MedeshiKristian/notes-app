package com.uzhnu.notesapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;

public class EditNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    private NoteModel noteModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        noteModel = (NoteModel) PreferencesManager.getInstance().get(Constants.KEY_NOTE);
        PreferencesManager.getInstance().remove(Constants.KEY_NOTE);
        if (noteModel != null) {
            binding.editTextNote.setText(noteModel.getText());
        }

        setListeners();
    }

    private void setListeners() {
        binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());
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
                String newText = binding.editTextNote.getText().toString();
                if (newText.isEmpty()) {
                    onBackPressed();
                    return true;
                }
                EditNoteEvent editNoteEvent = new EditNoteEvent(newText);
                if (noteModel != null) {
                    int position = (int) PreferencesManager.getInstance().get(Constants.KEY_POSITION);
                    editNoteEvent.setPosition(position);
                    editNoteEvent.setNoteId(noteModel.getDocumentId());
                }
                EventBus.getDefault().post(editNoteEvent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}