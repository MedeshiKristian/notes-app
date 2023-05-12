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
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.uzhnu.notesapp.adapters.CategoriesAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.models.CategoryModel;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    public static final String FINISH_ACTIVITY = "finishMainActivity";

    private static final String defaultTitle = "My notes";

    private ActivityMainBinding binding;
    private List<NoteModel> noteModels;
    private NotesAdapter notesAdapter;

    private List<CategoryModel> categoryModels;
    private CategoriesAdapter categoriesAdapter;

    private Function<Boolean, Void> setDeleteActionVisible;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (action.equals(FINISH_ACTIVITY)) {
                finish();
            }
        }
    };

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
        loadUserCategories();

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

        categoryModels = new ArrayList<>();
        categoriesAdapter = new CategoriesAdapter(categoryModels, categoryName -> {
            // TODO Load notes from categoryName collection
            return null;
        });
        binding.recyclerViewCategories.setAdapter(categoriesAdapter);
    }

    private void updateToolBar() {
        if (binding != null) {
            if (notesAdapter == null || !notesAdapter.getDeleteActionVisible()) {
                binding.topAppBar.setTitle(defaultTitle);

                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                        binding.drawerLayout, binding.topAppBar,
                        R.string.navigation_open, R.string.navigation_close);

                binding.drawerLayout.addDrawerListener(toggle);

                toggle.syncState();

                binding.topAppBar.setNavigationOnClickListener(view -> {
                    toggleSidebar();
//                    binding.drawerLayout.openDrawer(GravityCompat.START);
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
                    binding.topAppBar.setNavigationOnClickListener(view -> {
                        onBackPressed();
                    });
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

        binding.imageViewUser.setOnClickListener(view -> {
            showBottomSheetPickImage();
        });

        binding.textViewAddCategory.setOnClickListener(view -> {
            // TODO Open dialog with input category name
        });
    }

    private void loadUserDetails() {
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.textViewUsername.setText(userModel.getUsername());
                        binding.textViewPhoneNumber.setText(PhoneNumberUtils.formatNumber(
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
    private void loadUserCategories() {
        setIsProgressCategories(true);
        FirebaseUtil.getCurrentUserCategories().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        categoryModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            categoryModels.add(FirebaseUtil.getCategory(queryDocumentSnapshot));
                        }
                        if (categoryModels.size() > 0) {
                            categoriesAdapter.notifyDataSetChanged();
                            binding.recyclerViewCategories.smoothScrollToPosition(0);
                        }
                    }
                    setIsProgressCategories(false);
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

    private void setIsProgressCategories(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.recyclerViewCategories.setVisibility(View.GONE);
            binding.circularProgressIndicatorCategories.show();
            binding.circularProgressIndicatorCategories.setProgress(100, true);
        } else {
            binding.circularProgressIndicatorCategories.hide();
            binding.recyclerViewCategories.setVisibility(View.VISIBLE);
        }
    }

    private void showBottomSheetPickImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_pick_image);

        LinearLayout viewPictureLayout = bottomSheetDialog.findViewById(R.id.view_picture_linear_layout);
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

        assert viewPictureLayout != null;
        viewPictureLayout.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
            startActivity(intent);
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
                        String encodedImage = ImageUtil.encodeImage(bitmap);
                        FirebaseUtil.getCurrentUserDetails()
                                .update(Constants.KEY_IMAGE, encodedImage);
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);
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
                                String encodedImage = ImageUtil.encodeImage(bitmap);
                                FirebaseUtil.getCurrentUserDetails()
                                        .update(Constants.KEY_IMAGE, encodedImage);
                                PreferencesManager.getInstance().put(Constants.KEY_IMAGE, encodedImage);
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
//            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (notesAdapter.getDeleteActionVisible()) {
            setDeleteActionVisible.apply(false);
            notesAdapter.removeAllSelections(true);
            updateToolBar();
        } else {
            super.onBackPressed();
        }
    }
}