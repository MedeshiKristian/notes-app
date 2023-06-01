package com.uzhnu.notesapp.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AndroidUtil {
    private static final String DATE_FORMAT = "MMMM/dd/yyyy - HH:mm:ss";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
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

    @NonNull
    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }
}
