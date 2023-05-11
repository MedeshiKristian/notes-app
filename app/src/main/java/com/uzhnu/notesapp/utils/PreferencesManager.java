package com.uzhnu.notesapp.utils;

import java.util.HashMap;
import java.util.Map;

public class PreferencesManager {
    private static Map<String, Object> data;

    public static Map<String, Object> getInstance() {
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }
}
