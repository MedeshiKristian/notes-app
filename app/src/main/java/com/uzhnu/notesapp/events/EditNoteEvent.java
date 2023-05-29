package com.uzhnu.notesapp.events;

import com.uzhnu.notesapp.models.NoteModel;

public class EditNoteEvent {
    private NoteModel noteModel;
    private String newNoteText;

    public EditNoteEvent(String newNoteText) {
        this.newNoteText = newNoteText;
    }

    public EditNoteEvent(NoteModel noteModel) {
        this.noteModel = noteModel;
    }

    public boolean isNewNote() {
        return noteModel == null;
    }

    public NoteModel getNoteModel() {
        return noteModel;
    }

    public String getNewNoteText() {
        return newNoteText;
    }
}
