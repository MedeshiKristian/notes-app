package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.LockSlidrEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;

import org.greenrobot.eventbus.EventBus;

public class ShowNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        init();
        setListeners();
    }

    private void init() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);

        binding.toolbar.setTitle("Note");

        binding.editor.setEnabled(false);
        binding.editor.setEditorHeight(200);
        binding.editor.setEditorFontSize(22);

        binding.richEditToolbar.scrollViewActions.setVisibility(View.GONE);

        setProgress(true);
        String notePath = getIntent().getStringExtra(Constants.KEY_NOTE_PATH);
        Log.d(Constants.TAG, "Received the path: " + notePath);

        StoreUtil.getDatebase().document(notePath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    NoteModel noteModel = NoteModel.toNote(documentSnapshot);
                    binding.editor.setHtml(noteModel.getText());
                    setProgress(false);
                });
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());


        binding.editor.setOnInitialLoadListener(isReady -> {
            if (isReady) {
                setProgress(false);
            }
        });

        binding.toolbar.setOnClickListener(view -> {
            binding.editor.clearFocus();
        });
    }

    private void setProgress(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.editor.setVisibility(View.GONE);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.editor.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ShowNoteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}