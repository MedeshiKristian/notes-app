package com.uzhnu.notesapp.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
    @NonNull
    public static FirebaseFirestore getDatebase() {
        return FirebaseFirestore.getInstance();
    }

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @NonNull
    public static DocumentReference getCurrentUserDetails() {
        return getDatebase()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(getCurrentUserId());
    }

    @NonNull
    public static NoteModel getNoteFromDocument(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        NoteModel noteModel = new NoteModel();
        noteModel.setText(queryDocumentSnapshot.getString(Constants.KEY_TEXT));
        noteModel.setLastEdited(queryDocumentSnapshot.getDate(Constants.KEY_LAST_EDITED));
        noteModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        noteModel.setDocumentId(queryDocumentSnapshot.getId());
        return noteModel;
    }

    @NonNull
    public static FolderModel getFolderFromDocument(
            @NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        FolderModel categoryMode = new FolderModel();
        categoryMode.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
        return categoryMode;
    }

    private static String getCurrentFolder() {
        return (String) PreferencesManager.getInstance().get(Constants.KEY_CURRENT_FOLDER);
    }

    @NonNull
    public static CollectionReference getCurrentFolderNotes() {
        return FirebaseUtil.getCurrentUserDetails().collection(getCurrentFolder());
    }

    @NonNull
    public static Task<DocumentReference> addUserNoteToFolder(@NonNull NoteModel noteModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_TEXT, noteModel.getText());
        objectMap.put(Constants.KEY_LAST_EDITED, noteModel.getLastEdited());
        objectMap.put(Constants.KEY_CREATED_AT, noteModel.getCreatedAt());
        return FirebaseUtil.getCurrentFolderNotes().add(objectMap);
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
    public static Task<Void> updateUserNote(@NonNull NoteModel noteModel) {
        return FirebaseUtil.getNoteFromFolder(noteModel.getDocumentId())
                .update(Constants.KEY_TEXT, noteModel.getText(),
                        Constants.KEY_LAST_EDITED, noteModel.getLastEdited());
    }

    @NonNull
    public static CollectionReference getFolders() {
        return FirebaseUtil.getCurrentUserDetails()
                .collection(Constants.KEY_COLLECTION_FOLDERS_NAMES);
    }
}
