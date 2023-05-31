package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import java.util.Date;

public class UserModel {
    private String username;
    private String phoneNumber;
    private String image;
    private Date createdAt;
    private String userId;

    public UserModel() {
    }

    public UserModel(@NonNull String username, @NonNull String phoneNumber, @NonNull String image) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.createdAt = new Date();
    }

    public String getUsername() throws NullPointerException {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() throws NullPointerException {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() throws NullPointerException {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreatedAt() throws NullPointerException {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

