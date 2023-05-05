package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserModel {
    private String username;
    private String phoneNumber;
    private String image;

    public UserModel(@NonNull String username, @NonNull String phoneNumber, @NonNull String image) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.image = image;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    public void setImage(@NonNull String image) {
        this.image = image;
    }
}

