package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.events.SelectFolderEvent;
import com.uzhnu.notesapp.events.SelectNoteEvent;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.ImageUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private List<NoteModel> noteModels;
    private NotesAdapter notesAdapter;

    private List<FolderModel> folderModels;
    private FoldersAdapter foldersAdapter;

    private Menu toolBarMenu;

    private ImageUtil imageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        setInitToolBar();

        init();
        loadUserDetails();
        loadUserNotes();
        loadUserFolders();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void init() {
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER,
                Constants.KEY_COLLECTION_FOLDER_DEFAULT);

        imageUtil = new ImageUtil(this, binding.header.imageViewUser);

        noteModels = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteModels, getApplicationContext());
        binding.recyclerViewNotes.setAdapter(notesAdapter);

        folderModels = new ArrayList<>();
        foldersAdapter = new FoldersAdapter(folderModels);
        binding.recyclerViewFolders.setAdapter(foldersAdapter);

    }

    private void setInitToolBar() {
        binding.topAppBar.setTitle(Constants.KEY_COLLECTION_FOLDER_DEFAULT);

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
        binding.topAppBar.setNavigationOnClickListener(view -> toggleNavigationView());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectNoteEvent(@NonNull SelectNoteEvent event) {
        int countSelectedNotes = event.getCountSelectedItems();
        if (countSelectedNotes == 0) {
            setInitToolBar();
        } else {
            if (countSelectedNotes == 1) {
                binding.topAppBar.setNavigationIcon(
                        ContextCompat.getDrawable(
                                getApplicationContext(), R.drawable.ic_baseline_arrow_back_24
                        )
                );
                binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());
                binding.topAppBar.setTitle("1 item selected");
            } else {
                binding.topAppBar.setTitle(countSelectedNotes + " items selected");
            }
        }

        boolean show = countSelectedNotes > 0;
        if (this.toolBarMenu != null) {
            this.toolBarMenu.findItem(R.id.optionDelete).setVisible(show);
            this.toolBarMenu.findItem(R.id.optionLogOut).setVisible(!show);
            if (!show) {
                setInitToolBar();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    public void onEditNoteEvent(@NonNull EditNoteEvent event) {
        NoteModel noteModel = new NoteModel(event.getNoteText());
        setIsProgressNotes(true);
        if (event.getNoteId().isEmpty()) {
            FirebaseUtil.addUserNote(noteModel).addOnCompleteListener(task -> {
                noteModels.add(0, noteModel);
                noteModel.setDocumentId(task.getResult().getId());
                notesAdapter.notifyDataSetChanged();
                binding.recyclerViewNotes.smoothScrollToPosition(0);
                setIsProgressNotes(false);
            });
        } else {
            noteModel.setDocumentId(event.getNoteId());
            FirebaseUtil.updateUserNote(noteModel)
                    .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    NoteModel note = noteModels.get(0);
                                    noteModels.set(0, noteModel);
                                    noteModels.set(event.getPosition(), note);
                                    notesAdapter.notifyDataSetChanged();
                                    setIsProgressNotes(false);
                                }
                            }
                    );
        }
    }

    @Subscribe
    public void onSelectFolderEvent(@NonNull SelectFolderEvent event) {
        toggleNavigationView();
        String folderName = event.getFolderName();
        binding.topAppBar.setTitle(folderName);
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER, folderName);
        loadUserNotes();
    }

    private void setListeners() {
        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });
        binding.header.imageViewUser.setOnClickListener(view -> imageUtil.showBottomSheetPickImage());
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
        FirebaseUtil.getNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        noteModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            noteModels.add(FirebaseUtil.getNoteFromDocument(queryDocumentSnapshot));
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
        FirebaseUtil.getFolders().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        folderModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            folderModels.add(FirebaseUtil.getFolderFromDocument(queryDocumentSnapshot));
                        }
                        if (folderModels.size() > 0) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.toolBarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.optionDelete):
                notesAdapter.deleteAllSelectedNotes();
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

    private void toggleNavigationView() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            toggleNavigationView();
        } else if (notesAdapter.isDeleteActionVisible()) {
            EventBus.getDefault().post(new SelectNoteEvent(0));
            notesAdapter.removeAllSelections(true);
            setInitToolBar();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}