package com.uzhnu.notesapp.listeners;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScrollLockTouchListener implements RecyclerView.OnItemTouchListener {
    private boolean isScrollingDisabled = false;

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return isScrollingDisabled;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void setScrollingEnabled(boolean scrollingEnabled) {
        isScrollingDisabled = !scrollingEnabled;
    }
}
