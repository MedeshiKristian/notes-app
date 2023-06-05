package com.uzhnu.notesapp.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.uzhnu.notesapp.R;

public class ThemeUtil {
    @ColorInt
    private static int getColor(@NonNull Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);;
        return typedValue.data;
    }
    @ColorInt
    public static int getPrimary(@NonNull Context context) {
        return getColor(context, R.attr.themePrimary);
    }

    @ColorInt
    public static int getSecondary(@NonNull Context context) {
        return getColor(context, R.attr.themeSecondary);
    }

    @ColorInt
    public static int getTextColor(@NonNull Context context) {
        return getColor(context, R.attr.themeTextColor);
    }

    @ColorInt
    public static int getSelectionColor(@NonNull Context context) {
        return getColor(context, R.attr.themeSelectionColor);
    }
}
