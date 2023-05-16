package com.uzhnu.notesapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;

import jp.wasabeef.richeditor.RichEditor;

public class EditNoteActivity extends AppCompatActivity {
    private ActivityEditNoteBinding binding;

    private NoteModel noteModel;

    private int fontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        setIsProgress(true);

        noteModel = (NoteModel) PreferencesManager.getInstance().get(Constants.KEY_NOTE);
        PreferencesManager.getInstance().remove(Constants.KEY_NOTE);
        if (noteModel != null) {
            Log.i(Constants.TAG, noteModel.getText());
            binding.editor.setHtml(noteModel.getText());
        }

        binding.editor.setEditorHeight(200);
        binding.editor.setEditorFontSize(22);

        setListeners();

        binding.editor.setOnInitialLoadListener(isReady -> {
            if (isReady) {
                setIsProgress(false);
            }
        });
    }

    private void setListeners() {
        binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());

        findViewById(R.id.action_undo).setOnClickListener(v -> binding.editor.undo());

        findViewById(R.id.action_redo).setOnClickListener(v -> binding.editor.redo());

        findViewById(R.id.action_bold).setOnClickListener(v -> binding.editor.setBold());

        findViewById(R.id.action_italic).setOnClickListener(v -> binding.editor.setItalic());

        findViewById(R.id.action_subscript).setOnClickListener(v -> binding.editor.setSubscript());

        findViewById(R.id.action_superscript).setOnClickListener(v -> binding.editor.setSuperscript());

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> binding.editor.setStrikeThrough());

        findViewById(R.id.action_underline).setOnClickListener(v -> binding.editor.setUnderline());

        fontSize = 1;

        findViewById(R.id.action_increase_text).setOnClickListener(v -> {
            fontSize -= 1;
            if (fontSize < 1) fontSize = 1;
            binding.editor.setFontSize(fontSize);
        });

        findViewById(R.id.action_increase_text).setOnClickListener(v -> {
            fontSize += 1;
            if (fontSize > 7) fontSize = 6;
            binding.editor.setFontSize(fontSize);
        });

//        findViewById(R.id.action_heading1).setOnClickListener(v -> binding.editor.setHeading(1));
//
//        findViewById(R.id.action_heading2).setOnClickListener(v -> binding.editor.setHeading(2));
//
//        findViewById(R.id.action_heading3).setOnClickListener(v -> binding.editor.setHeading(3));
//
//        findViewById(R.id.action_heading4).setOnClickListener(v -> binding.editor.setHeading(4));
//
//        findViewById(R.id.action_heading5).setOnClickListener(v -> binding.editor.setHeading(5));
//
//        findViewById(R.id.action_heading6).setOnClickListener(v -> binding.editor.setHeading(6));

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged = false;

            @Override
            public void onClick(View v) {
                binding.editor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                binding.editor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(v -> binding.editor.setIndent());

        findViewById(R.id.action_outdent).setOnClickListener(v -> binding.editor.setOutdent());

        findViewById(R.id.action_align_left).setOnClickListener(v -> binding.editor.setAlignLeft());

        findViewById(R.id.action_align_center).setOnClickListener(v -> binding.editor.setAlignCenter());

        findViewById(R.id.action_align_right).setOnClickListener(v -> binding.editor.setAlignRight());

        findViewById(R.id.action_blockquote).setOnClickListener(v -> binding.editor.setBlockquote());

        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> binding.editor.setBullets());

        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> binding.editor.setNumbers());

        findViewById(R.id.action_insert_image).setOnClickListener(v -> binding.editor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg",
                "dachshund", 320));

        findViewById(R.id.action_insert_youtube).setOnClickListener(v -> binding.editor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA"));

        findViewById(R.id.action_insert_audio).setOnClickListener(v -> binding.editor.insertAudio("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3"));

        findViewById(R.id.action_insert_video).setOnClickListener(v -> binding.editor.insertVideo("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4", 360));

        findViewById(R.id.action_insert_link).setOnClickListener(v -> binding.editor.insertLink("https://github.com/wasabeef", "wasabeef"));

        findViewById(R.id.action_insert_checkbox).setOnClickListener(v -> binding.editor.insertTodo());
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
                String newText = binding.editor.getHtml();
                Log.i(Constants.TAG, newText);
                if (newText.isEmpty()) {
                    onBackPressed();
                    return true;
                }
                EditNoteEvent editNoteEvent = new EditNoteEvent(newText);
                if (noteModel != null) {
                    noteModel.setText(newText);
                    noteModel.updateLastEdited();
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

    private void setIsProgress(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.editor.setVisibility(View.GONE);
            binding.richEditToolbar.scrollViewActions.setVisibility(View.GONE);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.editor.setVisibility(View.VISIBLE);
            binding.richEditToolbar.scrollViewActions.setVisibility(View.VISIBLE);
        }
    }
}