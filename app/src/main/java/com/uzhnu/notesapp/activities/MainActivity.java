package com.uzhnu.notesapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    public static final String FINISH_ACTIVITY = "finishMainActivity";

    private ActivityMainBinding binding;
    
    private List<NoteModel> noteModels;
    private NotesAdapter notesAdapter;

    private List<FolderModel> folderModels;
    private FoldersAdapter foldersAdapter;

    private Function<Boolean, Void> setDeleteActionVisible;

    private BroadcastReceiver broadcastReceiver;
    private ActivityResultLauncher<Intent> pickImageFromGallery;
    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<String> requestWriteStorePermission;

    Uri camUri;
    private ActivityResultLauncher<Intent> pickImageFromCamera;

    @Override
    protected void onStart() {
        super.onStart();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                String action = intent.getAction();
                if (action.equals(FINISH_ACTIVITY)) {
                    finish();
                }
            }
        };
        pickImageFromGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                ImageUtil.getActivityResultCallbackForGallery(
                        getApplicationContext(), binding.header.imageViewUser
                )
        );
        pickImageFromCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        try {
                            Bitmap bitmap = ImageUtil.getBitmapFromUri(getApplicationContext(), camUri);
                            assert bitmap != null;
                            binding.header.imageViewUser.setImageBitmap(bitmap);
                            String encodedImage = ImageUtil.encodeImage(bitmap);
                            FirebaseUtil.getCurrentUserDetails().update(Constants.KEY_IMAGE, encodedImage);
                            PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);

                        } catch (FileNotFoundException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
        );
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermission.launch(Manifest.permission.WRITE_APN_SETTINGS);
                    } else if (result) {
                        launchCamera();
                    }
                }
        );
        requestWriteStorePermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (checkSelfPermission(Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermission.launch(Manifest.permission.CAMERA);
                    } else if (result) {
                        launchCamera();
                    }
                }
        );
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        camUri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, camUri);
        pickImageFromCamera.launch(cameraIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        updateToolBar();

        init();
        loadUserDetails();
        loadUserNotes();
        loadUserFolders();

        setListeners();

        registerReceiver(broadcastReceiver, new IntentFilter(FINISH_ACTIVITY));
    }

    private void init() {
        noteModels = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteModels, (foo) -> {
            this.updateToolBar();
            return null;
        }, getApplicationContext());
        binding.recyclerViewNotes.setAdapter(notesAdapter);

        folderModels = new ArrayList<>();
        foldersAdapter = new FoldersAdapter(folderModels, folderName -> {
            // TODO Load notes from folderName collection
            return null;
        });
        binding.recyclerViewFolders.setAdapter(foldersAdapter);
    }

    private void updateToolBar() {
        if (binding != null) {
            if (notesAdapter == null || !notesAdapter.getDeleteActionVisible()) {
                binding.topAppBar.setTitle(Constants.KEY_COLLECTION_FOLDERS_DEFAULT);

                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                        binding.drawerLayout, binding.topAppBar,
                        R.string.navigation_open, R.string.navigation_close) {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        super.onDrawerSlide(drawerView, slideOffset);
                        float moveFactor = binding.navigationView.getWidth() * slideOffset;
                        binding.coordinatorContent.setTranslationX(moveFactor);
                    }
                };

                binding.drawerLayout.addDrawerListener(toggle);

                toggle.syncState();

                binding.topAppBar.setNavigationOnClickListener(view -> {
                    toggleSidebar();
                });
            } else {
                int countSelectedItems = notesAdapter.getCountSelectedItems();
                if (countSelectedItems == 1) {
                    binding.topAppBar.setNavigationIcon(
                            ContextCompat.getDrawable(
                                    getApplicationContext(),
                                    R.drawable.ic_baseline_arrow_back_24
                            )
                    );
                    binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());
                    binding.topAppBar.setTitle("1 item selected");
                } else {
                    binding.topAppBar.setTitle(countSelectedItems + " items selected");
                }
            }
        }
    }

    private void setListeners() {
        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });
        binding.header.imageViewUser.setOnClickListener(view -> showBottomSheetPickImage());
    }

    private void loadUserDetails() {
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.header.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.header.textViewUsername.setText(userModel.getUsername());
                        binding.header.textViewPhoneNumber.setText(PhoneNumberUtils.formatNumber(
                                userModel.getPhoneNumber(),
                                Locale.getDefault().getCountry()
                        ));
                    } else {
                        Log.e(Constants.TAG, "Task for getting user image failed");
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUserNotes() {
        setIsProgressNotes(true);
        FirebaseUtil.getCurrentUserNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        noteModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            noteModels.add(FirebaseUtil.getNote(queryDocumentSnapshot));
                        }
                        if (noteModels.size() > 0) {
                            Collections.sort(noteModels);
                            notesAdapter.notifyDataSetChanged();
                            binding.recyclerViewNotes.smoothScrollToPosition(0);
                        }
                    }
                    setIsProgressNotes(false);
                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUserFolders() {
        setIsProgressFolders(true);
        FirebaseUtil.getCurrentUserFolders().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        folderModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            folderModels.add(FirebaseUtil.getFolder(queryDocumentSnapshot));
                        }
                        if (folderModels.size() > 0) {
                            Log.i(Constants.TAG, "folders are not empty");
                            foldersAdapter.notifyDataSetChanged();
                            binding.recyclerViewFolders.smoothScrollToPosition(0);
                        }
                    }
                    setIsProgressFolders(false);
                });
    }

    private void setIsProgressNotes(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.recyclerViewNotes.setVisibility(View.GONE);
            binding.circularProgressIndicatorNotes.show();
            binding.circularProgressIndicatorNotes.setProgress(100, true);
        } else {
            binding.circularProgressIndicatorNotes.hide();
            binding.recyclerViewNotes.setVisibility(View.VISIBLE);
        }
    }

    private void setIsProgressFolders(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.recyclerViewFolders.setVisibility(View.GONE);
            binding.circularProgressIndicatorFolders.show();
            binding.circularProgressIndicatorFolders.setProgress(100, true);
        } else {
            binding.circularProgressIndicatorFolders.hide();
            binding.recyclerViewFolders.setVisibility(View.VISIBLE);
        }
    }

    private void showBottomSheetPickImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_pick_image);
        bottomSheetDialog.show();

        LinearLayout viewPictureLayout = bottomSheetDialog.findViewById(R.id.view_picture_linear_layout);
        LinearLayout cameraLayout = bottomSheetDialog.findViewById(R.id.camera_linear_layout);
        LinearLayout galleryLayout = bottomSheetDialog.findViewById(R.id.gallery_linear_layout);


        assert cameraLayout != null;
        cameraLayout.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestWriteStorePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                launchCamera();
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

        assert viewPictureLayout != null;
        viewPictureLayout.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        setDeleteActionVisible = (show) -> {
            if (menu != null) {
                menu.findItem(R.id.optionDelete).setVisible(show);
                menu.findItem(R.id.optionLogOut).setVisible(!show);
                assert binding != null;
                notesAdapter.setIsDeleteActionVisible(show);
            }
            return null;
        };
        notesAdapter.setSetDeleteActionVisibleInMainCallback(setDeleteActionVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.optionDelete):
                notesAdapter.deleteAllSelectedItems();
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

    private void toggleSidebar() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            toggleSidebar();
        } else if (notesAdapter.getDeleteActionVisible()) {
            setDeleteActionVisible.apply(false);
            notesAdapter.removeAllSelections(true);
            updateToolBar();
        } else {
            super.onBackPressed();
        }
    }
}