package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.events.LockSlidrEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class EditNoteActivity extends AppCompatActivity {
    private static final int MIN_FONT_SIZE = 1;
    private static final int MAX_FONT_SIZE = 7;
    private static final int COLOR_VIOLET = Color.rgb(148, 0, 211);
    private static final int COLOR_INDIGO = Color.rgb(75, 0, 130);
    private static final int COLOR_BLUE = Color.rgb(0, 0, 255);
    private static final int COLOR_GREEN = Color.rgb(0, 255, 0);
    private static final int COLOR_YELLOW = Color.rgb(255, 255, 0);
    private static final int COLOR_ORANGE = Color.rgb(255, 127, 0);
    private static final int COLOR_RED = Color.rgb(255, 127, 0);
    private static final int COLOR_WHITE = Color.rgb(255, 255, 255);
    private static final int COLOR_BLACK = Color.rgb(0, 0, 0);

    private int fontSize;

    private ActivityEditNoteBinding binding;

    private NoteModel noteModel;

    private SlidrInterface slidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

//        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // TODO
        SlidrConfig config = new SlidrConfig.Builder().build();

        slidrInterface = Slidr.attach(this, config);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());

        binding.editor.setOnInitialLoadListener(isReady -> {
            if (isReady) {
                setIsProgress(false);
            }
        });

//        binding.editor.setOnFocusChangeListener((view, b) -> {
//            if (b) {
//                slidrInterface.lock();
//            } else {
//                slidrInterface.unlock();
//            }
//        });

        setActionButtons();
        setTextColorButtons();
        setTextBackgroundColorButtons();
    }

    private void setActionButtons() {
        findViewById(R.id.action_undo).setOnClickListener(v -> binding.editor.undo());

        findViewById(R.id.action_redo).setOnClickListener(v -> binding.editor.redo());

        findViewById(R.id.action_bold).setOnClickListener(v -> binding.editor.setBold());

        findViewById(R.id.action_italic).setOnClickListener(v -> binding.editor.setItalic());

        findViewById(R.id.action_subscript).setOnClickListener(v -> binding.editor.setSubscript());

        findViewById(R.id.action_superscript).setOnClickListener(v -> binding.editor.setSuperscript());

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> binding.editor.setStrikeThrough());

        findViewById(R.id.action_underline).setOnClickListener(v -> binding.editor.setUnderline());

        fontSize = 1;

        findViewById(R.id.action_decrease_text).setOnClickListener(v -> {
            fontSize -= 1;
            if (fontSize < MIN_FONT_SIZE) {
                fontSize = MIN_FONT_SIZE;
            }
            binding.editor.setFontSize(fontSize);
        });

        findViewById(R.id.action_increase_text).setOnClickListener(v -> {
            fontSize += 1;
            if (fontSize > MAX_FONT_SIZE) {
                fontSize = MAX_FONT_SIZE;
            }
            binding.editor.setFontSize(fontSize);
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    binding.backgroundColorsLayout.getRoot()
                            .setVisibility(View.GONE);
                }
                binding.textColorsLayout.getRoot()
                        .setVisibility(isVisible ? View.GONE : View.VISIBLE);
                isVisible = !isVisible;
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    binding.textColorsLayout.getRoot()
                            .setVisibility(View.GONE);
                }
                binding.backgroundColorsLayout.getRoot()
                        .setVisibility(isVisible ? View.GONE : View.VISIBLE);
                isVisible = !isVisible;
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

    private void setTextColorButtons() {
        binding.textColorsLayout.violetColor.setBackgroundColor(COLOR_VIOLET);
        binding.textColorsLayout.violetColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_VIOLET);
        });

        binding.textColorsLayout.indigoColor.setBackgroundColor(COLOR_INDIGO);
        binding.textColorsLayout.indigoColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_INDIGO);
        });

        binding.textColorsLayout.blueColor.setBackgroundColor(COLOR_BLUE);
        binding.textColorsLayout.blueColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_BLUE);
        });

        binding.textColorsLayout.greenColor.setBackgroundColor(COLOR_GREEN);
        binding.textColorsLayout.greenColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_GREEN);
        });

        binding.textColorsLayout.yellowColor.setBackgroundColor(COLOR_YELLOW);
        binding.textColorsLayout.yellowColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_YELLOW);
        });

        binding.textColorsLayout.orangeColor.setBackgroundColor(COLOR_ORANGE);
        binding.textColorsLayout.orangeColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_ORANGE);
        });

        binding.textColorsLayout.redColor.setBackgroundColor(COLOR_RED);
        binding.textColorsLayout.redColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_RED);
        });

        binding.textColorsLayout.whiteColor.setBackgroundColor(COLOR_WHITE);
        binding.textColorsLayout.whiteColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_WHITE);
        });

        binding.textColorsLayout.blackColor.setBackgroundColor(COLOR_BLACK);
        binding.textColorsLayout.blackColor.setOnClickListener(view -> {
            binding.editor.setTextColor(COLOR_BLACK);
        });
    }

    private void setTextBackgroundColorButtons() {
        binding.backgroundColorsLayout.violetColor.setBackgroundColor(COLOR_VIOLET);
        binding.backgroundColorsLayout.violetColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_VIOLET);
        });

        binding.backgroundColorsLayout.indigoColor.setBackgroundColor(COLOR_INDIGO);
        binding.backgroundColorsLayout.indigoColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_INDIGO);
        });

        binding.backgroundColorsLayout.blueColor.setBackgroundColor(COLOR_BLUE);
        binding.backgroundColorsLayout.blueColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_BLUE);
        });

        binding.backgroundColorsLayout.greenColor.setBackgroundColor(COLOR_GREEN);
        binding.backgroundColorsLayout.greenColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_GREEN);
        });

        binding.backgroundColorsLayout.yellowColor.setBackgroundColor(COLOR_YELLOW);
        binding.backgroundColorsLayout.yellowColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_YELLOW);
        });

        binding.backgroundColorsLayout.orangeColor.setBackgroundColor(COLOR_ORANGE);
        binding.backgroundColorsLayout.orangeColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_ORANGE);
        });

        binding.backgroundColorsLayout.redColor.setBackgroundColor(COLOR_RED);
        binding.backgroundColorsLayout.redColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_RED);
        });

        binding.backgroundColorsLayout.whiteColor.setBackgroundColor(COLOR_WHITE);
        binding.backgroundColorsLayout.whiteColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_WHITE);
        });

        binding.backgroundColorsLayout.blackColor.setBackgroundColor(COLOR_BLACK);
        binding.backgroundColorsLayout.blackColor.setOnClickListener(view -> {
            binding.editor.setTextBackgroundColor(COLOR_BLACK);
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
            case (R.id.option_save_note):
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

    @Override
    public void onBackPressed() {
        if (MainActivity.isRunning) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
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

    @SuppressLint("ResourceAsColor")
    @Subscribe
    public void onLockSlidrEvent(LockSlidrEvent event) {
        getWindow().setStatusBarColor(R.color.primary);
        slidrInterface.lock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}