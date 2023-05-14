package com.uzhnu.notesapp.events;

public class SelectFolderEvent {
    private String folderName;

    public SelectFolderEvent(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
