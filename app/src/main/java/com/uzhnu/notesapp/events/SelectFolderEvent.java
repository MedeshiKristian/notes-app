package com.uzhnu.notesapp.events;

import com.uzhnu.notesapp.adapters.FoldersAdapter;

public class SelectFolderEvent {
    private final String folderName;
    private final FoldersAdapter.FoldersViewHolder holder;

    public SelectFolderEvent(String folderName, FoldersAdapter.FoldersViewHolder holder) {
        this.folderName = folderName;
        this.holder = holder;
    }

    public String getFolderName() {
        return folderName;
    }

    public FoldersAdapter.FoldersViewHolder getHolder() {
        return holder;
    }
}
