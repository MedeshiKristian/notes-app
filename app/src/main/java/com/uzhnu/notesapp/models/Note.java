package com.uzhnu.notesapp.models;

import java.util.Date;

public class Note {
    public String text;
    private Date lastEdited;
    private Date createdAt;

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
}
