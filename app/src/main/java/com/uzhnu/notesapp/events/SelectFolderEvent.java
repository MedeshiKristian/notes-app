package com.uzhnu.notesapp.events;

import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.models.FolderModel;

public class SelectFolderEvent {
    private final FolderModel folderModel;
    private final FoldersAdapter.FoldersViewHolder holder;

    public SelectFolderEvent(FolderModel folderModel, FoldersAdapter.FoldersViewHolder holder) {
        this.folderModel = folderModel;
        this.holder = holder;
    }

    public FolderModel getFolder() {
        return folderModel;
    }

    public FoldersAdapter.FoldersViewHolder getHolder() {
        return holder;
    }
}
