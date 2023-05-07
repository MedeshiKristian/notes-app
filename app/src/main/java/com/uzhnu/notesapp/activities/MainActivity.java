package com.uzhnu.notesapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setTitle("All notes");

        setListeners();

        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        binding.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.textViewUsername.setText(userModel.getUsername());
                        binding.textViewPhoneNumber.setText(userModel.getPhoneNumber());
                    } else {
                        Log.w(Constants.TAG, "Task for getting user image failed");
                    }
                });

        ArrayList<String> categories = new ArrayList<>();
        categories.add("Personal");
        categories.add("Work");
        categories.add("Life");
        categories.add("Travel");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        binding.listSlidermenu.setAdapter(adapter);
        binding.listSlidermenu2.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
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

        binding.imageViewUser.setOnClickListener(view -> {
            showBottomSheetPickImage();
        });
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
        } else {
            super.onBackPressed();
        }
    }
}