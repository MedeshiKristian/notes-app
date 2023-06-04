package com.uzhnu.notesapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String TAG = "myLogs";
    public static final String KEY_NIGHT_THEME = "night";
    public static final String KEY_COLLECTION_NAME = "collectionName";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_USER = "user";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_LAST_EDITED_AT = "lastEditedAt";
    public static final String KEY_LAST_EDITED_BY = "lastEditedBy";
    public static final String KEY_COLLECTION_FOLDER_DEFAULT = "Notes";
    public static final String KEY_NOTE = "note";
    public static final String KEY_TEXT = "text";
    public static final String KEY_PINNED = "pinned";
    public static final String KEY_COLLECTION_FOLDERS = "folders";
    public static final String KEY_NAME = "name";
    //    public static final String KEY_POSITION = "position";
    public static final String KEY_CURRENT_FOLDER = "currentFolder";
    public static final String KEY_EDITORS = "editors";
    public static final String KEY_MULTISELECT = "multiselect";
    public static final String KEY_MAIN_ACTIVITY = "mainActivity";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    private static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA57QiUtA:APA91bEvtdSjuVAr9swg1HuFCsdyXY6nL7Ky-ucnIz9rmqxdXCQ-vUB3suHd67Gx9BDZPcqlW-wzgAHKVz2pPKHBrEKm1ijbte-HyJc355nZwMycqBHHo_zrgze40rgQrJiSjYbgIhCC"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_TO = "to";
    public static final String KEY_CONTENT_TITLE = "title";
    public static final String KEY_CONTENT_TEXT = "body";
    public static final String KEY_NOTIFICATION_ID = "id";
    public static final String KEY_NOTE_PATH = "notePath";
}
