package com.uzhnu.notesapp.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uzhnu.notesapp.models.Note;
import com.uzhnu.notesapp.models.User;

public class FirebaseUtil {
    private static User user;

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
    public static CollectionReference getCurrentUserNotes() {
        return getCurrentUserDetails()
                .collection(Constants.KEY_COLLECTION_USER_NOTES);
    }
}
