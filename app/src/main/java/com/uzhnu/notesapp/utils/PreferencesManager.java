package com.uzhnu.notesapp.utils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PreferencesManager {
    private static Map<String, Object> instance;

    @NonNull
    public static Map<String, Object> getInstance() {
        if (instance == null) {
            instance = new HashMap<>();
        }
        return instance;
    }
}
