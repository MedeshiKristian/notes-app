package com.uzhnu.notesapp.models;

import android.util.Log;

import com.uzhnu.notesapp.utils.Constants;

import java.util.Date;

public class Note implements Comparable<Note> {
    public String text;
    private Date lastEdited;
    private Date createdAt;
    private String documentId;

    public Note() {
    }

    public Note(String text) {
        this.text = text;
        this.createdAt = new Date();
        this.lastEdited = this.createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Date lastEdited) {
        this.lastEdited = lastEdited;
    }

    @Override
    public int compareTo(Note note) {
        return note.getLastEdited().compareTo(this.getLastEdited());
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
