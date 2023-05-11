package com.uzhnu.notesapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note implements Comparable<Note>, Parcelable {
    public static final String format = "dd MMMM yyyy HH:mm:ss";

    private String documentId;
    private String text;
    private Date lastEdited;
    private Date createdAt;

    public Note() {
    }

    public Note(String text) {
        this.text = text;
        this.createdAt = new Date();
        this.lastEdited = this.createdAt;
    }

    public Note(@NonNull Parcel in) throws ParseException {
        this.text = in.readString();
        this.documentId = in.readString();
        this.createdAt = new SimpleDateFormat(format, Locale.getDefault()).parse(in.readString());
        this.lastEdited = new SimpleDateFormat(format, Locale.getDefault()).parse(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(getText());
        parcel.writeString(getDocumentId());
        parcel.writeString(new SimpleDateFormat(format, Locale.getDefault()).format(getCreatedAt()));
        parcel.writeString(new SimpleDateFormat(format, Locale.getDefault()).format(getLastEdited()));
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            try {
                return new Note(in);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

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
