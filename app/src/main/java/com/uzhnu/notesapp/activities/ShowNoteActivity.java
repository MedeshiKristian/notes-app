package com.uzhnu.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.LockSlidrEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;

import org.greenrobot.eventbus.EventBus;

public class ShowNoteActivity extends SlidrActivity {
    private ActivityEditNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle("Note");
        setSupportActionBar(binding.toolbar);

        init();
        setListeners();
    }

    private void init() {
        if (!MainActivity.isRunning) {
            EventBus.getDefault().post(new LockSlidrEvent());
        }

        setProgress(true);
        String notePath = getIntent().getStringExtra(Constants.KEY_NOTE_PATH);
        Log.i(Constants.TAG, "Received the path: " + notePath);

        StoreUtil.getDatebase().document(notePath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    NoteModel noteModel = NoteModel.toNote(documentSnapshot);
                    binding.editor.setHtml(noteModel.getText());
                    setProgress(false);
                });

        binding.editor.setEnabled(false);
        binding.editor.setEditorHeight(200);
        binding.editor.setEditorFontSize(22);

        binding.richEditToolbar.scrollViewActions.setVisibility(View.GONE);
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

        binding.editor.setOnFocusChangeListener((view, b) -> {
            if (b) {
                slidrInterface.lock();
            } else {
                slidrInterface.unlock();
            }
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
}