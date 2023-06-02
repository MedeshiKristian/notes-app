package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.FirebaseAuthUtil;
import com.uzhnu.notesapp.utilities.FirebaseStoreUtil;

import java.util.Date;

public class FolderModel implements Comparable<FolderModel> {
    private String name;
    private String collectionName;
    private Date createdAt;
    private String documentId;
    public String createdBy;

    public FolderModel() {
    }

    public FolderModel(String name) {
        this.name = name;
        this.collectionName = name;
        this.createdAt = new Date();
        this.createdBy = FirebaseAuthUtil.getCurrentUserId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public int compareTo(@NonNull FolderModel folderModel) {
        if (folderModel.getName().equals(Constants.KEY_COLLECTION_FOLDER_DEFAULT)) {
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
}
