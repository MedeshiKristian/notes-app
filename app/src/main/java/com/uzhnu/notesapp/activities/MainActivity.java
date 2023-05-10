package com.uzhnu.notesapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.models.Note;
import com.uzhnu.notesapp.models.User;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    public static final String FINISH_ACTIVITY = "finish_main_activity";

    private ActivityMainBinding binding;
    private List<Note> notes;
    private NotesAdapter notesAdapter;

    private Function<Boolean, Void> setDeleteActionVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        updateToolBarTitle();

        init();
        loadUserDetails();
        loadUserNotes();

        setListeners();

        ArrayList<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Work");
        categories.add("Life");
        categories.add("Travel");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        binding.listSlidermenu.setAdapter(adapter);

        registerReceiver(broadcastReceiver, new IntentFilter(FINISH_ACTIVITY));
    }

    private void updateToolBarTitle() {
        if (binding != null) {
            if (notesAdapter == null || !notesAdapter.getDeleteActionVisible()) {
                binding.topAppBar.setTitle("My notes");
            } else {
                int countSelectedItems = notesAdapter.getCountSelectedItems();
                if (countSelectedItems == 1) {
                    binding.topAppBar.setTitle("1 item selected");
                } else {
                    binding.topAppBar.setTitle(countSelectedItems + " items selected");
                }
            }
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FINISH_ACTIVITY)) {
                finish();
            }
        }
    };

    private void loadUserDetails() {
        setIsProgress(true);
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    setIsProgress(false);
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        assert user != null;
                        binding.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(user.getImage()));
                        binding.textViewUsername.setText(user.getUsername());
                        binding.textViewPhoneNumber.setText(PhoneNumberUtils.formatNumber(
                                user.getPhoneNumber(),
                                Locale.getDefault().getCountry()
                        ));

                    } else {
                        Log.e(Constants.TAG, "Task for getting user image failed");
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUserNotes() {
        setIsProgress(true);
        FirebaseUtil.getCurrentUserNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        notes.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            notes.add(FirebaseUtil.getNote(queryDocumentSnapshot));
                        }
                        if (notes.size() > 0) {
                            Collections.sort(notes);
                            notesAdapter.notifyDataSetChanged();
                            binding.recyclerViewNotes.smoothScrollToPosition(0);
                            binding.recyclerViewNotes.setVisibility(View.VISIBLE);
                        }
                    }
                    setIsProgress(false);
                });

    }

    private void setListeners() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                binding.drawerLayout, binding.topAppBar,
                R.string.navigation_open, R.string.navigation_close);

        binding.drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        binding.topAppBar.setNavigationOnClickListener(view -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        binding.imageViewUser.setOnClickListener(view -> {
            showBottomSheetPickImage();
        });
    }

    private void setIsProgress(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.recyclerViewNotes.setVisibility(View.GONE);
            binding.circularProgressIndicator.show();
            binding.circularProgressIndicator.setProgress(100, true);
        } else {
            binding.circularProgressIndicator.hide();
            binding.recyclerViewNotes.setVisibility(View.VISIBLE);
        }
    }

    private void showBottomSheetPickImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_pick_image);

        LinearLayout cameraLayout = bottomSheetDialog.findViewById(R.id.camera_linear_layout);
        LinearLayout galleryLayout = bottomSheetDialog.findViewById(R.id.gallery_linear_layout);

        bottomSheetDialog.show();

        assert cameraLayout != null;
        cameraLayout.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                pickImageFromCamera.launch(intent);
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            }
            bottomSheetDialog.hide();
        });


        assert galleryLayout != null;
        galleryLayout.setOnClickListener(view -> {
            Intent intent =
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImageFromGallery.launch(intent);
            bottomSheetDialog.hide();
        });
    }

    private final ActivityResultLauncher<Intent> pickImageFromCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        binding.imageViewUser.setImageBitmap(bitmap);
                        FirebaseUtil.getCurrentUserDetails()
                                .update(Constants.KEY_IMAGE, ImageUtil.encodeImage(bitmap));
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    pickImageFromCamera.launch(intent);
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        // TODO NullPointerException
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                binding.imageViewUser.setImageBitmap(bitmap);
                                FirebaseUtil.getCurrentUserDetails()
                                        .update(Constants.KEY_IMAGE, ImageUtil.encodeImage(bitmap));
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Please choose a valid image",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (FileNotFoundException exception) {
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this,
                                    "Please choose a valid file", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void init() {
        notes = new ArrayList<>();
        notesAdapter = new NotesAdapter(notes, (foo) -> {
            this.updateToolBarTitle();
            return null;
        });
        binding.recyclerViewNotes.setAdapter(notesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        setDeleteActionVisible = (show) -> {
            if (menu != null) {
                menu.findItem(R.id.optionDelete).setVisible(show);
                assert binding != null;
                notesAdapter.setDeleteActionVisible(show);
            }
            return null;
        };
        notesAdapter.setSetDeleteActionVisible(setDeleteActionVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.optionDelete):
                notesAdapter.deleteAllSelectedItems();
                //loadUserNotes();
                return true;
            case (R.id.optionLogOut):
                FirebaseUtil.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (notesAdapter.getDeleteActionVisible()) {
            setDeleteActionVisible.apply(false);
            notesAdapter.removeAllSelections(true);
            updateToolBarTitle();
        } else {
            super.onBackPressed();
        }
    }
}