package com.uzhnu.notesapp.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

public class CustomDrawerLayout extends DrawerLayout {
    private static final int EDGE_SIZE_MULTIPLY = 4;

    public CustomDrawerLayout(@NonNull Context context) {
        super(context);
        setMarginEdge();
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMarginEdge();
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMarginEdge();
    }

    private void setMarginEdge() {
        try {
            Field dragger = this.getClass().getSuperclass().getDeclaredField("mLeftDragger");
            dragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) dragger.get(this);
            Field edgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            edgeSize.setAccessible(true);
            int edge = edgeSize.getInt(draggerObj);
            edgeSize.setInt(draggerObj, edge * EDGE_SIZE_MULTIPLY);
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
