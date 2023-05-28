package com.uzhnu.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.os.BuildCompat;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.callbacks.SwipeToDeleteCallback;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.dialogs.DeleteNotesDialog;
import com.uzhnu.notesapp.dialogs.EditUsernameDialog;
import com.uzhnu.notesapp.events.EditNoteEvent;
import com.uzhnu.notesapp.events.LockSlidrEvent;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static boolean isRunning = false;
    private ActivityMainBinding binding;

    private List<NoteModel> noteModels;
    private NotesAdapter notesAdapter;
    private LinearLayoutManager layoutManager;

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

        Object multiselect = PreferencesManager.getInstance().get(Constants.KEY_MULTISELECT);
        if (multiselect != null) {
            boolean show = (boolean) multiselect;
            setOptionsVisibility(show);
        }

        searchView = (SearchView) menu.findItem(R.id.option_search).getActionView();
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
                DeleteNotesDialog dialog = new DeleteNotesDialog(new DeleteNotesDialog.DeleteNotesListener() {
                    @Override
                    public void onDialogPositiveClick(@NonNull DialogFragment dialog) {
                        notesAdapter.deleteAllSelectedNotes();
                        loadUserNotes();
                    }

                    @Override
                    public void onDialogCancelClick(@NonNull DialogFragment dialog) {
                        assert dialog.getDialog() != null;
                        dialog.getDialog().cancel();
                        EventBus.getDefault().post(new MultiSelectEvent(true));
                    }
                });
                dialog.show(getSupportFragmentManager(), "Delete notes dialog");
                return true;
            case (R.id.option_pin):
                notesAdapter.togglePinOnSelectedNotes();
                layoutManager.removeAllViews();
                loadUserNotes();
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
        isRunning = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().register(notesAdapter);
        EventBus.getDefault().register(foldersAdapter);

    }

    private void init() {
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
                = (ViewGroup.MarginLayoutParams) binding.toolbarInit.getLayoutParams();
        params.setMargins(0, statusBarHeight, 0, 0);
        binding.toolbarInit.setLayoutParams(params);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

//        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        window.getDecorView().setSystemUiVisibility(flags);


        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER,
                Constants.KEY_COLLECTION_FOLDER_DEFAULT);

        PreferencesManager.getInstance().put(Constants.KEY_MAIN_ACTIVITY, this);

        setInitToolBar();

//        for (int i = 0; i < 20; i++) {
//            FirebaseUtil.addUserNoteToFolder(new NoteModel("Note " + i));
//        }

        binding.notesContent.swipeRefreshNotes.setColorSchemeColors(
                ContextCompat.getColor(getApplicationContext(), R.color.primary)
        );

        imageUtil = new ImageUtil(this, binding.navigationStart.header.imageViewUser);

        layoutManager
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);

        noteModels = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteModels, layoutManager, getApplicationContext());
        binding.notesContent.recyclerViewNotes.setLayoutManager(layoutManager);
        binding.notesContent.recyclerViewNotes.setAdapter(notesAdapter);

        folderModels = new ArrayList<>();
        foldersAdapter = new FoldersAdapter(this, folderModels);
        binding.navigationStart.recyclerViewFolders.setAdapter(foldersAdapter);
    }

    private void setInitToolBar() {
        binding.toolbarInit.setTitle(
                (String) PreferencesManager.getInstance().get(Constants.KEY_CURRENT_FOLDER));

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
        setProgressNotes(true);
        if (event.isNewNote()) {
            FirebaseUtil.addUserNoteToFolder(noteModel).addOnCompleteListener(task -> {
                loadUserNotes();
                setProgressNotes(false);
            });
        } else {
            noteModel.setDocumentId(event.getNoteId());
            FirebaseUtil.updateUserNote(noteModel)
                    .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    loadUserNotes();
                                    setProgressNotes(false);
                                }
                            }
                    );
        }
    }

    @Subscribe
    public void onSelectFolderEvent(@NonNull SelectFolderEvent event) {
        toggleNavigationView();
        FolderModel folder = event.getFolder();
        binding.toolbarInit.setTitle(folder.getName());
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER,
                folder.getCollectionName());
        loadUserNotes();
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
            this.toolBarMenu.findItem(R.id.option_log_out).setVisible(!multiselectShow);
        }
    }

    private void setListeners() {
        binding.floatingActionButtonAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });


        binding.navigationStart.header.imageViewUser.setOnClickListener(view -> imageUtil.showBottomSheetPickImage());


        binding.notesContent.swipeRefreshNotes.setOnRefreshListener(this::loadUserNotes);
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, notesAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getLayoutPosition();
                final NoteModel note = notesAdapter.getDataSet().get(position);

                String text = "Note was removed from the list";
                Snackbar snackbar = Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE);
                snackbar.setTextColor(Color.BLACK);
                snackbar.setAction("UNDO", view -> {
                    FirebaseUtil.restoreNoteToFolder(note).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            notesAdapter.restore(note, position);
                        }
                    });
                    binding.notesContent.recyclerViewNotes.scrollToPosition(position);
                });
                snackbar.setActionTextColor(ContextCompat
                        .getColor(getApplicationContext(), R.color.primary));

                FirebaseUtil.deleteUserNote(note).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notesAdapter.remove(position);
                        snackbar.show();
                    }
                });
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(binding.notesContent.recyclerViewNotes);

        EditUsernameDialog dialog =
                new EditUsernameDialog(binding.navigationStart.header.textViewUsername);

        binding.navigationStart.header.textViewUsername.setOnClickListener(view -> {
            dialog.show(getSupportFragmentManager(), "Update username");
        });
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
        setProgressNotes(true);
        FirebaseUtil.getCurrentFolderNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        noteModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            noteModels.add(FirebaseUtil.getNoteFromDocument(queryDocumentSnapshot));
                        }
                        Collections.sort(noteModels);
                        notesAdapter.setDataSet(noteModels);
                        binding.notesContent.recyclerViewNotes.smoothScrollToPosition(0);
                        binding.notesContent.swipeRefreshNotes.setRefreshing(false);
                    }
                    setProgressNotes(false);
                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUserFolders() {
        setProgressFolders(true);
        FirebaseUtil.getFolders().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        folderModels.clear();
                        folderModels.add(new FolderModel(Constants.KEY_COLLECTION_FOLDER_DEFAULT));
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            folderModels.add(FirebaseUtil.getFolderFromDocument(queryDocumentSnapshot));
                        }
                        Collections.sort(folderModels);
                        foldersAdapter.notifyDataSetChanged();
                        binding.navigationStart.recyclerViewFolders.smoothScrollToPosition(0);
                    }
                    setProgressFolders(false);
                });
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
        EventBus.getDefault().unregister(foldersAdapter);
        isRunning = false;
        EventBus.getDefault().post(new LockSlidrEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}