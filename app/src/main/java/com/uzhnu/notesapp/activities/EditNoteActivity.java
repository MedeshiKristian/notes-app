package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityEditNoteBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.PreferencesManager;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends SlidrActivity {
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
    private static final SimpleDateFormat FORMATTER
            = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());

    private int fontSize;

    private ActivityEditNoteBinding binding;

    private NoteModel noteModel;

    private ActivityResultLauncher<Intent> uploadImageFile;
    private ActivityResultLauncher<Intent> uploadAudioFile;
    private ActivityResultLauncher<Intent> uploadVideoFile;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        setProgress(true);
        init();
        uploadImageFile = uploadFile("image/", url -> {
            binding.editor.insertImage(url, "dachshund", 320);
        });
        uploadAudioFile = uploadFile("audio/", url -> {
            binding.editor.insertAudio(url);
        });
        uploadVideoFile = uploadFile("video/", url -> {
            binding.editor.insertVideo(url, 360);
        });
        setListeners();
    }

    private void init() {
        noteModel = (NoteModel) PreferencesManager.getInstance().get(Constants.KEY_NOTE);
        PreferencesManager.getInstance().remove(Constants.KEY_NOTE);
        if (noteModel != null) {
            Log.i(Constants.TAG, noteModel.getText());
            binding.editor.setHtml(noteModel.getText());
        }

        binding.editor.setEditorHeight(200);
        binding.editor.setEditorFontSize(22);
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


        setActionButtons();
        setTextColorButtons();
        setTextBackgroundColorButtons();
    }

    private void setActionButtons() {
        binding.richEditToolbar.actionUndo.setOnClickListener(v -> binding.editor.undo());

        binding.richEditToolbar.actionRedo.setOnClickListener(v -> binding.editor.redo());

        binding.richEditToolbar.actionBold.setOnClickListener(v -> binding.editor.setBold());

        binding.richEditToolbar.actionItalic.setOnClickListener(v -> binding.editor.setItalic());

        binding.richEditToolbar.actionSubscript.setOnClickListener(v -> binding.editor.setSubscript());

        binding.richEditToolbar.actionSuperscript.setOnClickListener(v -> binding.editor.setSuperscript());

        binding.richEditToolbar.actionStrikethrough.setOnClickListener(v -> binding.editor.setStrikeThrough());

        binding.richEditToolbar.actionUnderline.setOnClickListener(v -> binding.editor.setUnderline());

        fontSize = 1;

        binding.richEditToolbar.actionDecreaseText.setOnClickListener(v -> {
            fontSize -= 1;
            if (fontSize < MIN_FONT_SIZE) {
                fontSize = MIN_FONT_SIZE;
            }
            binding.editor.setFontSize(fontSize);
        });

        binding.richEditToolbar.actionIncreaseText.setOnClickListener(v -> {
            fontSize += 1;
            if (fontSize > MAX_FONT_SIZE) {
                fontSize = MAX_FONT_SIZE;
            }
            binding.editor.setFontSize(fontSize);
        });

        binding.richEditToolbar.actionTxtColor.setOnClickListener(new View.OnClickListener() {
            private boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    binding.backgroundColorsLayout.getRoot().setVisibility(View.GONE);
                }
                binding.textColorsLayout.getRoot()
                        .setVisibility(isVisible ? View.GONE : View.VISIBLE);
                isVisible = !isVisible;
            }
        });

        binding.richEditToolbar.actionBgColor.setOnClickListener(new View.OnClickListener() {
            private boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    binding.textColorsLayout.getRoot().setVisibility(View.GONE);
                }
                binding.backgroundColorsLayout.getRoot()
                        .setVisibility(isVisible ? View.GONE : View.VISIBLE);
                isVisible = !isVisible;
            }
        });

        binding.richEditToolbar.actionIndent.setOnClickListener(v -> binding.editor.setIndent());

        binding.richEditToolbar.actionOutdent.setOnClickListener(v -> binding.editor.setOutdent());

        binding.richEditToolbar.actionAlignLeft.setOnClickListener(v -> binding.editor.setAlignLeft());

        binding.richEditToolbar.actionAlignCenter.setOnClickListener(v -> binding.editor.setAlignCenter());

        binding.richEditToolbar.actionAlignRight.setOnClickListener(v -> binding.editor.setAlignRight());

        binding.richEditToolbar.actionBlockquote.setOnClickListener(v -> binding.editor.setBlockquote());

        binding.richEditToolbar.actionInsertBullets.setOnClickListener(v -> binding.editor.setBullets());

        binding.richEditToolbar.actionInsertNumbers.setOnClickListener(v -> binding.editor.setNumbers());

        binding.richEditToolbar.actionInsertYoutube.setOnClickListener(v -> {
            binding.editor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA");
        });

        binding.richEditToolbar.actionInsertImage.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            uploadImageFile.launch(Intent.createChooser(intent, null));
        });

        binding.richEditToolbar.actionInsertAudio.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            uploadAudioFile.launch(Intent.createChooser(intent, null));
        });

        binding.richEditToolbar.actionInsertVideo.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            uploadVideoFile.launch(Intent.createChooser(intent, null));
        });

        binding.richEditToolbar.actionInsertLink.setOnClickListener(v -> binding.editor.insertLink("https://github.com/wasabeef", "wasabeef"));

        binding.richEditToolbar.actionInsertCheckbox.setOnClickListener(v -> binding.editor.insertTodo());
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
                EditNoteEvent editNoteEvent;
                if (noteModel != null) {
                    noteModel.setText(newText);
                    editNoteEvent = new EditNoteEvent(noteModel);
                } else {
                    editNoteEvent = new EditNoteEvent(newText);
                }
                EventBus.getDefault().post(editNoteEvent);
                if (backgroundView != null) {
                    backgroundView.setTranslationX(0);
                }
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setProgress(boolean show) {
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

    @NonNull
    private ActivityResultLauncher<Intent> uploadFile(String location,
                                                      ActivityResultCallback<String> callback) {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    setProgress(true);
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        String[] typeInfo = getContentResolver().getType(uri).split("/");
                        Log.d(Constants.TAG, "type: " + Arrays.toString(typeInfo));
                        String fileName = FORMATTER.format(new Date());
                        StorageReference storageReference
                                = FirebaseStorage.getInstance().getReference(location + fileName);
                        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                            StorageMetadata metadata = taskSnapshot.getMetadata();
                            if (metadata != null) {
                                getUrlWithCallback(metadata.getReference(), callback);
                            }
                        });
                    } else {
                        setProgress(false);
                        AndroidUtil.showToast(getApplicationContext(), "Failed to upload");
                    }
                }
        );
    }

    private void getUrlWithCallback(StorageReference storageReference,
                                    ActivityResultCallback<String> callback) {
        if (storageReference != null) {
            storageReference.getDownloadUrl().addOnCompleteListener(uriTask -> {
                if (uriTask.isSuccessful()) {
                    callback.onActivityResult(String.valueOf(uriTask.getResult()));
                    Log.d(Constants.TAG, "" + uriTask.getResult());
                }
            });
        } else {
            Log.e(Constants.TAG, "Failed to get url");
        }
        setProgress(false);
    }
}