package com.uzhnu.notesapp.utilities.firebase;

import android.annotation.SuppressLint;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.adapters.NotesAdapter;
import com.uzhnu.notesapp.databinding.ActivityMainBinding;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.ImageUtil;
import com.uzhnu.notesapp.utilities.PreferencesManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class StoreUtil {
    public static void loadUserDetails(ActivityMainBinding binding) {
        StoreUtil.getCurrentUser().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        UserModel userModel = UserModel.toUser(task.getResult());
                        PreferencesManager.getInstance()
                                .put(Constants.KEY_IMAGE, userModel.getImage());
                        binding.navigationStart.header.imageViewUser
                                .setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
                        binding.navigationStart.header.textViewUsername
                                .setText(userModel.getUsername());
                        binding.navigationStart.header.textViewPhoneNumber
                                .setText(PhoneNumberUtils.formatNumber(
                                        FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),
                                        Locale.getDefault().getCountry()
                                ));
                        PreferencesManager.getInstance().put(Constants.KEY_USER, userModel);
                    } else {
                        Log.e(Constants.TAG, "Task for getting user image failed");
                    }
                });
    }

    public static void loadNotes(List<NoteModel> noteModels,
                                 NotesAdapter notesAdapter,
                                 ActivityMainBinding binding,
                                 @NonNull Consumer<Boolean> setProgress) {
        setProgress.accept(true);
        StoreUtil.getCurrentFolderNotes().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        noteModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (!queryDocumentSnapshot.getId().equals(Constants.KEY_EDITORS)) {
                                noteModels.add(NoteModel.toNote(queryDocumentSnapshot));
                            }
                        }
                        Collections.sort(noteModels);
                        notesAdapter.setDataSet(noteModels);
                        binding.notesContent.recyclerViewNotes.smoothScrollToPosition(0);
                        binding.notesContent.swipeRefreshNotes.setRefreshing(false);
                    }
                    setProgress.accept(false);
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void loadFolders(List<FolderModel> folderModels,
                                   FoldersAdapter foldersAdapter,
                                   ActivityMainBinding binding,
                                   @NonNull Consumer<Boolean> setProgress) {
        setProgress.accept(true);
        final Consumer<FolderModel> subscribeToFolder = folderModel -> {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic(MessagingUtil.getTopic(folderModel))
                    .addOnCompleteListener(task2 -> {
                        String msg = "Subscribed";
                        if (!task2.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(Constants.TAG, msg);
                    });
        };
        StoreUtil.getFolders().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        folderModels.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FolderModel folderModel = FolderModel.toFolder(queryDocumentSnapshot);
                            folderModels.add(folderModel);
                            subscribeToFolder.accept(folderModel);
                        }
                        if (folderModels.isEmpty()) {
                            FolderModel folderModel = new FolderModel(Constants.KEY_COLLECTION_FOLDER_DEFAULT);
//                            UserModel userModel = new UserModel();
//                            userModel.setUserId(AuthUtil.getCurrentUserId());
                            StoreUtil.addFolder(folderModel)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            folderModels.add(folderModel);
                                            foldersAdapter.notifyDataSetChanged();
                                            binding.navigationStart.recyclerViewFolders.smoothScrollToPosition(0);
                                        }
                                    });
                        } else {
                            Collections.sort(folderModels);
                            foldersAdapter.notifyDataSetChanged();
                            binding.navigationStart.recyclerViewFolders.smoothScrollToPosition(0);
                        }
                    }
                    setProgress.accept(false);
                });
    }

    @NonNull
    public static FirebaseFirestore getDatebase() {
        return FirebaseFirestore.getInstance();
    }

    @NonNull
    public static DocumentReference getCurrentUser() {
        return StoreUtil.getUser(AuthUtil.getCurrentUserId());
    }

    @NonNull
    public static CollectionReference getUsers() {
        return StoreUtil.getDatebase().collection(Constants.KEY_COLLECTION_USERS);
    }

    @NonNull
    public static DocumentReference getUser(String userId) {
        return StoreUtil.getDatebase()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(userId);
    }

    @NonNull
    public static Task<DocumentReference> addNoteToFolder(@NonNull NoteModel noteModel) {
        return StoreUtil.getCurrentFolderNotes()
                .add(NoteModel.toMap(noteModel));
    }

    @NonNull
    public static Task<Void> restoreNoteToFolder(@NonNull NoteModel noteModel) {
        return StoreUtil.getCurrentFolderNotes()
                .document(noteModel.getDocumentId())
                .set(NoteModel.toMap(noteModel));
    }

    @NonNull
    public static Task<Void> deleteUserNote(@NonNull NoteModel noteModel) {
        return getCurrentFolderNotes().document(noteModel.getDocumentId()).delete();
    }

    @NonNull
    public static DocumentReference getNoteFromFolder(String noteId) {
        return getCurrentFolderNotes().document(noteId);
    }

    @NonNull
    public static Task<Void> updateNote(@NonNull NoteModel noteModel) {
        noteModel.updateLastEdited();
        return StoreUtil.getNoteFromFolder(noteModel.getDocumentId())
                .update(Constants.KEY_TEXT, noteModel.getText(),
                        Constants.KEY_LAST_EDITED_AT, noteModel.getLastEdited(),
                        Constants.KEY_LAST_EDITED_BY, noteModel.getLastEditedBy(),
                        Constants.KEY_PINNED, noteModel.isPined());
    }

    @NonNull
    public static CollectionReference getFolders() {
        return StoreUtil.getCurrentUser()
                .collection(Constants.KEY_COLLECTION_FOLDERS);
    }

    public static void setCurrentFolder(@NonNull FolderModel folder) {
        PreferencesManager.getInstance().put(Constants.KEY_CURRENT_FOLDER, folder);
    }

    @NonNull
    public static FolderModel getCurrentFolder() {
        return (FolderModel) Objects.requireNonNull(PreferencesManager.getInstance().get(Constants.KEY_CURRENT_FOLDER));
    }

    @NonNull
    public static CollectionReference getCurrentFolderNotes() {
        FolderModel folder = getCurrentFolder();
        return StoreUtil.getUser(folder.getCreatedBy())
                .collection(folder.getCollectionName());
    }

    @NonNull
    public static String getDocumentId(@NonNull FolderModel folderModel) {
        return folderModel.getCreatedBy() + folderModel.getCollectionName();
    }

    @NonNull
    public static Task<Void> addFolder(@NonNull FolderModel folderModel,
                                                    @NonNull UserModel userModel) {
        return StoreUtil.getUser(userModel.getId())
                .collection(Constants.KEY_COLLECTION_FOLDERS)
                .document(getDocumentId(folderModel))
                .set(FolderModel.toMap(folderModel))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addEditor(folderModel, userModel);
                    }
                });
    }

    @NonNull
    public static Task<Void> updateFolderEditors(@NonNull FolderModel folderModel,
                                                              @NonNull UserModel userModel) {
        return StoreUtil.getUser(userModel.getId())
                .collection(Constants.KEY_COLLECTION_FOLDERS)
                .document(getDocumentId(folderModel))
                .set(FolderModel.toMap(folderModel))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateEditors(folderModel, userModel);
                    }
                });
    }

    @NonNull
    public static Task<Void> addFolder(@NonNull FolderModel folderModel) {
        UserModel userModel = new UserModel();
        userModel.setUserId(AuthUtil.getCurrentUserId());
        return addFolder(folderModel, userModel);
    }

    private static void addEditor(@NonNull FolderModel folderModel,
                                 @NonNull UserModel userModel) {
        Map<String, Object> data = new HashMap<>();
        data.put(userModel.getId(), 1);
        StoreUtil.getUser(folderModel.getCreatedBy())
                .collection(folderModel.getCollectionName())
                .document(Constants.KEY_EDITORS)
                .set(data);
    }

    private static void updateEditors(@NonNull FolderModel folderModel,
                                  @NonNull UserModel userModel) {
        Map<String, Object> data = new HashMap<>();
        data.put(userModel.getId(), 1);
        StoreUtil.getUser(folderModel.getCreatedBy())
                .collection(folderModel.getCollectionName())
                .document(Constants.KEY_EDITORS)
                .update(data);
    }
}
