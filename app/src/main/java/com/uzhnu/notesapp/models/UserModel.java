package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.utilities.Constants;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    public static UserModel toUser(@NonNull QueryDocumentSnapshot queryDocumentSnapshot) {
        UserModel userModel = new UserModel();
        userModel.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
        userModel.setUsername(queryDocumentSnapshot.getString(Constants.KEY_USERNAME));
        userModel.setPhoneNumber(queryDocumentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
        userModel.setCreatedAt(queryDocumentSnapshot.getDate(Constants.KEY_CREATED_AT));
        userModel.setUserId(queryDocumentSnapshot.getId());
        return userModel;
    }

    @NonNull
    public static UserModel toUser(@NonNull DocumentSnapshot documentSnapshot) {
        UserModel userModel = new UserModel();
        userModel.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
        userModel.setUsername(documentSnapshot.getString(Constants.KEY_USERNAME));
        userModel.setPhoneNumber(documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
        userModel.setCreatedAt(documentSnapshot.getDate(Constants.KEY_CREATED_AT));
        userModel.setUserId(documentSnapshot.getId());
        return userModel;
    }
}

