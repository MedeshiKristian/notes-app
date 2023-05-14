package com.uzhnu.notesapp.events;

public class EditNoteEvent {
    private final String noteText;
    private String noteId;
    private int position;

    public EditNoteEvent(String noteText) {
        this.noteText = noteText;
        this.noteId = "";
        position = -1;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getNoteId() {
        return noteId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
