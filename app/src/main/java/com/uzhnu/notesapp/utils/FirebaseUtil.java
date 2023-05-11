package com.uzhnu.notesapp.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.models.Note;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
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
        return FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(getCurrentUserId());
    }

    @NonNull
    public static Note getNote(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        Note note = new Note();
        note.setText(queryDocumentSnapshot.getString(Constants.KEY_TEXT));
        note.setLastEdited(queryDocumentSnapshot.getDate(Constants.KEY_LAST_EDITED));
        note.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        note.setDocumentId(queryDocumentSnapshot.getId());
        return note;
    }

    @NonNull
    public static Task<DocumentReference> addUserNote(@NonNull Note note) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_TEXT, note.getText());
        objectMap.put(Constants.KEY_LAST_EDITED, note.getLastEdited());
        objectMap.put(Constants.KEY_CREATED_AT, note.getCreatedAt());
        return FirebaseUtil.getCurrentUserNotes()
                .add(objectMap);
    }

    @NonNull
    public static Task<Void> deleteUserNote(@NonNull Note note) {
        return getCurrentUserNotes().document(note.getDocumentId()).delete();
    }

    @NonNull
    public static DocumentReference getUserNote(String noteId) {
        return getCurrentUserNotes().document(noteId);
    }

    @NonNull
    public static CollectionReference getCurrentUserNotes() {
        return getCurrentUserDetails()
                .collection(Constants.KEY_COLLECTION_USER_NOTES);
    }
}
