package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FolderModel implements Comparable<FolderModel> {
    private String collectionName;
    private String name;
    private Date createdAt;
    private String createdBy;

    public FolderModel() {
    }

    public FolderModel(String name) {
        this.name = name;
        this.collectionName = name;
        this.createdAt = new Date();
        this.createdBy = AuthUtil.getCurrentUserId();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public int compareTo(@NonNull FolderModel folderModel) {
        if (folderModel.getCollectionName().equals(Constants.KEY_COLLECTION_FOLDER_DEFAULT)) {
            return 1;
        }
        return this.getCreatedAt().compareTo(folderModel.getCreatedAt());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @NonNull
    public static Map<String, Object> toMap(@NonNull FolderModel folderModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_CREATED_AT, folderModel.getCreatedAt());
        objectMap.put(Constants.KEY_COLLECTION_NAME, folderModel.getCollectionName());
        objectMap.put(Constants.KEY_NAME, folderModel.getName());
        objectMap.put(Constants.KEY_CREATED_BY, folderModel.getCreatedBy());
        return objectMap;
    }

    @NonNull
    public static FolderModel toFolder(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        FolderModel folderModel = new FolderModel();
        folderModel.setCollectionName(queryDocumentSnapshot.getString(Constants.KEY_COLLECTION_NAME));
        folderModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        folderModel.setCreatedBy(queryDocumentSnapshot.getString(Constants.KEY_CREATED_BY));
        folderModel.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
        return folderModel;
    }
}
