package com.uzhnu.notesapp.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.models.CategoryModel;
import com.uzhnu.notesapp.models.NoteModel;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
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
    public static NoteModel getNote(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        NoteModel noteModel = new NoteModel();
        noteModel.setText(queryDocumentSnapshot.getString(Constants.KEY_TEXT));
        noteModel.setLastEdited(queryDocumentSnapshot.getDate(Constants.KEY_LAST_EDITED));
        noteModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        noteModel.setDocumentId(queryDocumentSnapshot.getId());
        return noteModel;
    }

    @NonNull
    public static CategoryModel getCategory(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        CategoryModel categoryMode = new CategoryModel();
        categoryMode.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
        return categoryMode;
    }

    @NonNull
    public static CollectionReference getCurrentUserNotes() {
        return getCurrentUserDetails()
                .collection(Constants.KEY_COLLECTION_NOTES_DEFAULT);
    }

    @NonNull
    public static Task<DocumentReference> addUserNote(@NonNull NoteModel noteModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_TEXT, noteModel.getText());
        objectMap.put(Constants.KEY_LAST_EDITED, noteModel.getLastEdited());
        objectMap.put(Constants.KEY_CREATED_AT, noteModel.getCreatedAt());
        return FirebaseUtil.getCurrentUserNotes()
                .add(objectMap);
    }

    @NonNull
    public static Task<Void> deleteUserNote(@NonNull NoteModel noteModel) {
        return getCurrentUserNotes().document(noteModel.getDocumentId()).delete();
    }

    @NonNull
    public static DocumentReference getUserNote(String noteId) {
        return getCurrentUserNotes().document(noteId);
    }

    @NonNull
    public static CollectionReference  getCurrentUserCategories() {
        return FirebaseUtil.getCurrentUserDetails()
                .collection(Constants.KEY_COLLECTION_CATEGORIES_NAMES);
    }
}
