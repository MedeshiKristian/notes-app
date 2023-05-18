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
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.events.MultiSelectEvent;
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
    private List<NoteModel> allNoteModels;
    private NotesAdapter notesAdapter;

    private List<FolderModel> folderModels;
    private FoldersAdapter foldersAdapter;

    private Menu toolBarMenu;
    private SearchView searchView;

    private ImageUtil imageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarInit);

        setInitToolBar();

        init();

        loadUserDetails();
        loadUserNotes();
        loadUserFolders();

        setListeners();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.toolBarMenu = menu;

        searchView = (SearchView) menu.findItem(R.id.option_search).getActionView();

        searchView.setIconifiedByDefault(true);

        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i(Constants.TAG, "Submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.i(Constants.TAG, s);
                noteModels.clear();
                Log.i(Constants.TAG, "before" + allNoteModels.size());
                for (NoteModel note : allNoteModels) {
                    if (note.getText().contains(s)) {
                        noteModels.add(note);
                    }
                }
                Log.i(Constants.TAG, "after" + allNoteModels.size());
                notesAdapter.notifyDataSetChanged();
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            noteModels.clear();
            noteModels.addAll(allNoteModels);
            notesAdapter.notifyDataSetChanged();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.option_delete):
                notesAdapter.deleteAllSelectedNotes();
                return true;
            case (R.id.option_log_out):
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
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().register(notesAdapter);
    }

    private void init() {
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER,
                Constants.KEY_COLLECTION_FOLDER_DEFAULT);

//        for (int i = 0; i < 120; i++) {
//            FirebaseUtil.addUserNoteToFolder(new NoteModel("Note " + i));
//        }

        imageUtil = new ImageUtil(this, binding.navigationStart.header.imageViewUser);

        noteModels = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteModels, getApplicationContext());
        binding.notesContent.recyclerViewNotes.setAdapter(notesAdapter);

        folderModels = new ArrayList<>();
        foldersAdapter = new FoldersAdapter(folderModels);
        binding.navigationStart.recyclerViewFolders.setAdapter(foldersAdapter);
    }

    private void setInitToolBar() {
        binding.toolbarInit.setTitle(Constants.KEY_COLLECTION_FOLDER_DEFAULT);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                binding.drawerLayout, binding.toolbarInit,
                R.string.navigation_open, R.string.navigation_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float coefficient = 0.40f;
                float moveFactor = binding.navigationStart.navigationView.getWidth()
                        * slideOffset * coefficient;
                binding.coordinatorContent.setTranslationX(moveFactor);
            }
        };
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.toolbarInit.setNavigationOnClickListener(view -> toggleNavigationView());
        binding.navigationStart.navigationView.setVisibility(View.VISIBLE);
        binding.floatingActionButtonAddNote.setVisibility(View.VISIBLE);
    }

    private void setContextToolBar() {
        binding.toolbarInit.setNavigationIcon(
                ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.ic_baseline_close_24
                )
        );
        binding.toolbarInit.setNavigationOnClickListener(view -> onBackPressed());
        binding.navigationStart.navigationView.setVisibility(View.GONE);
        binding.floatingActionButtonAddNote.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectNoteEvent(@NonNull SelectNoteEvent event) {
        int countSelectedNotes = notesAdapter.getCountSelectedNotes();
        if (countSelectedNotes == 0) {
            setInitToolBar();
        } else {
            if (countSelectedNotes == 1) {
                binding.toolbarInit.setTitle("1 item selected");
            } else {
                binding.toolbarInit.setTitle(countSelectedNotes + " items selected");
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    public void onEditNoteEvent(@NonNull EditNoteEvent event) {
        NoteModel noteModel = new NoteModel(event.getNoteText());
        setIsProgressNotes(true);
        if (event.isNewNote()) {
            FirebaseUtil.addUserNoteToFolder(noteModel).addOnCompleteListener(task -> {
                noteModels.add(0, noteModel);
                noteModel.setDocumentId(task.getResult().getId());
                notesAdapter.notifyDataSetChanged();
                binding.notesContent.recyclerViewNotes.smoothScrollToPosition(0);
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
        binding.toolbarInit.setTitle(folderName);
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER, folderName);
        loadUserNotes();
    }

    @Subscribe
    public void onMultiSelectEvent(@NonNull MultiSelectEvent event) {
        boolean show = event.isShow();
        if (show) {
            setContextToolBar();
        } else {
            setInitToolBar();
        }
        if (this.toolBarMenu != null) {
            this.toolBarMenu.findItem(R.id.option_delete).setVisible(show);
            this.toolBarMenu.findItem(R.id.option_search).setVisible(!show);
            this.toolBarMenu.findItem(R.id.option_log_out).setVisible(!show);
        }
    }

    private void setListeners() {
        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        binding.navigationStart.header.imageViewUser.setOnClickListener(view -> imageUtil.showBottomSheetPickImage());
    }

    private void loadUserDetails() {
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        PreferencesManager.getInstance().put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.navigationStart.header.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.navigationStart.header.textViewUsername.setText(userModel.getUsername());
                        binding.navigationStart.header.textViewPhoneNumber.setText(PhoneNumberUtils.formatNumber(
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
        FirebaseUtil.getCurrentFolderNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        noteModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            noteModels.add(FirebaseUtil.getNoteFromDocument(queryDocumentSnapshot));
                        }
                        if (noteModels.size() > 0) {
                            Collections.sort(noteModels);
                            allNoteModels = new ArrayList<>(noteModels);
                            Log.i(Constants.TAG, "Loaded" + allNoteModels.size());
                            notesAdapter.notifyDataSetChanged();
                            binding.notesContent.recyclerViewNotes.smoothScrollToPosition(0);
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
                            binding.navigationStart.recyclerViewFolders.smoothScrollToPosition(0);
                        }
                    }
                    setIsProgressFolders(false);
                });
    }

    private void setIsProgressNotes(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.notesContent.recyclerViewNotes.setVisibility(View.GONE);
            binding.circularProgressIndicatorNotes.show();
            binding.circularProgressIndicatorNotes.setProgress(100, true);
        } else {
            binding.circularProgressIndicatorNotes.hide();
            binding.notesContent.recyclerViewNotes.setVisibility(View.VISIBLE);
        }
    }

    private void setIsProgressFolders(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.navigationStart.recyclerViewFolders.setVisibility(View.GONE);
            binding.navigationStart.circularProgressIndicatorFolders.show();
            binding.navigationStart.circularProgressIndicatorFolders.setProgress(100, true);
        } else {
            binding.navigationStart.circularProgressIndicatorFolders.hide();
            binding.navigationStart.recyclerViewFolders.setVisibility(View.VISIBLE);
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
        } else if (notesAdapter.isMultiSelect()) {
            EventBus.getDefault().post(new MultiSelectEvent(false));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(notesAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}