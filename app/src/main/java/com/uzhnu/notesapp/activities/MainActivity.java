package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.callbacks.SwipeToDeleteCallback;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.dialogs.DeleteNotesDialog;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.events.LockSlidrEvent;
import com.uzhnu.notesapp.events.MultiSelectEvent;
import com.uzhnu.notesapp.events.SelectFolderEvent;
import com.uzhnu.notesapp.events.SelectNoteEvent;
import com.uzhnu.notesapp.events.UnlockSlidrEvent;
import com.uzhnu.notesapp.listeners.ScrollLockTouchListener;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.NotificationModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;
import com.uzhnu.notesapp.utilities.ThemeUtil;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;
import com.uzhnu.notesapp.utilities.firebase.MessagingUtil;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static boolean isRunning = false;
    private ActivityMainBinding binding;

    private List<NoteModel> noteModels;
    private NotesAdapter notesAdapter;
    private LinearLayoutManager layoutManager;

    private List<FolderModel> folderModels;
    private FoldersAdapter foldersAdapter;

    private Menu toolBarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "create");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        init();
        setInitToolBar();
        loadUserDetails();
        loadNotes();
        loadFolders();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Constants.TAG, "start");
        isRunning = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().register(notesAdapter);
        EventBus.getDefault().register(foldersAdapter);
        EventBus.getDefault().post(new UnlockSlidrEvent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.coordinatorContent.setTranslationX(0);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.toolBarMenu = menu;

        Object multiselect = PreferencesManager.getInstance().get(Constants.KEY_MULTISELECT);
        if (multiselect != null) {
            boolean show = (boolean) multiselect;
            setOptionsVisibility(show);
        }

        SearchView searchView = (SearchView) menu.findItem(R.id.option_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setPadding(0, 0, 0, 5);
        searchView.setOnSearchClickListener(view -> {
            binding.floatingActionButtonAddNote.setVisibility(View.INVISIBLE);
        });

        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.i(Constants.TAG, "Submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                assert notesAdapter != null;
                notesAdapter.filter(s, noteModels);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            binding.floatingActionButtonAddNote.setVisibility(View.VISIBLE);
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.option_delete):
                DeleteNotesDialog dialog = new DeleteNotesDialog(notesAdapter, this::loadNotes);
                dialog.show(getSupportFragmentManager(), "Delete notes dialog");
                return true;
            case (R.id.option_manage_access):
                FolderModel currentFolder = StoreUtil.getCurrentFolder();
                if (currentFolder.getCollectionName().equals(Constants.KEY_COLLECTION_FOLDER_DEFAULT)) {
                    AndroidUtil.showToast(getApplicationContext(), "You can't share you default folder");
                } else if (!currentFolder.getCreatedBy().equals(AuthUtil.getCurrentUserId())) {
                    AndroidUtil.showToast(getApplicationContext(), "You are not the creator of this folder");
                } else {
                    Intent intent = new Intent(MainActivity.this,
                            ManageFolderAccessActivity.class);
                    startActivity(intent);
                }
            case (R.id.option_pin):
                notesAdapter.togglePinOnSelectedNotes();
                layoutManager.removeAllViews();
                loadNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
//        initWindow();
        FolderModel defaultFolder = new FolderModel(Constants.KEY_COLLECTION_FOLDER_DEFAULT);
        defaultFolder.setName(Constants.KEY_COLLECTION_FOLDER_DEFAULT);
        StoreUtil.setCurrentFolder(defaultFolder);
        PreferencesManager.getInstance().put(Constants.KEY_COLLECTION_FOLDER_DEFAULT,
                StoreUtil.getCurrentFolder());

        PreferencesManager.getInstance().put(Constants.KEY_MAIN_ACTIVITY, this);

//        for (int i = 0; i < 20; i++) {
//            StoreUtil.addNoteToFolder(new NoteModel("Note " + i));
//        }

        binding.notesContent.swipeRefreshNotes
                .setColorSchemeColors(ThemeUtil.getTextColor(this));
        binding.notesContent.swipeRefreshNotes
                .setProgressBackgroundColorSchemeColor(ThemeUtil.getPrimary(this));

        layoutManager
                = new

                LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);

        noteModels = new ArrayList<>();
        notesAdapter = new

                NotesAdapter(noteModels, layoutManager, getApplicationContext());
        binding.notesContent.recyclerViewNotes.setLayoutManager(layoutManager);
        binding.notesContent.recyclerViewNotes.setAdapter(notesAdapter);

        folderModels = new ArrayList<>();
        foldersAdapter = new

                FoldersAdapter(this, folderModels);
        binding.navigationStart.recyclerViewFolders.setAdapter(foldersAdapter);
    }

    private void initWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        int statusBarHeight = 0;
        @SuppressLint("InternalInsetResource")
        int resourceId = getResources().getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
        );
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        ViewGroup.MarginLayoutParams params
                = (ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();
        params.setMargins(0, statusBarHeight, 0, 0);
        binding.toolbar.setLayoutParams(params);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

//        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        window.getDecorView().setSystemUiVisibility(flags);
    }

    private void initTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean(Constants.KEY_NIGHT_THEME, false);

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setInitToolBar() {
        binding.toolbar.setTitle(StoreUtil.getCurrentFolder().getName());
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                binding.drawerLayout, binding.toolbar,
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

        binding.toolbar.setNavigationOnClickListener(view -> toggleNavigationView());
        binding.navigationStart.navigationView.setVisibility(View.VISIBLE);
        binding.floatingActionButtonAddNote.setVisibility(View.VISIBLE);
    }

    private void setContextToolBar() {
        binding.toolbar.setNavigationIcon(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_close_24)
        );
        binding.toolbar.setNavigationIconTint(ThemeUtil.getTextColor(this));
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());
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
                binding.toolbar.setTitle("1 item selected");
            } else {
                binding.toolbar.setTitle(countSelectedNotes + " items selected");
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    public void onEditNoteEvent(@NonNull EditNoteEvent event) {
        setProgressNotes(true);
        UserModel userModel = (UserModel) PreferencesManager.getInstance().get(Constants.KEY_USER);
        assert userModel != null;
        FolderModel currentFolder = StoreUtil.getCurrentFolder();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setTitle(currentFolder.getName());
        notificationModel.setId((int) new Date().getTime() / 1000);
        if (event.isNewNote()) {
            NoteModel noteModel = new NoteModel(event.getNewNoteText());
            StoreUtil.addNoteToFolder(noteModel)
                    .addOnSuccessListener(documentReference -> {
                loadNotes();
                notificationModel.setText(userModel.getUsername() +
                        " created a note");
                notificationModel.setNotePath(documentReference.getPath());
                sendMessage(currentFolder, notificationModel);
            });
        } else {
            notificationModel.setNotePath(
                    StoreUtil.getCurrentFolderNotes()
                            .document(event.getNoteModel().getDocumentId()).getPath()
            );
            StoreUtil.updateNote(event.getNoteModel())
                    .addOnSuccessListener(unused -> {
                                loadNotes();
                                notificationModel.setText(userModel.getUsername() +
                                        " edited a note");
                                sendMessage(currentFolder, notificationModel);
                            }
                    );
        }
    }

    private void sendMessage(@NonNull FolderModel folderModel,
                             @NonNull NotificationModel notificationModel) {
        MessagingUtil.sendMessage(MessagingUtil.getTopic(folderModel), notificationModel);
    }

    @Subscribe
    public void onSelectFolderEvent(@NonNull SelectFolderEvent event) {
        toggleNavigationView();
        FolderModel folder = event.getFolder();
        binding.toolbar.setTitle(folder.getName());
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER, folder);
        loadNotes();
    }

    @Subscribe
    public void onMultiSelectEvent(@NonNull MultiSelectEvent event) {
        boolean show = event.isShow();
        PreferencesManager.getInstance().put(Constants.KEY_MULTISELECT, show);
        if (show) {
            setContextToolBar();
            binding.navigationStart.getRoot().setVisibility(View.GONE);
        } else {
            setInitToolBar();
            binding.navigationStart.navigationView.setVisibility(View.VISIBLE);
        }
        setOptionsVisibility(show);
    }

    private void setOptionsVisibility(boolean multiselectShow) {
        if (this.toolBarMenu != null) {
            this.toolBarMenu.findItem(R.id.option_delete).setVisible(multiselectShow);
            this.toolBarMenu.findItem(R.id.option_pin).setVisible(multiselectShow);
            this.toolBarMenu.findItem(R.id.option_search).setVisible(!multiselectShow);
            this.toolBarMenu.findItem(R.id.option_manage_access).setVisible(!multiselectShow);
        }
    }

    private void setListeners() {
        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        binding.navigationStart.layoutLogOut.setOnClickListener(view -> {
            AuthUtil.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.navigationStart.layoutSettings.setOnClickListener(view -> {
            toggleNavigationView();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        StoreUtil.getCurrentUser()
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constants.TAG, "Error occurred in change image listener");
                        error.printStackTrace();
                        return;
                    }
                    if (value != null) {
                        UserModel userModel = value.toObject(UserModel.class);
                        assert userModel != null;
                        binding.navigationStart.header.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.navigationStart.header.textViewUsername
                                .setText(userModel.getUsername());
                        binding.navigationStart.header.textViewPhoneNumber.setText(
                                PhoneNumberUtils.formatNumber(
                                        FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),
                                        Locale.getDefault().getCountry()
                                )
                        );
                    }
                });

        ScrollLockTouchListener touchListener = new ScrollLockTouchListener();
        binding.notesContent.recyclerViewNotes.addOnItemTouchListener(touchListener);

        binding.notesContent.swipeRefreshNotes.setOnRefreshListener(this::loadNotes);
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                touchListener.setScrollingEnabled(false);
                final int position = viewHolder.getLayoutPosition();
                final NoteModel note = notesAdapter.getDataSet().get(position);
                String text = "Note was removed from the list";
                Snackbar snackbar =
                        Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG)
                                .setAction("UNDO", view -> {
                                    StoreUtil.restoreNoteToFolder(note)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    notesAdapter.restore(note, position);
                                                }
                                            });
                                    binding.notesContent.recyclerViewNotes
                                            .scrollToPosition(position);
                                });

                StoreUtil.deleteNote(note).addOnCompleteListener(task -> {
                    touchListener.setScrollingEnabled(true);
                    if (task.isSuccessful()) {
                        notesAdapter.remove(position);
                        snackbar.show();
                    }
                });
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(binding.notesContent.recyclerViewNotes);

        binding.navigationStart.header.imageViewUser.setOnClickListener(view -> {
            startSettingsActivity();
        });

        binding.navigationStart.header.textViewUsername.setOnClickListener(view -> {
            startSettingsActivity();
        });

        binding.navigationStart.header.textViewPhoneNumber.setOnClickListener(view -> {
            startSettingsActivity();
        });
    }

    private void startSettingsActivity() {
        toggleNavigationView();
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void loadUserDetails() {
        StoreUtil.loadUserDetails(binding);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNotes() {
        StoreUtil.loadNotes(noteModels, notesAdapter, binding, this::setProgressNotes);
    }

    private void loadNotes(Void unused) {
        loadNotes();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadFolders() {
        StoreUtil.loadFolders(folderModels, foldersAdapter, binding,
                this::setProgressFolders);
    }

    private void setProgressNotes(boolean show) {
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

    private void setProgressFolders(boolean show) {
        if (binding == null) return;
        if (show) {
            binding.navigationStart.recyclerViewFolders.setVisibility(View.GONE);
            binding.navigationStart.circularProgressIndicatorFolders.show();
            binding.navigationStart.circularProgressIndicatorFolders
                    .setProgress(100, true);
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
        Log.d(Constants.TAG, "stop");
        EventBus.getDefault().unregister(notesAdapter);
        EventBus.getDefault().unregister(foldersAdapter);
        isRunning = false;
        EventBus.getDefault().post(new LockSlidrEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "destroy");
        EventBus.getDefault().unregister(this);
    }
}