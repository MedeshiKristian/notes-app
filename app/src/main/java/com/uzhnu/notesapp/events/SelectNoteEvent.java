package com.uzhnu.notesapp.events;

public class SelectNoteEvent {
    private int countSelectedItems;

    public SelectNoteEvent(int countSelectedItems) {
        this.countSelectedItems = countSelectedItems;
    }

    public int getCountSelectedItems() {
        return this.countSelectedItems;
    }
}
