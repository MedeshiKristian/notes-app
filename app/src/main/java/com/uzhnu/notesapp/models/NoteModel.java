package com.uzhnu.notesapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;

import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NoteModel implements Comparable<NoteModel>, Parcelable {
    public static final String DATE_FORMAT = "dd MMMM yyyy HH:mm:ss";

    private String documentId;
    private String text;
    private Date lastEdited;
    private Date createdAt;
    private String lastEditedBy;
    private Boolean isPined;

    public NoteModel() {
    }

    public NoteModel(String text) {
        this.text = text;
        this.isPined = false;
        this.createdAt = new Date();
        this.lastEdited = this.createdAt;
        this.lastEditedBy = AuthUtil.getCurrentUserId();
    }

    public NoteModel(@NonNull Parcel in) throws ParseException {
        this.text = in.readString();
        this.documentId = in.readString();
        this.createdAt = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(in.readString());
        this.lastEdited = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(getText());
        parcel.writeString(getDocumentId());
        parcel.writeString(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(getCreatedAt()));
        parcel.writeString(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(getLastEdited()));
    }

    public static final Creator<NoteModel> CREATOR = new Creator<NoteModel>() {
        @Nullable
        @Override
        public NoteModel createFromParcel(Parcel in) {
            try {
                return new NoteModel(in);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public NoteModel[] newArray(int size) {
            return new NoteModel[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void updateLastEdited() {
        this.lastEdited = new Date();
        this.lastEditedBy = AuthUtil.getCurrentUserId();
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
    public int compareTo(@NonNull NoteModel noteModel) {
        if (this.isPined() != noteModel.isPined()) {
            return noteModel.isPined().compareTo(this.isPined());
        }
        return noteModel.getLastEdited().compareTo(this.getLastEdited());
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public Boolean isPined() {
        return isPined;
    }

    public void setPined(Boolean pined) {
        isPined = pined;
    }

    public void togglePin() {
        setPined(!isPined());
    }

    @NonNull
    public static NoteModel toNote(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        NoteModel noteModel = new NoteModel();
        noteModel.setText(queryDocumentSnapshot.getString(Constants.KEY_TEXT));
        noteModel.setLastEdited(queryDocumentSnapshot.getDate(Constants.KEY_LAST_EDITED_AT));
        noteModel.setPined(queryDocumentSnapshot.getBoolean(Constants.KEY_PINNED));
        noteModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        noteModel.setLastEditedBy(queryDocumentSnapshot.getString(Constants.KEY_LAST_EDITED_BY));
        noteModel.setDocumentId(queryDocumentSnapshot.getId());
        return noteModel;
    }

    @NonNull
    public static NoteModel toNote(@NonNull DocumentSnapshot documentSnapshot) {
        NoteModel noteModel = new NoteModel();
        noteModel.setText(documentSnapshot.getString(Constants.KEY_TEXT));
        noteModel.setLastEdited(documentSnapshot.getDate(Constants.KEY_LAST_EDITED_AT));
        noteModel.setPined(documentSnapshot.getBoolean(Constants.KEY_PINNED));
        noteModel.setCreatedAt(documentSnapshot.getDate(Constants.KEY_CREATED_AT));
        noteModel.setLastEditedBy(documentSnapshot.getString(Constants.KEY_LAST_EDITED_BY));
        noteModel.setDocumentId(documentSnapshot.getId());
        return noteModel;
    }

    @NonNull
    public static Map<String, Object> toMap(@NonNull NoteModel noteModel) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(Constants.KEY_TEXT, noteModel.getText());
        objectMap.put(Constants.KEY_LAST_EDITED_AT, noteModel.getLastEdited());
        objectMap.put(Constants.KEY_PINNED, noteModel.isPined());
        objectMap.put(Constants.KEY_CREATED_AT, noteModel.getCreatedAt());
        objectMap.put(Constants.KEY_LAST_EDITED_BY, noteModel.getLastEditedBy());
        return objectMap;
    }
}
