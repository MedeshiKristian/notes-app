package com.uzhnu.notesapp.utils;

import android.icu.text.CaseMap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.NoteModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseStoreUtil {
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
    public static Task<DocumentSnapshot> getUserName(@NonNull String userId) {
        return getDatebase()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(userId).get();
    }

    @NonNull
    public static NoteModel getNoteFromDocument(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        NoteModel noteModel = new NoteModel();
        noteModel.setText(queryDocumentSnapshot.getString(Constants.KEY_TEXT));
        noteModel.setLastEdited(queryDocumentSnapshot.getDate(Constants.KEY_LAST_EDITED_AT));
        noteModel.setPined(queryDocumentSnapshot.getBoolean(Constants.KEY_PINNED));
        noteModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        noteModel.setLastEditedBy(queryDocumentSnapshot.getString(Constants.KEY_LAST_EDITED_BY));
        noteModel.setDocumentId(queryDocumentSnapshot.getId());
        return noteModel;
    }

    @NonNull
    public static Map<String, Object> getObjectFromNote(@NonNull NoteModel noteModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_TEXT, noteModel.getText());
        objectMap.put(Constants.KEY_LAST_EDITED_AT, noteModel.getLastEdited());
        objectMap.put(Constants.KEY_PINNED, noteModel.isPined());
        objectMap.put(Constants.KEY_CREATED_AT, noteModel.getCreatedAt());
        objectMap.put(Constants.KEY_LAST_EDITED_BY, noteModel.getLastEditedBy());
        return objectMap;
    }

    @NonNull
    public static Task<DocumentReference> addNoteToFolder(@NonNull NoteModel noteModel) {
        return FirebaseStoreUtil.getCurrentFolderNotes()
                .add(getObjectFromNote(noteModel));
    }

    @NonNull
    public static Task<Void> restoreNoteToFolder(@NonNull NoteModel noteModel) {
        return FirebaseStoreUtil.getCurrentFolderNotes()
                .document(noteModel.getDocumentId())
                .set(getObjectFromNote(noteModel));
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
        return FirebaseStoreUtil.getNoteFromFolder(noteModel.getDocumentId())
                .update(Constants.KEY_TEXT, noteModel.getText(),
                        Constants.KEY_LAST_EDITED_AT, noteModel.getLastEdited(),
                        Constants.KEY_PINNED, noteModel.isPined());
    }

    @NonNull
    public static CollectionReference getFolders() {
        return FirebaseStoreUtil.getCurrentUserDetails()
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
        return FirebaseStoreUtil.getDatebase()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(folder.getCreatedBy())
                .collection(folder.getCollectionName());
    }

    @NonNull
    public static Map<String, Object> getObjectFromFolder(@NonNull FolderModel folderModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_NAME, folderModel.getName());
        objectMap.put(Constants.KEY_CREATED_AT, folderModel.getCreatedAt());
        objectMap.put(Constants.KEY_COLLECTION_NAME, folderModel.getCollectionName());
        objectMap.put(Constants.KEY_CREATED_BY, folderModel.getCreatedBy());
        return objectMap;
    }

    @NonNull
    public static FolderModel getFolderFromDocument(
            @NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        FolderModel folderModel = new FolderModel();
        folderModel.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
        folderModel.setCollectionName(queryDocumentSnapshot.getString(Constants.KEY_COLLECTION_NAME));
        folderModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        folderModel.setCreatedBy(queryDocumentSnapshot.getString(Constants.KEY_CREATED_BY));
        folderModel.setDocumentId(queryDocumentSnapshot.getId());
        return folderModel;
    }

    @NonNull
    public static Task<DocumentReference> addFolder(@NonNull FolderModel folderModel) {
        return FirebaseStoreUtil.getFolders()
                .add(getObjectFromFolder(folderModel));
    }

    @NonNull
    public static Task<Void> updateFolder(@NonNull FolderModel folderModel) {
        return FirebaseStoreUtil.getDatebase()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(folderModel.getCreatedBy())
                .collection(Constants.KEY_COLLECTION_FOLDERS)
                .document(folderModel.getDocumentId())
                .update(Constants.KEY_NAME, folderModel.getName());
    }

    @NonNull
    public static Task<Void> deleteFolder(@NonNull FolderModel folderModel) {
        return FirebaseStoreUtil.getDatebase()
                .collection(folderModel.getCreatedBy())
                .document(folderModel.getDocumentId())
                .delete();
    }
}
