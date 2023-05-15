package com.uzhnu.notesapp.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class AndroidUtil {
    public static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    public static String getPlainTextFromHtmlp(@NonNull String html) {
        return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "")
                .replace("&nbsp;", " ");
    }
}
