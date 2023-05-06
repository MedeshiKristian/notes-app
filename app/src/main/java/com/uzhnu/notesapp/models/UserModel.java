package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

public class UserModel {
    private String username;
    private String phoneNumber;
    private String image;

    public UserModel() {
    }

    public UserModel(@NonNull String username, @NonNull String phoneNumber, @NonNull String image) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.image = image;
    }

    @NonNull
    public String getUsername() throws NullPointerException {
        if (username == null) {
            throw new NullPointerException("Username is null");
        }
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPhoneNumber() throws NullPointerException {
        if (phoneNumber == null) {
            throw new NullPointerException("User phone number is null");
        }
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    public String getImage() throws NullPointerException {
        if (image == null) {
            throw new NullPointerException("User image is null");
        }
        return image;
    }

    public void setImage(@NonNull String image) {
        this.image = image;
    }
}

