package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import java.util.Date;

public class FolderModel implements Comparable<FolderModel> {
    private String name;
    private Date createdAt;
    public FolderModel() {
    }

    public FolderModel(String name) {
        this.name = name;
        this.createdAt = new Date();
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

    @Override
    public int compareTo(FolderModel folderModel) {
        if (folderModel.getName().equals("Notes")) {
            return 1;
        }
        return this.getCreatedAt().compareTo(folderModel.getCreatedAt());
    }
}
