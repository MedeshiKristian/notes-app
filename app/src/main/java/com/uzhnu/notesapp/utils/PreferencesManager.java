package com.uzhnu.notesapp.utils;

import java.util.HashMap;
import java.util.Map;

public class PreferencesManager {
    private static Map<String, Object> instance;

    public static Map<String, Object> getInstance() {
        if (instance == null) {
            instance = new HashMap<>();
        }
        return instance;
    }
}
