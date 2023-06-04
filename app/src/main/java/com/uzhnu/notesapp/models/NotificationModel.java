package com.uzhnu.notesapp.models;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class NotificationModel {
    private String title;
    private String text;
    private String senderPhoneNumber;
    private int id;
    private String notePath;
    public NotificationModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        this.senderPhoneNumber = senderPhoneNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNotePath(String notePath) {
        this.notePath = notePath;
    }

    public String getNotePath() {
        return notePath;
    }

    @NonNull
    public static NotificationModel toNotification(@NonNull RemoteMessage remoteMessage) {
        NotificationModel notificationModel = new NotificationModel();
        Map<String, String> data = remoteMessage.getData();
        notificationModel.setSenderPhoneNumber(data.get(Constants.KEY_PHONE_NUMBER));
        notificationModel.setTitle(data.get(Constants.KEY_CONTENT_TITLE));
        notificationModel.setText(data.get(Constants.KEY_CONTENT_TEXT));
        notificationModel.setNotePath(data.get(Constants.KEY_NOTE_PATH));
        notificationModel.setId(Integer.parseInt(Objects.requireNonNull(data.get(Constants.KEY_NOTIFICATION_ID))));
        return notificationModel;
    }

    @NonNull
    public static JSONObject toJson(@NonNull NotificationModel notificationModel)
            throws JSONException {
        JSONObject data = new JSONObject();
        data.put(Constants.KEY_PHONE_NUMBER, AuthUtil.getUserPhoneNumber());
        data.put(Constants.KEY_CONTENT_TITLE, notificationModel.getTitle());
        data.put(Constants.KEY_CONTENT_TEXT, notificationModel.getText());
        data.put(Constants.KEY_NOTE_PATH, notificationModel.getNotePath());
        data.put(Constants.KEY_NOTIFICATION_ID, notificationModel.getId());
        return data;
    }
}
