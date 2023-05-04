package com.uzhnu.notesapp.utils;

import android.content.Context;
import android.widget.Toast;

public class AndroidUtils {
    public static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
